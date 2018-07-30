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
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import intellij.haskell.external.repl.StackRepl.StackReplOutput
import intellij.haskell.external.repl.StackReplsManager
import intellij.haskell.navigation.HaskellReference
import intellij.haskell.psi._
import intellij.haskell.util._

import scala.annotation.tailrec
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future, TimeoutException}

private[component] object DefinitionLocationComponent {
  private final val LocAtPattern = """(.+)\:\(([\d]+),([\d]+)\)-\(([\d]+),([\d]+)\)""".r
  private final val PackageModulePattern = """.+\:([\w\.\-]+)""".r

  private case class Key(psiFile: PsiFile, moduleName: Option[String], qualifiedNameElement: HaskellQualifiedNameElement, name: String)

  type DefinitionLocationResult = Either[NoInfo, DefinitionLocation]

  private final val Cache: AsyncLoadingCache[Key, DefinitionLocationResult] = Scaffeine().buildAsync((k: Key) => findDefinitionLocationResult(k))

  def findDefinitionLocation(psiFile: PsiFile, qualifiedNameElement: HaskellQualifiedNameElement, isCurrentFile: Boolean): DefinitionLocationResult = {
    find(psiFile, qualifiedNameElement, isCurrentFile, initialRequest = true)
  }

  def invalidate(psiFile: PsiFile): Unit = {
    val keys = Cache.synchronous().asMap().filter(_._1.psiFile == psiFile).flatMap { case (k, v) =>
      v.toOption match {
        case Some(definitionLocation) =>
          if (ApplicationUtil.runReadAction(k.qualifiedNameElement.isValid) && ApplicationUtil.runReadAction(definitionLocation.namedElement.isValid) && k.name == ApplicationUtil.runReadAction(definitionLocation.namedElement.getName)) {
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
    Cache.synchronous().asMap().filter(_._1.psiFile.getProject == project).keys.foreach(Cache.synchronous.invalidate)
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
    findLocationInfo(project, psiFile, key, withoutLastColumn) match {
      case Some(o) => o.stdoutLines.headOption.map(l => createDefinitionLocationResultFromLocationInfo(project, psiFile, l, key)) match {
        case Some(r) => r
        case None => Left(NoInfoAvailable)
      }
      case None => Left(ReplNotAvailable)
    }
  }

  private def findLocationInfo(project: Project, psiFile: PsiFile, key: Key, withoutLastColumn: Boolean): Option[StackReplOutput] = {
    val qualifiedNameElement = key.qualifiedNameElement
    val name = key.name
    for {
      vf <- HaskellFileUtil.findVirtualFile(psiFile)
      sp <- LineColumnPosition.fromOffset(vf, qualifiedNameElement.getTextRange.getStartOffset)
      ep <- LineColumnPosition.fromOffset(vf, qualifiedNameElement.getTextRange.getEndOffset)
      endColumnNr = if (withoutLastColumn) ep.columnNr - 1 else ep.columnNr
      repl <- StackReplsManager.getProjectRepl(psiFile)
      output <- repl.findLocationInfo(key.moduleName, psiFile, sp.lineNr, sp.columnNr, ep.lineNr, endColumnNr, name)
    } yield output
  }

  private def createDefinitionLocationResultFromLocationInfo(project: Project, psiFile: PsiFile, output: String, key: Key): DefinitionLocationResult = {
    val name = key.name
    val (moduleName, namedElement) = output match {
      case LocAtPattern(filePath, startLineNr, startColumnNr, _, _) =>
        val (virtualFile, psiFile) = HaskellProjectUtil.findFile(filePath, project)
        HaskellReference.findIdentifierByLocation(project, virtualFile,  psiFile, startLineNr.toInt, startColumnNr.toInt, name)
      case PackageModulePattern(mn) =>
        val module = HaskellProjectUtil.findModuleForFile(psiFile)
        val file = HaskellReference.findFileByModuleName(project, module, mn)

        (Some(mn), file.flatMap(f => HaskellReference.findIdentifierInFileByName(f, name)))
      case _ => (None, None)
    }
    namedElement match {
      case Some(e) => Right(DefinitionLocation(moduleName, e))
      case None => Left(NoInfoAvailable)
    }
  }

  private final val CurrentFileTimeout = Duration.create(50, TimeUnit.MILLISECONDS)
  private final val Timeout = Duration.create(1, TimeUnit.SECONDS)

  @tailrec
  private[component] def find(psiFile: PsiFile, qualifiedNameElement: HaskellQualifiedNameElement, isCurrentFile: Boolean, initialRequest: Boolean): DefinitionLocationResult = {
    def wait(f: => Future[DefinitionLocationResult]) = {
      try {
        Await.result(f, if (isCurrentFile) CurrentFileTimeout else Timeout)
      } catch {
        case _: TimeoutException => Left(ReplIsBusy)
      }
    }

    val project = psiFile.getProject
    val moduleName = HaskellPsiUtil.findModuleName(psiFile)
    val identifierElement = qualifiedNameElement.getIdentifierElement
    val name = ApplicationUtil.runReadAction(identifierElement.getName)
    val key = Key(psiFile, moduleName, qualifiedNameElement, name)

    if (initialRequest && LoadComponent.isModuleLoaded(moduleName, psiFile)) {
      LocationInfoUtil.preloadLocationsInExpression(project, psiFile, qualifiedNameElement)
    }

    if (!LoadComponent.isModuleLoaded(moduleName, psiFile) && isCurrentFile) {
      Left(NoInfoAvailable)
    } else {
      val result = wait(Cache.get(key))
      result match {
        case Right(_) => result
        case Left(ReplNotAvailable) | Left(IndexNotReady) =>
          Cache.synchronous().invalidate(key)
          result
        case Left(NoInfoAvailable) =>
          result
        case Left(ReplIsBusy) =>
          Cache.synchronous().invalidate(key)
          if (!isCurrentFile && !project.isDisposed) {
            Thread.sleep(100)
            find(psiFile, qualifiedNameElement, isCurrentFile, initialRequest = false)
          } else {
            result
          }
      }
    }
  }
}

object LocationInfoUtil {

  import java.util.concurrent.ConcurrentHashMap

  import com.intellij.openapi.application.ApplicationManager
  import intellij.haskell.psi.HaskellPsiUtil

  import scala.collection.JavaConverters._

  private val activeTaskByTarget = new ConcurrentHashMap[String, Boolean]().asScala

  def preloadLocationsInExpression(project: Project, psiFile: PsiFile, qualifiedNameElement: HaskellQualifiedNameElement): Unit = {
    HaskellComponentsManager.findStackComponentInfo(psiFile) match {
      case Some(stackComponentInfo) =>
        val target = stackComponentInfo.target
        val putResult = activeTaskByTarget.put(target, true)
        if (putResult.isEmpty) {
          if (isPreloadValid(project, qualifiedNameElement)) {
            ApplicationManager.getApplication.executeOnPooledThread(ScalaUtil.runnable {
              try {
                if (isPreloadValid(project, qualifiedNameElement)) {
                  val namedElementsInsideExpression = findNameElementsInExpression(project, qualifiedNameElement)
                  namedElementsInsideExpression.toSeq.diff(Seq(qualifiedNameElement)).foreach { ne =>
                    if (!project.isDisposed && !LoadComponent.isBusy(project, stackComponentInfo)) {
                      DefinitionLocationComponent.find(psiFile, ne, isCurrentFile = true, initialRequest = false)
                      // We have to wait for other requests which have more priority because those are on dispatch thread
                      Thread.sleep(50)
                    }
                  }
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

  private def findNameElementsInExpression(project: Project, qualifiedNameElement: HaskellQualifiedNameElement) = {
    val parent = HaskellPsiUtil.findExpressionParent(qualifiedNameElement, inReadAction = true)
    if (ApplicationUtil.runReadAction(parent.exists(_.isValid))) {
      parent.map(p => ApplicationUtil.runReadAction(HaskellPsiUtil.findQualifiedNamedElements(p))).getOrElse(Iterable())
    } else {
      Iterable()
    }
  }

  private def isPreloadValid(project: Project, qualifiedNameElement: HaskellQualifiedNameElement) = {
    ApplicationUtil.runReadAction(qualifiedNameElement.isValid) && !project.isDisposed
  }
}

case class DefinitionLocation(moduleName: Option[String], namedElement: HaskellNamedElement)

