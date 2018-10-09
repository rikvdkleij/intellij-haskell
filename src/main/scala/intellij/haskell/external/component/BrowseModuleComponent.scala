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
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import intellij.haskell.external.repl._
import intellij.haskell.util.StringUtil

import scala.concurrent.{ExecutionContext, Future}

private[component] object BrowseModuleComponent {

  private case class Key(project: Project, moduleName: String, psiFile: Option[PsiFile], exported: Boolean)

  type BrowseModuleResult = Iterable[ModuleIdentifier]
  private type BrowseModuleInternalResult = Either[NoInfo, Iterable[ModuleIdentifier]]

  private final val Cache: AsyncLoadingCache[Key, BrowseModuleInternalResult] = Scaffeine().buildAsync((k: Key) => {
    if (k.project.isDisposed) {
      Left(NoInfoAvailable(k.moduleName, "-"))
    } else {
      findModuleIdentifiers(k)
    }
  })

  private def matchResult(key: Key, result: Future[BrowseModuleInternalResult])(implicit ec: ExecutionContext): Future[Iterable[ModuleIdentifier]] = {
    concurrent.blocking(result.map {
      case Right(ids) => ids
      case Left(NoInfoAvailable(_, _)) =>
        Iterable()
      case Left(ReplNotAvailable) | Left(ReplIsBusy) | Left(IndexNotReady) | Left(ModuleNotLoaded(_)) | Left(ReadActionTimeout(_)) =>
        Cache.synchronous().invalidate(key)
        Iterable()
    })
  }

  def findLibraryModuleIdentifiers(project: Project, moduleName: String)(implicit ec: ExecutionContext): Future[Iterable[ModuleIdentifier]] = {
    val key = Key(project, moduleName, None, exported = true)
    matchResult(key, Cache.get(key))
  }

  def findTopLevelIdentifiers(psiFile: PsiFile, moduleName: String)(implicit ec: ExecutionContext): Future[Iterable[ModuleIdentifier]] = {
    val key = Key(psiFile.getProject, moduleName, Some(psiFile), exported = false)

    if (LoadComponent.isFileLoaded(psiFile)) {
      matchResult(key, Cache.get(key))
    } else {
      matchResult(key, Cache.getIfPresent(key).getOrElse(Future.successful(Left(ModuleNotLoaded(key.moduleName)))))
    }
  }

  def findExportedIdentifiers(stackComponentGlobalInfo: StackComponentGlobalInfo, psiFile: PsiFile, moduleName: String)(implicit ec: ExecutionContext): Future[Iterable[ModuleIdentifier]] = {
    if (stackComponentGlobalInfo.libraryModuleNames.exists(_.exposed.toSeq.contains(moduleName))) {
      findLibraryModuleIdentifiers(psiFile.getProject, moduleName)
    } else {
      val key = Key(psiFile.getProject, moduleName, Some(psiFile), exported = true)
      matchResult(key, Cache.get(key))
    }
  }

  def findModuleNamesInCache(project: Project): Iterable[String] = {
    Cache.synchronous().asMap().filter(_._1.project == project).map(_._1.moduleName)
  }

  def refreshTopLevel(project: Project, moduleName: String, psiFile: PsiFile): Unit = {
    val key = Key(project, moduleName, Some(psiFile), exported = false)
    Cache.synchronous().refresh(key)
  }

  def invalidateTopLevel(project: Project, moduleName: String, psiFile: PsiFile): Unit = {
    val keys = Cache.synchronous().asMap().filter(k => k._1.project == project && k._1.moduleName == moduleName && k._1.psiFile.contains(psiFile) && !k._1.exported).keys
    Cache.synchronous().invalidateAll(keys)
  }

  def invalidateForModuleName(project: Project, moduleName: String): Unit = {
    val keys = Cache.synchronous.asMap().keys.filter(_.moduleName == moduleName)
    Cache.synchronous.invalidateAll(keys)
  }

  def invalidateForFile(psiFile: PsiFile): Unit = {
    val keys = Cache.synchronous.asMap().keys.filter(_.psiFile.contains(psiFile))
    Cache.synchronous.invalidateAll(keys)
  }

  def invalidate(project: Project): Unit = {
    val keys = Cache.synchronous().asMap().keys.filter(_.project == project)
    Cache.synchronous.invalidateAll(keys)
  }

  private def findModuleIdentifiers(key: Key): BrowseModuleInternalResult = {
    val project = key.project
    val moduleName = key.moduleName

    key.psiFile match {
      case Some(psiFile) if !key.exported =>
        if (LoadComponent.isFileLoaded(psiFile)) {
          StackReplsManager.getProjectRepl(psiFile) match {
            case Some(repl) =>
              if (repl.isBusy) {
                Left(ReplIsBusy)
              } else {
                repl.getLocalModuleIdentifiers(moduleName, psiFile) match {
                  case Some(output) if output.stderrLines.isEmpty => Right(output.stdoutLines.takeWhile(l => !l.startsWith("-- imported via")).flatMap(l => findModuleIdentifiers(project, l, moduleName)))
                  case _ => Left(ReplNotAvailable)
                }
              }
            case _ => Left(ReplNotAvailable)
          }
        } else {
          Left(ModuleNotLoaded(key.moduleName))
        }
      case Some(psiFile) if key.exported =>
        StackReplsManager.getProjectRepl(psiFile) match {
          case Some(repl) =>
            if (repl.isBusy) {
              Left(ReplIsBusy)
            } else {
              repl.getModuleIdentifiers(moduleName, psiFile) match {
                case Some(output) if output.stderrLines.isEmpty => repl.getModuleIdentifiers(moduleName, psiFile).map(_.stdoutLines.flatMap(l => findModuleIdentifiers(project, l, moduleName))) match {
                  case Some(ids) => Right(ids)
                  case None => Left(NoInfoAvailable(key.moduleName, key.psiFile.map(_.getName).getOrElse("-")))
                }
                case _ => Left(ReplNotAvailable)
              }
            }
          case None => Left(ReplIsBusy)
        }
      case None => findLibModuleIdentifiers(project, moduleName)
    }
  }

  private def findLibModuleIdentifiers(project: Project, moduleName: String): Either[NoInfo, Seq[ModuleIdentifier]] = {
    StackReplsManager.getGlobalRepl(project) match {
      case Some(repl) => if (repl.isBusy) {
        Left(ReplIsBusy)
      } else {
        repl.getModuleIdentifiers(moduleName) match {
          case None => Left(ReplNotAvailable)
          case Some(o) if o.stdoutLines.nonEmpty => Right(o.stdoutLines.flatMap(l => findModuleIdentifiers(project, l, moduleName).toSeq))
          case _ => Left(NoInfoAvailable(moduleName, "-"))
        }
      }
      case None => Left(ReplNotAvailable)
    }
  }

  // This kind of declarations are returned in case DuplicateRecordFields are enabled
  private final val Module$SelPattern =
    """([\w\.\-]+)\.\$sel:(.+)""".r

  private def findModuleIdentifiers(project: Project, declarationLine: String, moduleName: String): Option[ModuleIdentifier] = {
    declarationLine match {
      case Module$SelPattern(mn, declaration) => DeclarationLineUtil.findName(declaration).map(nd => createModuleIdentifier(nd.name, mn, nd.declaration))
      case _ => DeclarationLineUtil.findName(declarationLine) map (nd => createModuleIdentifier(nd.name, moduleName, nd.declaration))
    }
  }

  private def createModuleIdentifier(name: String, moduleName: String, declaration: String) = {
    ModuleIdentifier(StringUtil.removeOuterParens(name), moduleName, declaration, isOperator = DeclarationLineUtil.isWithinParens(name))
  }
}

case class ModuleIdentifier(name: String, moduleName: String, declaration: String, isOperator: Boolean)
