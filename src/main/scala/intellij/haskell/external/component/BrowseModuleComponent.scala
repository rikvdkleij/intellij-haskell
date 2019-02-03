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
import intellij.haskell.util.index.HaskellModuleNameIndex
import intellij.haskell.util.{HaskellProjectUtil, ScalaFutureUtil, StringUtil}

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

  private def matchResult(key: Key, result: Future[BrowseModuleInternalResult])(implicit ec: ExecutionContext): Future[Option[Iterable[ModuleIdentifier]]] = {
    concurrent.blocking(result.map {
      case Right(ids) => Some(ids)
      case Left(NoInfoAvailable(_, _)) =>
        None
      case Left(ReplNotAvailable) | Left(ReplIsBusy) | Left(IndexNotReady) | Left(ModuleNotLoaded(_)) | Left(ReadActionTimeout(_)) =>
        Cache.synchronous().invalidate(key)
        None
    })
  }

  def findLibraryModuleIdentifiers(project: Project, moduleName: String)(implicit ec: ExecutionContext): Future[Option[Iterable[ModuleIdentifier]]] = {
    val key = Key(project, moduleName, None, exported = true)
    matchResult(key, Cache.get(key))
  }

  def findLibraryModuleIdentifiersSync(project: Project, moduleName: String): BrowseModuleInternalResult = {
    val key = Key(project, moduleName, None, exported = true)
    Cache.synchronous().get(key)
  }

  def loadExportedIdentifiersSync(project: Project, psiFile: PsiFile, moduleName: String): Unit = {
    import scala.concurrent.ExecutionContext.Implicits.global
    val result = findExportedIdentifiers(psiFile, moduleName)
    ScalaFutureUtil.waitForValue(project, result, "findExportedIdentifiersSync")
  }

  def findTopLevelIdentifiers(psiFile: PsiFile, moduleName: String)(implicit ec: ExecutionContext): Future[Option[Iterable[ModuleIdentifier]]] = {
    val key = Key(psiFile.getProject, moduleName, Some(psiFile), exported = false)

    if (LoadComponent.isFileLoaded(psiFile)) {
      matchResult(key, Cache.get(key))
    } else {
      matchResult(key, Cache.getIfPresent(key).getOrElse(Future.successful(Left(ModuleNotLoaded(key.moduleName)))))
    }
  }

  def findExportedIdentifiers(psiFile: PsiFile, moduleName: String)(implicit ec: ExecutionContext): Future[Option[Iterable[ModuleIdentifier]]] = {
    val moduleFiles = HaskellModuleNameIndex.findFileByModuleName(psiFile.getProject, moduleName)
    val projectFile = moduleFiles.toOption.exists(_.headOption.exists(HaskellProjectUtil.isSourceFile))
    val project = psiFile.getProject

    if (projectFile) {
      val key = Key(project, moduleName, Some(psiFile), exported = true)
      val result = matchResult(key, Cache.get(key))
      result.map {
        case r@Some(_) => r
        case None =>
          val result = Cache.synchronous().asMap().find { case (k, _) => k.project == project && k.moduleName == moduleName }.map(_._2).flatMap(_.toOption)
          result.foreach(r => Cache.synchronous().put(key, Right(r)))
          result
      }
    } else {
      findLibraryModuleIdentifiers(psiFile.getProject, moduleName)
    }
  }

  def findModuleIdentifiersInCache(project: Project): Iterable[ModuleIdentifier] = {
    Cache.synchronous().asMap().filter(_._1.project == project).values.flatMap(_.toSeq).flatten
  }

  def refreshTopLevel(project: Project, moduleName: String, psiFile: PsiFile): Unit = {
    val key = Key(project, moduleName, Some(psiFile), exported = false)
    Cache.synchronous().refresh(key)
  }

  def invalidateTopLevel(project: Project, moduleName: String, psiFile: PsiFile): Unit = {
    val key = Key(project, moduleName, Some(psiFile), exported = false)
    Cache.synchronous().invalidate(key)
  }

  def invalidateExportedModuleName(project: Project, moduleName: String): Unit = {
    val synchronousCache = Cache.synchronous
    val key = synchronousCache.asMap().keys.filter(k => k.moduleName == moduleName && k.exported) // Can be more than one for a module name if file of module can not be found
    key.foreach(synchronousCache.invalidate)
  }

  def refreshExportedModuleNames(project: Project, moduleNames: Seq[String]): Unit = {
    val synchronousCache = Cache.synchronous
    val keys = synchronousCache.asMap().keys.filter(k => moduleNames.contains(k.moduleName))
    keys.foreach(synchronousCache.refresh)
  }

  def invalidate(project: Project): Unit = {
    val synchronousCache = Cache.synchronous
    val keys = synchronousCache.asMap().keys.filter(_.project == project)
    synchronousCache.invalidateAll(keys)
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
              } else if (!repl.available) {
                Left(ReplNotAvailable)
              } else {
                repl.getLocalModuleIdentifiers(moduleName, psiFile) match {
                  case Some(output) if output.stderrLines.isEmpty && output.stdoutLines.nonEmpty => Right(output.stdoutLines.takeWhile(l => !l.startsWith("-- imported via")).flatMap(l => findModuleIdentifiers(project, l, moduleName)))
                  case _ => Left(ReplNotAvailable)
                }
              }
            case _ => Left(ReplNotAvailable)
          }
        } else {
          Left(ModuleNotLoaded(key.moduleName))
        }
      case Some(psiFile) if key.exported =>
        val projectRepl = StackReplsManager.getProjectRepl(psiFile)
        projectRepl match {
          case Some(repl) =>
            if (repl.isBusy) {
              findInOtherRepl(project, psiFile, moduleName, repl) match {
                case r@Right(_) => r
                case _ => Left(ReplIsBusy)
              }
            } else if (!repl.available) {
              findInOtherRepl(project, psiFile, moduleName, repl) match {
                case r@Right(_) => r
                case _ => Left(ReplNotAvailable)
              }
            } else {
              if (repl.isBrowseModuleLoaded(moduleName)) {
                repl.getModuleIdentifiers(moduleName, psiFile) match {
                  case Some(output) if output.stderrLines.isEmpty && output.stdoutLines.nonEmpty => Right(output.stdoutLines.flatMap(l => findModuleIdentifiers(project, l, moduleName)))
                  case _ => Left(ReplNotAvailable)
                }
              } else {
                findInOtherRepl(project, psiFile, moduleName, repl)
              }
            }
          case None => Left(ReplNotAvailable)
        }
      case None => findLibModuleIdentifiers(project, moduleName)
    }
  }

  private def findInOtherRepl(project: Project, psiFile: PsiFile, moduleName: String, repl: ProjectStackRepl) = {
    HaskellModuleNameIndex.findFileByModuleName(project, moduleName) match {
      case Right(files) => files.headOption match {
        case Some(f) => val otherRepl = StackReplsManager.getProjectRepl(f)
          if (otherRepl.contains(repl)) {
            Left(ReplNotAvailable)
          } else {
            otherRepl match {
              case Some(oRepl) =>
                if (oRepl.isBusy) {
                  Left(ReplIsBusy)
                } else if (!oRepl.available) {
                  Left(ReplNotAvailable)
                } else {
                  oRepl.getModuleIdentifiers(moduleName, f) match {
                    case Some(output) if output.stderrLines.isEmpty && output.stdoutLines.nonEmpty => Right(output.stdoutLines.flatMap(l => findModuleIdentifiers(project, l, moduleName)))
                    case _ => Left(ReplNotAvailable)
                  }
                }
              case None => Left(ReplNotAvailable)
            }
          }
        case None => Left(NoInfoAvailable(moduleName, "-"))
      }
      case Left(noInfo) => Left(noInfo)
    }
  }

  private def findLibModuleIdentifiers(project: Project, moduleName: String): Either[NoInfo, Seq[ModuleIdentifier]] = {
    StackReplsManager.getGlobalRepl(project) match {
      case Some(repl) =>
        if (repl.isBusy) {
          Left(ReplIsBusy)
        } else if (!repl.available) {
          Left(ReplNotAvailable)
        } else {
          repl.getModuleIdentifiers(moduleName) match {
            case Some(o) if o.stderrLines.isEmpty && o.stdoutLines.nonEmpty => Right(o.stdoutLines.flatMap(l => findModuleIdentifiers(project, l, moduleName).toSeq))
            case _ => Left(ReplNotAvailable)
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

case class ModuleIdentifier(name: String, moduleName: String, declaration: String, isOperator: Boolean)
