/*
 * Copyright 2014-2020 Rik van der Kleij
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
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import intellij.haskell.external.repl._
import intellij.haskell.util.index.HaskellModuleNameIndex
import intellij.haskell.util.{HaskellFileUtil, HaskellProjectUtil, StringUtil}

import scala.concurrent.{ExecutionContext, Future}

private[component] object BrowseModuleComponent {

  private case class Key(project: Project, moduleName: String)

  private type BrowseModuleInternalResult = Either[NoInfo, Iterable[ModuleIdentifier]]

  private final val Cache: AsyncLoadingCache[Key, BrowseModuleInternalResult] = Scaffeine().buildAsync((k: Key) => {
    if (k.project.isDisposed) {
      Left(NoInfoAvailable(k.moduleName, "-"))
    } else {
      findModuleIdentifiers(k)
    }
  })

  def findModuleIdentifiers(project: Project, moduleName: String)(implicit ec: ExecutionContext): Future[Option[Iterable[ModuleIdentifier]]] = {
    val key = Key(project, moduleName)
    val result = Cache.get(key)
    result.map {
      case Right(ids) => Some(ids)
      case Left(NoInfoAvailable(_, _)) | Left(NoMatchingExport) =>
        None
      case Left(ReplNotAvailable) | Left(IndexNotReady) | Left(ModuleNotAvailable(_)) | Left(ReadActionTimeout(_)) =>
        Cache.synchronous().invalidate(key)
        None
    }
  }

  def findModuleIdentifiersSync(project: Project, moduleName: String): BrowseModuleInternalResult = {
    val key = Key(project, moduleName)
    Cache.synchronous().get(key)
  }

  def findModuleIdentifiersInCache(project: Project): Iterable[ModuleIdentifier] = {
    Cache.synchronous().asMap().filter(_._1.project == project).values.flatMap(_.toSeq).flatten
  }

  def invalidateModuleNames(project: Project, moduleNames: Seq[String]): Unit = {
    val synchronousCache = Cache.synchronous
    val keys = synchronousCache.asMap().keys.filter(k => k.project == project && moduleNames.contains(k.moduleName))
    keys.foreach(synchronousCache.invalidate)
  }

  def invalidate(project: Project): Unit = {
    val synchronousCache = Cache.synchronous
    val keys = synchronousCache.asMap().keys.filter(_.project == project)
    synchronousCache.invalidateAll(keys)
  }

  private def findModuleIdentifiers(key: Key): BrowseModuleInternalResult = {
    val project = key.project
    val moduleName = key.moduleName

    if (AvailableModuleNamesComponent.isProjectModule(project, moduleName)) {
      HaskellModuleNameIndex.findFilesByModuleName2(project, moduleName) match {
        case Right(files) => files.headOption match {
          case Some((moduleFile, isProjectFile)) =>
            if (isProjectFile) {
              getCurrentFile(project) match {
                case Some(cf) => findInRepl(project, StackReplsManager.getProjectRepl(cf), moduleName, None) match {
                  case r@Right(_) => r
                  case Left(_) =>
                    val projectRepl = StackReplsManager.getProjectRepl(moduleFile)
                    findInRepl(project, projectRepl, moduleName, Some(moduleFile))
                }
                case None =>
                  val projectRepl = StackReplsManager.getProjectRepl(moduleFile)
                  findInRepl(project, projectRepl, moduleName, Some(moduleFile))
              }
            } else {
              Left(NoInfoAvailable(moduleName, "-"))
            }
          case None =>
            Left(NoInfoAvailable(moduleName, "-"))
        }
        case Left(noInfo) => Left(noInfo)
      }
    } else {
      LibraryPackageInfoComponent.findLibraryModuleName(moduleName) match {
        case Some(true) => findLibraryModuleIdentifiers(project, moduleName)
        case _ => Left(NoInfoAvailable(moduleName, "-"))
      }
    }
  }

  private def getCurrentFile(project: Project): Option[PsiFile] = {
    if (StackProjectManager.isInitializing(project)) {
      None
    } else {
      FileEditorManager.getInstance(project).getSelectedFiles.headOption match {
        case Some(f) => HaskellFileUtil.convertToHaskellFileInReadAction(project, f).toOption
        case None => None
      }
    }
  }

  private def findInRepl(project: Project, projectRepl: Option[ProjectStackRepl], moduleName: String, psiFile: Option[PsiFile]): Either[NoInfo, Seq[ModuleIdentifier]] = {
    projectRepl match {
      case Some(repl) =>
        if (repl.available) {
          repl.getModuleIdentifiers(project, moduleName, psiFile) match {
            case Some(output) if output.stderrLines.isEmpty && output.stdoutLines.nonEmpty => Right(output.stdoutLines.flatMap(l => createProjectModuleIdentifier(project, l, moduleName)))
            case None => Left(ReplNotAvailable)
            case _ => Left(ModuleNotAvailable(moduleName))
          }
        } else {
          Left(ReplNotAvailable)
        }
      case None => Left(ReplNotAvailable)
    }
  }

  private def findLibraryModuleIdentifiers(project: Project, moduleName: String): Either[NoInfo, Seq[ModuleIdentifier]] = {
    StackReplsManager.getGlobalRepl(project) match {
      case Some(repl) =>
        if (repl.available) {
          repl.getModuleIdentifiers(moduleName) match {
            case Some(o) if o.stderrLines.isEmpty && o.stdoutLines.nonEmpty => Right(o.stdoutLines.flatMap(l => createLibraryModuleIdentifier(project, l, moduleName)))
            case None => Left(ReplNotAvailable)
            case _ => Left(ModuleNotAvailable(moduleName))
          }
        } else {
          Left(ReplNotAvailable)
        }
      case None => Left(ReplNotAvailable)
    }
  }

  // This kind of declarations are returned in case DuplicateRecordFields are enabled
  private final val Module$SelPattern = """([\w.\-]+)\.\$sel:([^:]+)(?::[^:]+)?::(.*)""".r

  def createLibraryModuleIdentifier(project: Project, declarationLine: String, moduleName: String): Option[ModuleIdentifier] = {
    DeclarationUtil.getDeclarationInfo(declarationLine, containsQualifiedIds = true).map(declarationInfo => {
      val id = declarationInfo.id
      if (moduleName == HaskellProjectUtil.Prelude) {
        val baseModuleName = declarationInfo.qualifiedId.flatMap(id => {
          if (id.contains(".") && id != ".") {
            Some(id.split('.').init.mkString("."))
          } else {
            None
          }
        })
        ModuleIdentifier(id, moduleName, declarationInfo.declarationLine, declarationInfo.operator, baseModuleName)
      } else {
        ModuleIdentifier(id, moduleName, declarationInfo.declarationLine, declarationInfo.operator)
      }
    })
  }

  private def createProjectModuleIdentifier(project: Project, declarationLine: String, moduleName: String): Option[ModuleIdentifier] = {
    declarationLine match {
      case Module$SelPattern(mn, id, fieldType) => Some(ModuleIdentifier(id, mn, s"$id :: $fieldType", StringUtil.isWithinParens(id)))
      case _ => DeclarationUtil.getDeclarationInfo(declarationLine, containsQualifiedIds = true).
        map(declarationInfo => ModuleIdentifier(declarationInfo.id, moduleName, declarationInfo.declarationLine, declarationInfo.operator))
    }
  }
}

/**
  * @param name                  is without (operator) parentheses.
  * @param preludeBaseModuleName is module name of the Prelude identifier where it's defined in base package.
  */
case class ModuleIdentifier(name: String, moduleName: String, declaration: String, operator: Boolean, preludeBaseModuleName: Option[String] = None)
