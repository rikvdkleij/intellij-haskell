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
import com.intellij.openapi.application._
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import intellij.haskell.external.repl.StackRepl.StackReplOutput
import intellij.haskell.external.repl.StackReplsManager
import intellij.haskell.navigation.HaskellReference
import intellij.haskell.psi._
import intellij.haskell.util._

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future, TimeoutException}

private[component] object DefinitionLocationComponent {
  private final val LocAtPattern = """(.+)\:\(([\d]+),([\d]+)\)-\(([\d]+),([\d]+)\)""".r
  private final val PackageModulePattern = """.+\:([\w\.\-]+)""".r

  private case class Key(psiFile: PsiFile, moduleName: Option[String], qualifiedNameElement: HaskellQualifiedNameElement)

  type DefinitionLocationResult = Either[NoInfo, DefinitionLocation]

  private final val Cache: AsyncLoadingCache[Key, DefinitionLocationResult] = Scaffeine().buildAsync((k: Key) => findDefinitionLocationResult(k))

  def findDefinitionLocation(psiFile: PsiFile, qualifiedNameElement: HaskellQualifiedNameElement, isCurrentFile: Boolean): DefinitionLocationResult = {
    find(psiFile, qualifiedNameElement, isCurrentFile, initialRequest = true)
  }

  def invalidate(psiFile: PsiFile): Unit = {
    val keys = Cache.synchronous().asMap().filter(_._1.psiFile == psiFile).flatMap { case (k, v) =>
      v.toOption match {
        case Some(definitionLocation) =>
          if (ApplicationUtil.runReadAction(k.qualifiedNameElement.isValid) &&
            ApplicationUtil.runReadAction(definitionLocation.namedElement.isValid) &&
            ApplicationUtil.runReadAction(k.qualifiedNameElement.getName) == ApplicationUtil.runReadAction(definitionLocation.namedElement.getName)) {
            None
          } else {
            Some(k)
          }
        case None => Some(k)
      }
    }

    Cache.synchronous().invalidateAll(keys)

    val otherFileKeys = Cache.synchronous().asMap().flatMap { case (k, v) =>
      v.toOption match {
        case Some(definitionLocation) =>
          if (ApplicationUtil.runReadAction(definitionLocation.namedElement.isValid) &&
            ApplicationUtil.runReadAction(k.qualifiedNameElement.isValid) &&
            definitionLocation.namedElement.getContainingFile == psiFile &&
            ApplicationUtil.runReadAction(k.qualifiedNameElement.getName) == ApplicationUtil.runReadAction(definitionLocation.namedElement.getName)) {
            None
          } else {
            Some(k)
          }
        case None => None
      }
    }

    Cache.synchronous().invalidateAll(otherFileKeys)
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
      val identifierElement = key.qualifiedNameElement.getIdentifierElement
      val name = ApplicationUtil.runInReadActionWithWriteActionPriority(project, {
        if (key.qualifiedNameElement.isValid) {
          Right(identifierElement.getName)
        }
        else {
          Left(NoInfoAvailable("-- invalid PSI element", psiFile.getName))
        }
      }, "getName and check if PSI element is valid")

      name match {
        case Left(noInfo) => Left(noInfo)
        case Right(r) => r match {
          case Left(noInfo) => Left(noInfo)
          case Right(n) =>
            if (n.headOption.exists(_.isUpper)) {
              createDefinitionLocationResult(project, psiFile, key, n, withoutLastColumn = true)
            } else {
              createDefinitionLocationResult(project, psiFile, key, n, withoutLastColumn = false)
            }
        }
      }
    }
  }

  private def createDefinitionLocationResult(project: Project, psiFile: PsiFile, key: Key, name: String, withoutLastColumn: Boolean): DefinitionLocationResult = {
    findLocationInfoWithRepl(project, psiFile, key, name, withoutLastColumn) match {
      case Right(o) => o.stdoutLines.headOption.map(l => createDefinitionLocationResultFromLocationInfo(project, psiFile, l, key, name)) match {
        case Some(r) => r
        case None => Left(NoInfoAvailable(name, key.psiFile.getName))
      }
      case Left(noInfo) => Left(noInfo)
    }
  }

  private def findLocationInfoWithRepl(project: Project, psiFile: PsiFile, key: Key, name: String, withoutLastColumn: Boolean): Either[NoInfo, StackReplOutput] = {
    val qualifiedNameElement = key.qualifiedNameElement
    (for {
      vf <- HaskellFileUtil.findVirtualFile(psiFile)
      sp <- LineColumnPosition.fromOffset(vf, qualifiedNameElement.getTextRange.getStartOffset)
      ep <- LineColumnPosition.fromOffset(vf, qualifiedNameElement.getTextRange.getEndOffset)
      endColumnNr = if (withoutLastColumn) ep.columnNr - 1 else ep.columnNr
    } yield {
      StackReplsManager.getProjectRepl(psiFile).flatMap(_.findLocationInfo(key.moduleName, psiFile, sp.lineNr, sp.columnNr, ep.lineNr, endColumnNr, name))
    }) match {
      case None => Left(NoInfoAvailable(name, psiFile.getContainingFile.getName))
      case Some(output) => output match {
        case None => Left(ReplNotAvailable)
        case Some(o) => Right(o)
      }
    }
  }

  private def createDefinitionLocationResultFromLocationInfo(project: Project, psiFile: PsiFile, output: String, key: Key, name: String): DefinitionLocationResult = {
    val (moduleName, namedElement) = output match {
      case LocAtPattern(filePath, startLineNr, startColumnNr, _, _) =>
        val (vf, file) = HaskellProjectUtil.findFile(filePath, project)
        file match {
          case Right(pf) =>
            ApplicationUtil.runInReadActionWithWriteActionPriority(project,
              HaskellReference.findIdentifierByLocation(project, vf, pf, startLineNr.toInt, startColumnNr.toInt, name), s"findIdentifierByLocation for $name of file ${psiFile.getName}"
            ) match {
              case Left(noInfo) => (None, Left(noInfo))
              case Right(r) => (r._1, Right(r._2))
            }
          case Left(noInfo) => (None, Left(noInfo))
        }
      case PackageModulePattern(mn) =>
        val module = HaskellProjectUtil.findModuleForFile(psiFile)
        HaskellReference.findFileByModuleName(project, module, mn) match {
          case Right(pf) => (Some(mn), ApplicationUtil.runInReadActionWithWriteActionPriority(project, pf.flatMap(HaskellReference.findIdentifierInFileByName(_, name)), s"findIdentifierInFileByName for $name in module $mn"))
          case Left(noInfo) => (None, Left(noInfo))
        }
      case _ => (None, Right(None))
    }
    namedElement match {
      case Right(ne) => ne match {
        case Some(e) => Right(DefinitionLocation(moduleName, e))
        case None => Left(NoInfoAvailable(name, key.psiFile.getName))
      }
      case Left(noInfo) => Left(noInfo)
    }
  }

  private[component] final val Timeout = Duration.create(50, TimeUnit.MILLISECONDS)

  private[component] def find(psiFile: PsiFile, qualifiedNameElement: HaskellQualifiedNameElement, isCurrentFile: Boolean, initialRequest: Boolean): DefinitionLocationResult = {
    def wait(f: => Future[DefinitionLocationResult]): DefinitionLocationResult = {
      try {
        Await.result(f, Timeout)
      } catch {
        case _: TimeoutException =>
          val name = ApplicationUtil.runReadAction(qualifiedNameElement.getName)
          Left(ReadActionTimeout(s"waiting for definition location result $name in ${psiFile.getName}"))
      }
    }

    val project = psiFile.getProject
    val moduleName = HaskellPsiUtil.findModuleName(psiFile)
    val key = Key(psiFile, moduleName, qualifiedNameElement)

    if (initialRequest && LoadComponent.isModuleLoaded(moduleName, psiFile)) {
      LocationInfoUtil.preloadLocationsInExpression(project, psiFile, qualifiedNameElement)
    }

    if (isCurrentFile && LoadComponent.isBusy(psiFile)) {
      Left(ReplIsBusy)
    } else if (isCurrentFile && !LoadComponent.isModuleLoaded(moduleName, psiFile)) {
      Left(ModuleNotLoaded(psiFile.getName))
    } else {
      val result = wait(Cache.get(key))
      result match {
        case Right(location) =>
          if (ApplicationUtil.runReadAction(location.namedElement.isValid)) {
            result
          } else {
            Cache.synchronous.invalidate(key)
            Left(NoInfoAvailable("-- invalid PSI element", psiFile.getName))
          }
        case Left(ReplIsBusy) | Left(ReadActionTimeout(_)) | Left(IndexNotReady) =>
          Cache.synchronous().invalidate(key)

          val application = ApplicationManager.getApplication

          import scala.concurrent.duration._

          if (!project.isDisposed) {
            if (application.isDispatchThread) {
              ApplicationUtil.scheduleInReadActionWithWriteActionPriority(project, {

                def find() = {
                  val result = Cache.synchronous.get(key)
                  result match {
                    case Right(_) => result
                    case Left(ReplIsBusy) | Left(ReadActionTimeout(_)) | Left(IndexNotReady) =>
                      Cache.synchronous.invalidate(key)
                      result
                    case l@Left(_) => l
                  }
                }

                find() match {
                  case r@Right(_) => r
                  case Left(ReplIsBusy) | Left(ReadActionTimeout(_)) | Left(IndexNotReady) =>
                    // Wait a moment to let the read actions breath
                    Thread.sleep(50)
                    find()
                  case l@Left(_) => l
                }

              }, s"finding location for for ${qualifiedNameElement.getName} in file ${psiFile.getName}", 60.seconds) match {
                case Right(location) => location
                case Left(noInfo) =>
                  HaskellEditorUtil.showStatusBarBalloonMessage(project, s"No result after waiting one minute to find all locations for ${qualifiedNameElement.getName}")
                  Left(noInfo)
              }
            } else {
              find(psiFile, qualifiedNameElement, isCurrentFile, initialRequest = false)
            }
          } else {
            result
          }
        case _ =>
          Cache.synchronous().invalidate(key)
          result
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
                      Thread.sleep(DefinitionLocationComponent.Timeout.toMillis)
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
    ApplicationUtil.runInReadActionWithWriteActionPriority(project, HaskellPsiUtil.findExpressionParent(qualifiedNameElement), "findExpressionParent").toOption.flatten match {
      case Some(p) => ApplicationUtil.runInReadActionWithWriteActionPriority(project, {
        if (qualifiedNameElement.isValid) {
          HaskellPsiUtil.findQualifiedNamedElements(p)
        } else {
          Iterable()
        }
      }, "isValid and findQualifiedNameElements").toOption.getOrElse(Iterable())
      case None => Iterable()
    }
  }

  private def isPreloadValid(project: Project, qualifiedNameElement: HaskellQualifiedNameElement) = {
    if (ApplicationManager.getApplication.isDispatchThread) {
      qualifiedNameElement.isValid && !project.isDisposed
    } else {
      ApplicationUtil.runInReadActionWithWriteActionPriority(project, qualifiedNameElement.isValid, "isValid").toOption.contains(true) && !project.isDisposed
    }
  }
}

case class DefinitionLocation(moduleName: Option[String], namedElement: HaskellNamedElement)

