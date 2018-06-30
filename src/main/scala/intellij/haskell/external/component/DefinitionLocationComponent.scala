/*
 * Copyright 2014-2018 Rik van der Kleij
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package intellij.haskell.external.component

import java.util.concurrent.TimeUnit

import com.github.blemale.scaffeine.{AsyncLoadingCache, Scaffeine}
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.search.GlobalSearchScope
import intellij.haskell.external.repl.StackRepl.StackReplOutput
import intellij.haskell.external.repl.StackReplsManager
import intellij.haskell.navigation.HaskellReference
import intellij.haskell.psi._
import intellij.haskell.util.index.HaskellFilePathIndex
import intellij.haskell.util.{ApplicationUtil, HaskellProjectUtil, LineColumnPosition, ScalaUtil}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future, TimeoutException}

private[component] object DefinitionLocationComponent {
  private final val LocAtPattern = """(.+)\:\(([\d]+),([\d]+)\)-\(([\d]+),([\d]+)\)""".r
  private final val PackageModulePattern = """.+\:([\w\.\-]+)""".r

  private case class Key(psiFile: PsiFile, namedElement: HaskellNamedElement, moduleName: Option[String], name: String)

  type DefinitionLocationResult = Either[NoInfo, DefinitionLocation]

  private final val Cache: AsyncLoadingCache[Key, DefinitionLocationResult] = Scaffeine().buildAsync((k: Key) => findDefinitionLocationResult(k))

  def findDefinitionLocation(psiFile: PsiFile, namedElement: HaskellNamedElement, isCurrentFile: Boolean): DefinitionLocationResult = {
    ApplicationUtil.runReadAction(HaskellPsiUtil.findQualifiedNameParent(namedElement).map(_.getIdentifierElement)).map(ne => find(psiFile, isCurrentFile, ne)) match {
      case Some(r) => r
      case None => Left(NoInfoAvailable)
    }
  }

  def invalidate(psiFile: PsiFile): Unit = {
    val keys = Cache.synchronous().asMap().filter(_._1.psiFile == psiFile).flatMap { case (k, v) =>
      v.toOption match {
        case Some(location) =>
          if (k.namedElement.isValid && location.element.isValid && k.name == ApplicationUtil.runReadAction(location.element.getName)) {
            None
          } else {
            Some(k)
          }
        case None => Some(k)
      }
    }

    Cache.synchronous().invalidateAll(keys)
  }

  def invalidateAll(project: Project): Unit = {
    val files = Cache.synchronous().asMap().map(_._1.psiFile).filter(_.getProject == project)
    files.foreach(invalidate)
  }

  private def findDefinitionLocationResult(key: Key): DefinitionLocationResult = {
    if (LoadComponent.isBusy(key.psiFile)) {
      Left(ReplIsBusy)
    } else {
      val psiFile = key.psiFile
      val project = psiFile.getProject

      val name = key.name
      if (name.headOption.exists(_.isUpper)) {
        createDefinitionLocationResult(project, psiFile, key, withoutLastColumn = true)
      } else {
        createDefinitionLocationResult(project, psiFile, key, withoutLastColumn = false)
      }
    }
  }

  private def createDefinitionLocationResult(project: Project, psiFile: PsiFile, key: Key, withoutLastColumn: Boolean): DefinitionLocationResult = {
    val name = key.name
    findLocationInfo(key, psiFile, name: String, project, withoutLastColumn) match {
      case Some(o) => o.stdoutLines.headOption.map(l => createDefinitionLocationResultFromLocationInfo(project, psiFile, l, name)) match {
        case Some(r) => r
        case None => Left(NoInfoAvailable)
      }
      case None => Left(ReplNotAvailable)
    }
  }

  private def findLocationInfo(key: Key, psiFile: PsiFile, name: String, project: Project, withoutLastColumn: Boolean): Option[StackReplOutput] = {
    val namedElement = key.namedElement
    for {
      sp <- ApplicationUtil.runReadAction(LineColumnPosition.fromOffset(psiFile, namedElement.getTextRange.getStartOffset))
      ep <- ApplicationUtil.runReadAction(LineColumnPosition.fromOffset(psiFile, namedElement.getTextRange.getEndOffset))
      endColumnNr = if (withoutLastColumn) ep.columnNr - 1 else ep.columnNr
      repl <- StackReplsManager.getProjectRepl(psiFile)
      output <- repl.findLocationInfo(key.moduleName, psiFile, sp.lineNr, sp.columnNr, ep.lineNr, endColumnNr, name)
    } yield output
  }

  private def createDefinitionLocationResultFromLocationInfo(project: Project, psiFile: PsiFile, output: String, name: String): DefinitionLocationResult = {
    val namedElement = output match {
      case LocAtPattern(filePath, startLineNr, startColumnNr, _, _) =>
        ApplicationUtil.runReadAction(HaskellReference.findIdentifierByLocation(filePath, startLineNr.toInt, startColumnNr.toInt, name, project))
      case PackageModulePattern(moduleName) =>
        val module = HaskellProjectUtil.findModuleForFile(psiFile)
        ApplicationUtil.runReadAction(HaskellReference.findIdentifiersByModuleName(moduleName, name, project, module)).headOption
      case _ => None
    }
    namedElement match {
      case Some(e) => Right(DefinitionLocation(e))
      case None => Left(NoInfoAvailable)
    }
  }

  private final val Timeout = Duration.create(100, TimeUnit.MILLISECONDS)

  private def find(psiFile: PsiFile, isCurrentFile: Boolean, namedElement: HaskellNamedElement): DefinitionLocationResult = {
    val project = psiFile.getProject
    val moduleName = HaskellFilePathIndex.findModuleName(psiFile, GlobalSearchScope.projectScope(project))
    val name = ApplicationUtil.runReadAction(namedElement.getName)
    val key = Key(psiFile, namedElement, moduleName, name)

    def matchResult(result: DefinitionLocationResult) = {
      result match {
        case Right(_) => result
        case Left(ReplNotAvailable) | Left(IndexNotReady) =>
          Cache.synchronous().invalidate(key)
          result
        case Left(NoInfoAvailable) =>
          result
        case Left(ReplIsBusy) =>
          if (!isCurrentFile && !project.isDisposed) {
            Thread.sleep(100)
            find(psiFile, isCurrentFile, namedElement)
          } else {
            Cache.synchronous().invalidate(key)
            result
          }
      }
    }

    if (isCurrentFile && LoadComponent.isModuleLoaded(moduleName, psiFile)) {
      ApplicationManager.getApplication.executeOnPooledThread(ScalaUtil.runnable {
        LocationInfoUtil.preloadLocationsAround(project, psiFile, namedElement)
      })
    }

    if (!LoadComponent.isModuleLoaded(moduleName, psiFile) && isCurrentFile) {
      Left(NoInfoAvailable)
    } else {
      matchResult(wait(Cache.get(key)))
    }
  }

  private def wait(f: => Future[DefinitionLocationResult]) = {
    try {
      Await.result(f, Timeout)
    } catch {
      case _: TimeoutException => Left(ReplIsBusy)
    }
  }
}

object LocationInfoUtil {

  import java.util.concurrent.ConcurrentHashMap

  import com.intellij.openapi.application.ApplicationManager
  import com.intellij.psi.PsiElement
  import intellij.haskell.psi.HaskellPsiUtil

  import scala.collection.JavaConverters._

  private val activeTaskByTarget = new ConcurrentHashMap[String, Boolean]().asScala

  def preloadLocationsAround(project: Project, psiFile: PsiFile, namedElement: PsiElement): Unit = {
    HaskellComponentsManager.findStackComponentInfo(psiFile) match {
      case Some(stackComponentInfo) =>
        val target = stackComponentInfo.target
        val putResult = activeTaskByTarget.put(target, true)
        if (putResult.isEmpty) {
          if (namedElement.isValid && !project.isDisposed) {
            val namedElements = ApplicationUtil.runReadAction(HaskellPsiUtil.findExpressionParent(namedElement).map(HaskellPsiUtil.findNamedElements).getOrElse(Iterable()))
            ApplicationManager.getApplication.executeOnPooledThread(ScalaUtil.runnable {
              try
                namedElements.foreach { e =>
                  if (!project.isDisposed && !LoadComponent.isBusy(project, stackComponentInfo)) {
                    DefinitionLocationComponent.findDefinitionLocation(psiFile, e, isCurrentFile = true)
                    // We have to wait for other requests which have more priority because those are on dispatch thread
                    Thread.sleep(200)
                  }
                } finally {
                activeTaskByTarget.remove(target)
              }
            })
          }
        }
      case None => ()
    }
  }
}

case class DefinitionLocation(element: HaskellNamedElement)

