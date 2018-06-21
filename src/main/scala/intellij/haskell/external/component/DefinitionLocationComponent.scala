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
import com.intellij.openapi.project.{IndexNotReadyException, Project}
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.{PsiElement, PsiFile}
import intellij.haskell.external.repl.StackRepl.StackReplOutput
import intellij.haskell.external.repl.StackReplsManager
import intellij.haskell.psi._
import intellij.haskell.util.index.{HaskellFilePathIndex, HaskellModuleNameIndex}
import intellij.haskell.util.{ApplicationUtil, HaskellProjectUtil, LineColumnPosition, ScalaUtil}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future, TimeoutException}

private[component] object DefinitionLocationComponent {
  private final val LocAtPattern = """(.+)\:\(([\d]+),([\d]+)\)-\(([\d]+),([\d]+)\)""".r
  private final val PackageModulePattern = """.+\:([\w\.\-]+)""".r

  private case class Key(startLineNr: Int, startColumnNr: Int, endLineNr: Int, endColumnNr: Int, name: String, psiFile: PsiFile, moduleName: Option[String])

  type DefinitionLocationResult = Either[NoInfo, LocationInfo]

  private final val Cache: AsyncLoadingCache[Key, DefinitionLocationResult] = Scaffeine().buildAsync((k: Key) => findDefinitionLocation(k))

  def findDefinitionLocation(namedElement: HaskellNamedElement, psiFile: PsiFile, isCurrentFile: Boolean, runInRead: Boolean = false): DefinitionLocationResult = {
    (for {
      ne <- HaskellPsiUtil.findQualifiedNameParent(namedElement).map(_.getIdentifierElement)
      textOffset = ne.getTextOffset
      sp <- LineColumnPosition.fromOffset(psiFile, textOffset, runInRead = runInRead)
      ep <- LineColumnPosition.fromOffset(psiFile, textOffset + ne.getTextLength, runInRead = runInRead)
    } yield find(psiFile, sp, ep, ApplicationUtil.runReadAction(ne.getName, runInRead), isCurrentFile, ne)) match {
      case Some(r) => r
      case None => Left(NoInfoAvailable)
    }
  }

  private def findElement(lineNr: Int, columnNr: Int, psiFile: PsiFile): Option[PsiElement] = {
    val fromPosition = LineColumnPosition(lineNr, columnNr)
    ApplicationManager.getApplication.runReadAction(ScalaUtil.computable(
      LineColumnPosition.getOffset(psiFile, fromPosition).flatMap(offset => Option(psiFile.findElementAt(offset)))
    ))
  }

  private def findName(element: Option[PsiElement]) = {
    ApplicationManager.getApplication.runReadAction(ScalaUtil.computable(
      element.flatMap(HaskellPsiUtil.findQualifiedNameParent).map(_.getIdentifierElement).map(_.getName)
    ))
  }

  def invalidate(fromFile: PsiFile): Unit = {
    val keyValueMap = Cache.synchronous().asMap().filter(_._1.psiFile == fromFile)
    val project = fromFile.getProject
    val keys = keyValueMap.flatMap { case (k, v) =>
      v.toOption match {
        case Some(info) =>
          val fromElement = findElement(k.startLineNr, k.startColumnNr, fromFile)
          info match {
            case DefinitionLocationInfo(filePath, startLineNr, startColumnNr, _, _) =>
              val toFile = ApplicationManager.getApplication.runReadAction(ScalaUtil.computable(
                HaskellProjectUtil.findFile(filePath, project)
              ))
              val toElement = toFile.flatMap(f => findElement(startLineNr, startColumnNr, f))
              (fromElement, toElement) match {
                case (Some(fe), Some(te)) if fe.getText == te.getText => None
                case (_, _) => Some(k)
              }
            case ModuleLocationInfo(_, lib) =>
              val name = findName(fromElement)
              name match {
                case None => Some(k)
                case Some(n) =>
                  if (lib) {
                    None
                  } else {
                    Some(k)
                  }
              }
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

  private def findDefinitionLocation(key: Key): DefinitionLocationResult = {
    if (LoadComponent.isBusy(key.psiFile)) {
      Left(ReplIsBusy)
    } else {
      val psiFile = key.psiFile
      val project = psiFile.getProject

      if (key.name.headOption.exists(_.isUpper)) {
        createLocationInfo(project, psiFile, key, withoutLastColumn = true)
      } else {
        createLocationInfo(project, psiFile, key, withoutLastColumn = false)
      }
    }
  }

  private def createLocationInfo(project: Project, psiFile: PsiFile, key: Key, withoutLastColumn: Boolean): DefinitionLocationResult = {
    findLocationInfo(key, psiFile, project, withoutLastColumn) match {
      case Some(o) => o.stdoutLines.headOption.map(l => createLocationInfo(project, psiFile, l)) match {
        case Some(r) => r
        case None => Left(NoInfoAvailable)
      }
      case None => Left(ReplNotAvailable)
    }
  }

  private def findLocationInfo(key: Key, psiFile: PsiFile, project: Project, withoutLastColumn: Boolean): Option[StackReplOutput] = {
    val endColumnNr = if (withoutLastColumn) key.endColumnNr - 1 else key.endColumnNr
    StackReplsManager.getProjectRepl(psiFile).flatMap(_.findLocationInfo(key.moduleName, psiFile, key.startLineNr, key.startColumnNr, key.endLineNr, endColumnNr, key.name))
  }

  private def createLocationInfo(project: Project, psiFile: PsiFile, output: String): DefinitionLocationResult = {
    output match {
      case LocAtPattern(filePath, startLineNr, startColumnNr, endLineNr, endColumnNr) => Right(DefinitionLocationInfo(filePath.trim, startLineNr.toInt, startColumnNr.toInt, endLineNr.toInt, endColumnNr.toInt))
      case PackageModulePattern(moduleName) =>
        val module = HaskellProjectUtil.findModuleForFile(psiFile)
        try {
          val library = !module.exists(m => ApplicationManager.getApplication.runReadAction(ScalaUtil.computable(HaskellModuleNameIndex.findHaskellFileByModuleName(project, moduleName, GlobalSearchScope.moduleScope(m)).isDefined)))
          Right(ModuleLocationInfo(moduleName, library))
        } catch {
          case _: IndexNotReadyException => Left(IndexNotReady)
        }
      case _ => Left(NoInfoAvailable)
    }
  }

  private final val Timeout = Duration.create(100, TimeUnit.MILLISECONDS)

  private def find(psiFile: PsiFile, startPosition: LineColumnPosition, endPosition: LineColumnPosition, expression: String, isCurrentFile: Boolean, namedElement: HaskellNamedElement): DefinitionLocationResult = {
    val project = psiFile.getProject
    val moduleName = HaskellFilePathIndex.findModuleName(psiFile, GlobalSearchScope.projectScope(project))
    val key = Key(startPosition.lineNr, startPosition.columnNr, endPosition.lineNr, endPosition.columnNr, expression, psiFile, moduleName)

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
            find(psiFile, startPosition, endPosition, expression, isCurrentFile, namedElement)
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
    HaskellComponentsManager.findStackComponentInfo(psiFile).map(_.target) match {
      case Some(target) =>
        val putResult = activeTaskByTarget.put(target, true)
        if (putResult.isEmpty) {
          if (namedElement.isValid && !project.isDisposed) {
            val namedElements = ApplicationManager.getApplication.runReadAction(ScalaUtil.computable {
              HaskellPsiUtil.findExpressionParent(namedElement).map(HaskellPsiUtil.findNamedElements).getOrElse(Iterable())
            })

            ApplicationManager.getApplication.executeOnPooledThread(ScalaUtil.runnable {
              try
                namedElements.foreach { e =>
                  if (!project.isDisposed) {
                    DefinitionLocationComponent.findDefinitionLocation(e, psiFile, isCurrentFile = true, runInRead = true)
                    // We have to wait for other requests which have more priority because those are on dispatch thread
                    Thread.sleep(200)
                  }
                }
              finally {
                activeTaskByTarget.remove(target)
              }
            })
          }
        }
      case None => ()
    }
  }
}

sealed trait LocationInfo

case class DefinitionLocationInfo(filePath: String, startLineNr: Int, startColumnNr: Int, endLineNr: Int, endColumnNr: Int) extends LocationInfo

case class ModuleLocationInfo(moduleName: String, library: Boolean) extends LocationInfo
