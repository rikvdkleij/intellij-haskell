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
import intellij.haskell.editor.FileModuleIdentifiers
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

  private final val Cache: AsyncLoadingCache[Key, DefinitionLocationResult] = Scaffeine().buildAsync((k: Key) => findDefinitionLocationResult(k))

  def findDefinitionLocation(psiFile: PsiFile, qualifiedNameElement: HaskellQualifiedNameElement, importQualifier: Option[String]): DefinitionLocationResult = {
    val key = Key(psiFile, qualifiedNameElement, importQualifier)
    find(key)
  }

  def findDefinitionLocationInCache(psiFile: PsiFile, qualifiedNameElement: HaskellQualifiedNameElement): Option[DefinitionLocation] = {
    Cache.synchronous().asMap().find { case (k, _) => k.psiFile == psiFile && k.qualifiedNameElement == qualifiedNameElement }.flatMap(_._2.toOption)
  }

  def findReferencesInCache(targetFile: PsiFile): Seq[(PsiFile, HaskellQualifiedNameElement)] = {
    val synchronousCache = Cache.synchronous()
    synchronousCache.asMap().filter { case (k, v) => v.toOption.exists(l => {
      if (!l.namedElement.isValid) {
        synchronousCache.invalidate(k)
        false
      } else {
        l.namedElement.getContainingFile.getOriginalFile == targetFile
      }
    })
    }.keys.map(k => (k.psiFile, k.qualifiedNameElement)).toSeq
  }

  def invalidate(elements: Seq[HaskellQualifiedNameElement]): Unit = {
    val synchronousCache = Cache.synchronous()
    val keys = synchronousCache.asMap().keys.filter(k => elements.contains(k.qualifiedNameElement))
    synchronousCache.invalidateAll(keys)
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
    ApplicationUtil.runReadAction(key.qualifiedNameElement.getIdentifierElement.getName) == definitionLocation.originalName &&
      ApplicationUtil.runReadAction(definitionLocation.namedElement.getName) == definitionLocation.originalName &&
      definitionLocation.originalQualifiedName.exists(qn => ApplicationUtil.runReadAction(key.qualifiedNameElement.getName == qn))
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

    val name = identifierElement.getName
    if (name.headOption.exists(_.isUpper)) {
      createDefinitionLocationResult(project, psiFile, key, name, withoutLastColumn = true)
    } else {
      createDefinitionLocationResult(project, psiFile, key, name, withoutLastColumn = false)
    }
  }

  private def createDefinitionLocationResult(project: Project, psiFile: PsiFile, key: Key, name: String, withoutLastColumn: Boolean): DefinitionLocationResult = {
    ProgressManager.checkCanceled()
    val sourceFile = HaskellProjectUtil.isSourceFile(psiFile)
    ProgressManager.checkCanceled()
    val moduleName = HaskellPsiUtil.findModuleName(psiFile)
    ProgressManager.checkCanceled()
    if (sourceFile) {
      // Again workaround intero bug
      if (key.importQualifier.isDefined || key.qualifiedNameElement.getQualifierName.isDefined) {
        val qName1 = key.qualifiedNameElement.getName
        val result = FileModuleIdentifiers.getModuleIdentifiers(psiFile) match {
          case None => Some(Left(ReadActionTimeout("")))
          case Some(mids) =>
            ProgressManager.checkCanceled()
            val qName = key.importQualifier match {
              case None => qName1
              case Some(q) => q + "." + qName1
            }
            for {
              mid <- mids.find(_.name == qName)
            } yield {
              val findResult = HaskellReference.findIdentifiersByModuleAndName(project, mid.moduleName, name)
              findResult match {
                case Right(nes) if nes.nonEmpty => nes.headOption.map(ne => Right(PackageModuleLocation(mid.moduleName, ne, name, Some(qName)))).getOrElse(Left(NoInfoAvailable(name, psiFile.getName)))
                case _ => Left(NoInfoAvailable(name, psiFile.getName))
              }
            }
        }

        ProgressManager.checkCanceled()

        val result2 = result match {
          case Some(r@Right(_)) => r
          case None => Left(NoInfoAvailable(name, psiFile.getName)) // identifier not imported
          case Some(Left(_)) =>
            ProgressManager.checkCanceled()
            HaskellComponentsManager.findNameInfo(key.qualifiedNameElement, key.importQualifier) match {
              case Right(infos) =>
                ProgressManager.checkCanceled()
                infos.headOption match {
                  case Some(nameInfo) =>
                    val findInfoResult = HaskellReference.findIdentifiersByNameInfo(nameInfo, key.qualifiedNameElement.getIdentifierElement, project)
                    ProgressManager.checkCanceled()
                    findInfoResult match {
                      case Right(nes) =>
                        nes.headOption match {
                          case Some(r) => Right(PackageModuleLocation(HaskellPsiUtil.findModuleName(r.getContainingFile).get, r, name, Some(qName1)))
                          case None => Left(NoInfoAvailable(name, psiFile.getName))
                        }
                      case Left(noInfo) => Left(noInfo)
                    }
                  case None => Left(NoInfoAvailable(name, psiFile.getName))
                }
              case Left(noInfo) => Left(noInfo)
            }
        }

        result2 match {
          case Right(location) => Right(location)
          case l =>
            HaskellNotificationGroup.logInfoEvent(project, "In createDefinitionLocationResult no result for " + name)
            l
        }
      } else {
        ProgressManager.checkCanceled()
        findLocationResultWithRepl(project, psiFile, moduleName, key, name, withoutLastColumn)
      }
    } else {
      ProgressManager.checkCanceled()
      HaskellComponentsManager.findNameInfo(key.qualifiedNameElement) match {
        case Right(infos) => infos.headOption match {
          case Some(info) =>
            ProgressManager.checkCanceled()
            val ne = HaskellReference.findIdentifiersByNameInfo(info, key.qualifiedNameElement.getIdentifierElement, project)
            ne match {
              case Right(nee) =>
                if (nee.isEmpty) {
                  HaskellPsiUtil.findHaskellDeclarationElements(psiFile).toSeq.flatMap(_.getIdentifierElements).
                    find(e => e.getName == name).
                    map(e => Right(PackageModuleLocation("-", e, name))).getOrElse(Left(NoInfoAvailable(name, psiFile.getName)))
                  Left(NoInfoAvailable("", ""))
                } else {
                  nee.headOption.map(e => Right(PackageModuleLocation("-", e, name))).getOrElse(Left(NoInfoAvailable(name, psiFile.getName)))
                }
              case Left(noInfo) => Left(noInfo)
            }
          case None => Left(NoInfoAvailable(name, psiFile.getName))
        }
        case Left(noInfo) => Left(noInfo)
      }
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
              f(repl) match {
                case Some(o) if o.stderrLines.isEmpty && o.stdoutLines.nonEmpty => Right(o)
                case None => Left(ReplNotAvailable)
                case _ => Left(NoInfoAvailable(name, psiFile.getName))
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
        findFileByModuleName(project, mn) match {
          case Right(files) =>
            files.headOption.flatMap(HaskellReference.findIdentifierInFileByName(_, name)) match {
              case Some(e) => Right(PackageModuleLocation(mn, e, name))
              case None => Left(NoInfoAvailable(name, key.psiFile.getName))
            }
          case Left(noInfo) => Left(noInfo)
        }
      case _ => Left(NoInfoAvailable(name, key.psiFile.getName))
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
          Left(ModuleNotLoaded(psiFile.getName))
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
          case Left(ReplIsBusy) | Left(ReadActionTimeout(_)) | Left(IndexNotReady) | Left(ModuleNotLoaded(_)) | Left(ReplNotAvailable) =>
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
            case Left(ReplIsBusy) | Left(ReadActionTimeout(_)) | Left(IndexNotReady) | Left(ReplNotAvailable) | Left(ModuleNotLoaded(_)) =>
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

