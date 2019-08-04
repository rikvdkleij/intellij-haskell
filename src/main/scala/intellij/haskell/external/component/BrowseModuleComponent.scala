/*
 * Copyright 2014-2019 Rik van der Kleij
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
import intellij.haskell.util.{HaskellFileUtil, StringUtil}

import scala.concurrent.{ExecutionContext, Future, blocking}

private[component] object BrowseModuleComponent {

  private case class Key(project: Project, moduleName: String)

  type BrowseModuleResult = Iterable[ModuleIdentifier]
  private type BrowseModuleInternalResult = Either[NoInfo, Iterable[ModuleIdentifier]]

  private final val Cache: AsyncLoadingCache[Key, BrowseModuleInternalResult] = Scaffeine().buildAsync((k: Key) => {
    if (k.project.isDisposed) {
      Left(NoInfoAvailable(k.moduleName, "-"))
    } else {
      findModuleIdentifiers(k)
    }
  })

  private def matchResult(key: Key, result: Future[BrowseModuleInternalResult])(implicit ec: ExecutionContext): Future[Option[Iterable[ModuleIdentifier]]] = {
    blocking(result.map {
      case Right(ids) => Some(ids)
      case Left(NoInfoAvailable(_, _)) | Left(NoMatchingExport) =>
        None
      case Left(ReplNotAvailable) | Left(IndexNotReady) | Left(ModuleNotAvailable(_)) | Left(ReadActionTimeout(_)) =>
        Cache.synchronous().invalidate(key)
        None
    })
  }

  def findModuleIdentifiers(project: Project, moduleName: String)(implicit ec: ExecutionContext): Future[Option[Iterable[ModuleIdentifier]]] = {
    val key = Key(project, moduleName)
    matchResult(key, Cache.get(key))
  }

  def findModuleIdentifiersSync(project: Project, moduleName: String): BrowseModuleInternalResult = {
    val key = Key(project, moduleName)
    Cache.synchronous().get(key)
  }

  def findModuleIdentifiersInCache(project: Project): Iterable[ModuleIdentifier] = {
    Cache.synchronous().asMap().filter(_._1.project == project).values.flatMap(_.toSeq).flatten
  }

  def invalidateModuleName(project: Project, moduleName: String): Unit = {
    val synchronousCache = Cache.synchronous
    val key = synchronousCache.asMap().keys.find(k => k.project == project && k.moduleName == moduleName)
    key.foreach(synchronousCache.invalidate)
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
            findLibraryModuleIdentifiers(project, moduleName)
          }
        case None =>
          // E.g. module name is Prelude which does not refer to file
          findLibraryModuleIdentifiers(project, moduleName)
      }
      case Left(noInfo) => Left(noInfo)
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
        if (!repl.available) {
          Left(ReplNotAvailable)
        } else {
          repl.getModuleIdentifiers(moduleName, psiFile) match {
            case Some(output) if output.stderrLines.isEmpty && output.stdoutLines.nonEmpty => Right(output.stdoutLines.flatMap(l => findModuleIdentifiers(project, l, moduleName)))
            case _ => Left(ModuleNotAvailable(moduleName))
          }
        }
      case None => Left(ReplNotAvailable)
    }
  }

  private def findLibraryModuleIdentifiers(project: Project, moduleName: String): Either[NoInfo, Seq[ModuleIdentifier]] = {
    StackReplsManager.getGlobalRepl(project) match {
      case Some(repl) =>
        if (!repl.available) {
          Left(ReplNotAvailable)
        } else {
          repl.getModuleIdentifiers(moduleName) match {
            case Some(o) if o.stderrLines.isEmpty && o.stdoutLines.nonEmpty => Right(o.stdoutLines.flatMap(l => findModuleIdentifiers(project, l, moduleName).toSeq))
            case _ => Left(ModuleNotAvailable(moduleName))
          }
        }
      case None => Left(ReplNotAvailable)
    }
  }

  // This kind of declarations are returned in case DuplicateRecordFields are enabled
  private final val Module$SelPattern =
    """([\w\.\-]+)\.\$sel:([^:]+)(?::[^:]+)?::(.*)""".r

  private def findModuleIdentifiers(project: Project, declarationLine: String, moduleName: String): Option[ModuleIdentifier] = {
    declarationLine match {
      case Module$SelPattern(mn, name, fieldType) => Some(createModuleIdentifier(name, mn, s"$name :: $fieldType"))
      case _ => DeclarationLineUtil.findName(declarationLine) map (nd => createModuleIdentifier(nd.name, moduleName, nd.declaration))
    }
  }

  private def createModuleIdentifier(name: String, moduleName: String, declaration: String) = {
    ModuleIdentifier(StringUtil.removeOuterParens(name), moduleName, declaration, isOperator = StringUtil.isWithinParens(name))
  }
}

// value of name is without (operator) parens
case class ModuleIdentifier(name: String, moduleName: String, declaration: String, isOperator: Boolean)
