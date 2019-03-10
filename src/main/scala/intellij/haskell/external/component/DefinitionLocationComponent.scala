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

import com.github.blemale.scaffeine.{AsyncLoadingCache, Scaffeine}
import com.intellij.openapi.application._
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.util.WaitFor
import intellij.haskell.HaskellNotificationGroup
import intellij.haskell.external.repl.StackRepl.StackReplOutput
import intellij.haskell.external.repl.{ProjectStackRepl, StackReplsManager}
import intellij.haskell.navigation.HaskellReference
import intellij.haskell.psi._
import intellij.haskell.util._
import intellij.haskell.util.index.HaskellModuleNameIndex._

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

private[component] object DefinitionLocationComponent {
  private final val LocAtPattern = """(.+)\:\(([\d]+),([\d]+)\)-\(([\d]+),([\d]+)\)""".r
  private final val PackageModulePattern = """([\w\-\d\.]+)(?:\-.*)?\:([\w\.\-]+)""".r

  // importQualifier is only set for identifiers in import declarations
  private case class Key(psiFile: PsiFile, qualifiedNameElement: HaskellQualifiedNameElement, importQualifier: Option[String])

  type DefinitionLocationResult = Either[NoInfo, DefinitionLocation]

  private final val Cache: AsyncLoadingCache[Key, DefinitionLocationResult] = Scaffeine().buildAsync((k: Key) => {
    if (ApplicationManager.getApplication.isReadAccessAllowed) {
      findDefinitionLocationResult(k)
    } else {
      ApplicationUtil.runInReadActionWithWriteActionPriority(k.psiFile.getProject, findDefinitionLocationResult(k), "find definition location", 1.second) match {
        case Right(x) => x
        case Left(noInfo) => Left(noInfo)
      }
    }
  })

  def findDefinitionLocation(psiFile: PsiFile, qualifiedNameElement: HaskellQualifiedNameElement, importQualifier: Option[String]): DefinitionLocationResult = {
    val key = Key(psiFile, qualifiedNameElement, importQualifier)
    find(key)
  }

  def findDefinitionLocationInCache(psiFile: PsiFile, qualifiedNameElement: HaskellQualifiedNameElement): Option[DefinitionLocation] = {
    Cache.synchronous().asMap().find { case (k, _) => k.psiFile == psiFile && k.qualifiedNameElement == qualifiedNameElement }.flatMap(_._2.toOption)
  }

  def invalidateOtherFiles(currentFile: PsiFile, name: String): Unit = {
    val synchronousCache = Cache.synchronous()
    val keys = synchronousCache.asMap().filter { case (k, v) => k.qualifiedNameElement.getIdentifierElement.getName == name && k.psiFile != currentFile }.keys
    keys.foreach(synchronousCache.invalidate)
  }

  private def checkValidKey(key: Key): Boolean = {
    try {
      ApplicationUtil.runReadAction(key.qualifiedNameElement.isValid) && ApplicationUtil.runReadAction(key.qualifiedNameElement.getIdentifierElement.isValid)
    } catch {
      case _: IllegalStateException => false
    }
  }

  private def checkValidLocation(definitionLocation: DefinitionLocation): Boolean = {
    ApplicationUtil.runReadAction(definitionLocation.namedElement.isValid)
  }

  private def checkValidName(key: Key, definitionLocation: DefinitionLocation): Boolean = {
    ApplicationUtil.runReadAction(Option(key.qualifiedNameElement.getIdentifierElement.getName)).contains(ApplicationUtil.runReadAction(definitionLocation.namedElement.getName))
  }

  def invalidate(psiFile: PsiFile): Unit = {
    val synchronousCache = Cache.synchronous()
    val keys = synchronousCache.asMap().flatMap { case (k, v) =>
      if (checkValidKey(k)) {
        v.toOption match {
          case Some(definitionLocation) if checkValidLocation(definitionLocation) & checkValidName(k, definitionLocation) => None
          case _ => Some(k)
        }
      } else {
        Some(k)
      }
    }
    synchronousCache.invalidateAll(keys)
  }

  def invalidateAll(project: Project): Unit = {
    val synchronousCache = Cache.synchronous()
    synchronousCache.asMap().filter(_._1.psiFile.getProject == project).keys.foreach(synchronousCache.invalidate)
  }

  private def findDefinitionLocationResult(key: Key): DefinitionLocationResult = {
    val psiFile = key.psiFile
    val project = psiFile.getProject
    val identifierElement = key.qualifiedNameElement.getIdentifierElement

    ProgressManager.checkCanceled()

    val name = ApplicationUtil.runReadAction(identifierElement.getName)
    if (name.headOption.exists(_.isUpper)) {
      createDefinitionLocationResult(project, key, name, withoutLastColumn = true)
    } else {
      createDefinitionLocationResult(project, key, name, withoutLastColumn = false)
    }
  }

  private def findLocationByImportedIdentifiers(project: Project, key: Key, name: String): Option[Either[NoInfoAvailable, PackageModuleLocation]] = {
    val psiFile = key.psiFile
    val qName1 = key.qualifiedNameElement.getName
    val mids = FileModuleIdentifiers.findAvailableModuleIdentifiers(psiFile)
    ProgressManager.checkCanceled()
    val qName2 = StringUtil.removeOuterParens(qName1)
    val qName = key.importQualifier match {
      case None => qName2
      case Some(q) => q + "." + qName2
    }
    for {
      mid <- mids.find(_.name == qName)
    } yield {
      HaskellReference.findIdentifiersByModuleAndName(project, Seq(mid.moduleName), name) match {
        case Right(nes) if nes.nonEmpty => nes.headOption.map(ne => Right(PackageModuleLocation(mid.moduleName, ne, name, Some(qName)))).getOrElse(Left(NoInfoAvailable(name, psiFile.getName)))
        case _ => Left(NoInfoAvailable(name, psiFile.getName))
      }
    }
  }

  private def createDefinitionLocationResult(project: Project, key: Key, name: String, withoutLastColumn: Boolean): DefinitionLocationResult = {
    val psiFile = key.psiFile
    ProgressManager.checkCanceled()
    val libraryFile = HaskellProjectUtil.isLibraryFile(psiFile)
    ProgressManager.checkCanceled()
    // Again workaround intero bug
    if (libraryFile || key.importQualifier.isDefined || key.qualifiedNameElement.getQualifierName.isDefined) {
      val locations = findLocationByImportedIdentifiers(project, key, name)

      ProgressManager.checkCanceled()

      locations match {
        case Some(ls) => ls
        case None => if (libraryFile) {
          ProgressManager.checkCanceled()
          HaskellComponentsManager.findNameInfo(key.qualifiedNameElement) match {
            case Right(infos) => infos.headOption match {
              case Some(info) =>
                ProgressManager.checkCanceled()
                HaskellReference.findIdentifiersByNameInfo(info, key.qualifiedNameElement.getIdentifierElement, project) match {
                  case Right(ne) =>
                    if (ne.isEmpty) {
                      // TODO Use findIdentifierInFileByName in HaskellReference
                      HaskellPsiUtil.findHaskellDeclarationElements(psiFile).toSeq.flatMap(_.getIdentifierElements).
                        find(_.getName == name).
                        map(e => Right(PackageModuleLocation("-", e, name))).getOrElse(Left(NoInfoAvailable(name, psiFile.getName)))
                      Left(NoInfoAvailable("", ""))
                    } else {
                      ne.headOption.map(ne => Right(PackageModuleLocation("-", ne, name))).getOrElse(Left(NoInfoAvailable(name, psiFile.getName)))
                    }
                  case Left(noInfo) => Left(noInfo)
                }
              case None => Left(NoInfoAvailable(name, psiFile.getName))
            }
            case Left(noInfo) => Left(noInfo)
          }
        } else {
          findLocationByImportedIdentifiers(project, key, name).getOrElse(Left(NoInfoAvailable(psiFile.getName, name)))
        }
      }
    } else {
      ProgressManager.checkCanceled()
      val moduleName = HaskellPsiUtil.findModuleName(psiFile)
      ProgressManager.checkCanceled()
      findLocationResultWithRepl(project, psiFile, moduleName, key, name, withoutLastColumn)
    }
  }

  private def findLocationResultWithRepl(project: Project, psiFile: PsiFile, moduleName: Option[String], key: Key, name: String, withoutLastColumn: Boolean): DefinitionLocationResult = {
    ProgressManager.checkCanceled()
    findLocationInfoWithRepl(project, psiFile, moduleName, key, name, withoutLastColumn) match {
      case Right(o) => o.stdoutLines.headOption.map(l => createDefinitionLocationResultFromLocationInfo(project, psiFile, l, key, name)) match {
        case Some(r) => r
        case None => Left(NoInfoAvailable(name, key.psiFile.getName))
      }
      case Left(noInfo) => Left(noInfo)
    }
  }

  private def findLocationInfoWithRepl(project: Project, psiFile: PsiFile, moduleName: Option[String], key: Key, name: String, withoutLastColumn: Boolean): Either[NoInfo, StackReplOutput] = {
    ProgressManager.checkCanceled()
    val qualifiedNameElement = key.qualifiedNameElement
    val findLocation = for {
      vf <- HaskellFileUtil.findVirtualFile(psiFile)
      sp <- LineColumnPosition.fromOffset(vf, qualifiedNameElement.getTextRange.getStartOffset)
      ep <- LineColumnPosition.fromOffset(vf, qualifiedNameElement.getTextRange.getEndOffset)
      endColumnNr = if (withoutLastColumn) ep.columnNr - 1 else ep.columnNr
    } yield {
      repl: ProjectStackRepl => repl.findLocationInfo(moduleName, psiFile, sp.lineNr, sp.columnNr, ep.lineNr, endColumnNr, name)
    }

    ProgressManager.checkCanceled()

    findLocation match {
      case None => Left(NoInfoAvailable(name, psiFile.getContainingFile.getName))
      case Some(f) =>
        StackReplsManager.getProjectRepl(psiFile) match {
          case Some(repl) =>
            if (repl.isBusy) {
              Left(ReplIsBusy)
            } else if (!repl.available) {
              Left(ReplNotAvailable)
            } else {
              if (ApplicationManager.getApplication.isDispatchThread && !LoadComponent.isModuleLoaded(moduleName, psiFile)) {
                Left(ModuleNotAvailable(moduleName.getOrElse(psiFile.getName)))
              } else {
                f(repl) match {
                  case Some(o) if o.stderrLines.isEmpty && o.stdoutLines.nonEmpty => Right(o)
                  case None => Left(ReplNotAvailable)
                  case _ => Left(NoInfoAvailable(name, psiFile.getName))
                }
              }
            }
          case None => Left(ReplNotAvailable)
        }
    }
  }

  private def createDefinitionLocationResultFromLocationInfo(project: Project, psiFile: PsiFile, output: String, key: Key, name: String): DefinitionLocationResult = {
    ProgressManager.checkCanceled()
    output match {
      case LocAtPattern(filePath, startLineNr, startColumnNr, _, _) =>
        // For some unknown reason performing this find file action by a ProgressIndicatorUtils.scheduleWithWriteActionPriority (according to the JB docs) blocks the UI
        val file = HaskellProjectUtil.findFile2(filePath, project)
        file match {
          case (Some(vf), Some(pf)) =>
            HaskellReference.findIdentifierByLocation(project, vf, pf, startLineNr.toInt, startColumnNr.toInt, name) match {
              case Some(e) => Right(LocalModuleLocation(pf, e, name))
              case None => Left(NoInfoAvailable(name, psiFile.getName))
            }
          case (_, _) => Left(NoInfoAvailable(name, psiFile.getName))
        }
      case PackageModulePattern(_, mn) =>
        findFilesByModuleName(project, mn) match {
          case Right(files) =>
            files.headOption.flatMap(HaskellReference.findIdentifierInFileByName(_, name)) match {
              case Some(e) => Right(PackageModuleLocation(mn, e, name))
              case None => Left(NoInfoAvailable(name, key.psiFile.getName))
            }
          case Left(noInfo) => Left(noInfo)
        }
      case _ => findLocationByImportedIdentifiers(project, key, name).getOrElse(Left(NoInfoAvailable(name, key.psiFile.getName)))
    }
  }

  private[component] def find(key: Key): DefinitionLocationResult = {
    val isDispatchThread = ApplicationManager.getApplication.isDispatchThread
    val isReadAccessAllowed = ApplicationManager.getApplication.isReadAccessAllowed

    val psiFile = key.psiFile
    val qualifiedNameElement = key.qualifiedNameElement

    def wait(f: => Future[DefinitionLocationResult]): DefinitionLocationResult = {

      new WaitFor(ApplicationUtil.timeout, 1) {
        override def condition(): Boolean = {
          ProgressManager.checkCanceled()
          f.isCompleted
        }
      }

      if (f.isCompleted) {
        Await.result(f, 1.milli)
      } else {
        if (isDispatchThread) {
          HaskellNotificationGroup.logInfoEvent(psiFile.getProject, s"No info in DefinitionLocationComponent.find for ${qualifiedNameElement.getName} in ${psiFile.getName} because timeout in dispatch thread")
          Left(ModuleNotAvailable(psiFile.getName))
        }
        else {
          HaskellNotificationGroup.logInfoEvent(psiFile.getProject, s"Timeout in DefinitionLocationComponent.find for ${qualifiedNameElement.getName} in ${psiFile.getName}")
          Left(ReplNotAvailable)
        }
      }
    }

    ProgressManager.checkCanceled()

    Cache.getIfPresent(key) match {
      case Some(v) =>
        val result = wait(v)
        result match {
          case Right(_) => result
          case Left(ReplIsBusy) | Left(ReadActionTimeout(_)) | Left(IndexNotReady) | Left(ModuleNotAvailable(_)) | Left(ReplNotAvailable) =>
            Cache.synchronous().invalidate(key)
            result
          case _ => result
        }
      case None =>
        if (isReadAccessAllowed) {
          val result = findDefinitionLocationResult(key)
          result match {
            case Right(_) =>
              Cache.synchronous.put(key, result)
              result
            case Left(ReplIsBusy) | Left(ReadActionTimeout(_)) | Left(IndexNotReady) | Left(ReplNotAvailable) | Left(ModuleNotAvailable(_)) =>
              result
            case Left(_) =>
              Cache.synchronous.put(key, result)
              result
          }
        } else {
          throw new IllegalStateException("Has to be in read state")
        }
    }
  }

}

sealed trait DefinitionLocation {
  def namedElement: HaskellNamedElement

  def originalName: String

  def originalQualifiedName: Option[String]
}

case class PackageModuleLocation(moduleName: String, namedElement: HaskellNamedElement, originalName: String, originalQualifiedName: Option[String] = None) extends DefinitionLocation

case class LocalModuleLocation(psiFile: PsiFile, namedElement: HaskellNamedElement, originalName: String, originalQualifiedName: Option[String] = None) extends DefinitionLocation

