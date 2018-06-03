/*
 * Copyright 2014-2017 Rik van der Kleij
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
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.{IndexNotReadyException, Project}
import com.intellij.psi.PsiFile
import com.intellij.psi.search.GlobalSearchScope
import intellij.haskell.external.repl._
import intellij.haskell.util.index.HaskellModuleNameIndex
import intellij.haskell.util.{ScalaUtil, StringUtil}

import scala.concurrent.{ExecutionContext, Future}

private[component] object BrowseModuleComponent {

  private case class Key(project: Project, moduleName: String, psiFile: Option[PsiFile], exported: Boolean)

  private sealed trait NoBrowseInfo

  private case object ReplNotAvailable extends NoBrowseInfo

  private case object ReplIsBusy extends NoBrowseInfo

  private case object NoInfoAvailable extends NoBrowseInfo

  private case object IndexNotReady extends NoBrowseInfo

  type BrowseModuleResult = Iterable[ModuleIdentifier]
  private type BrowseModuleInternalResult = Either[NoBrowseInfo, Iterable[ModuleIdentifier]]

  private final val Cache: AsyncLoadingCache[Key, BrowseModuleInternalResult] = Scaffeine().buildAsync((k: Key) => {
    if (k.project.isDisposed) {
      Left(NoInfoAvailable)
    } else {
      findModuleIdentifiers(k)
    }
  })

  def findModuleIdentifiers(project: Project, moduleName: String, psiFile: Option[PsiFile])(implicit ec: ExecutionContext): Future[Iterable[ModuleIdentifier]] = {

    def findKey = {
      psiFile match {
        case None =>
          val projectPsiFile = try {
            Right(ApplicationManager.getApplication.runReadAction(ScalaUtil.computable(HaskellModuleNameIndex.findHaskellFileByModuleName(project, moduleName, GlobalSearchScope.projectScope(project)))))
          } catch {
            case _: IndexNotReadyException => Left("Indices not ready")
          }
          projectPsiFile match {
            case Right(pf) => Some(Key(project, moduleName, pf, exported = true))
            case Left(_) => None
          }
        case pf@Some(_) => Some(Key(project, moduleName, pf, exported = false))
      }
    }

    findKey match {
      case Some(key) =>

        def matchResult(result: Future[BrowseModuleInternalResult]) = {
          concurrent.blocking(result.map {
            case Right(ids) => ids
            case Left(NoInfoAvailable) =>
              Iterable()
            case Left(ReplNotAvailable) =>
              Cache.synchronous().invalidate(key)
              Iterable()
            case Left(ReplIsBusy) =>
              Cache.synchronous.invalidate(key)
              Iterable()
            case Left(IndexNotReady) =>
              Cache.synchronous.invalidate(key)
              Iterable()
          })
        }

        Cache.getIfPresent(key) match {
          case Some(r) => matchResult(r)
          case None => key.psiFile match {
            case None => matchResult(Cache.get(key))
            case Some(pf) => if (!key.exported && LoadComponent.isFileLoaded(pf)) {
              matchResult(Cache.get(key))
            } else if (key.exported && LoadComponent.isModuleLoaded(Some(moduleName), pf)) {
              matchResult(Cache.get(key))
            } else {
              Future.successful(Iterable())
            }
          }
        }
      case None => Future.successful(Iterable())
    }
  }

  def findModuleNamesInCache(project: Project): Iterable[String] = {
    Cache.synchronous().asMap().filter(_._1.project == project).map(_._1.moduleName)
  }

  def refreshTopLevel(project: Project, moduleName: String, psiFile: PsiFile): Unit = {
    val key = Key(project, moduleName, Some(psiFile), exported = false)
    Cache.synchronous().refresh(key)
  }

  def invalidateForModuleName(project: Project, moduleName: String, psiFile: PsiFile): Unit = {
    val key = Key(project, moduleName, Some(psiFile), exported = true)
    Cache.synchronous.invalidate(key)
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
        if (LoadComponent.isBusy(psiFile)) {
          Left(ReplIsBusy)
        } else if (LoadComponent.isFileLoaded(psiFile)) {
          StackReplsManager.getProjectRepl(psiFile).flatMap(_.getLocalModuleIdentifiers(moduleName, psiFile)).map { output =>
            Right(output.stdoutLines.takeWhile(l => !l.startsWith("-- imported via")).flatMap(l => findModuleIdentifiers(project, l, moduleName)))
          }.getOrElse(Left(ReplNotAvailable))
        } else {
          Left(NoInfoAvailable)
        }
      case Some(psiFile) if key.exported =>
        val output = StackReplsManager.getProjectRepl(psiFile).flatMap(_.getModuleIdentifiers(moduleName, psiFile))
        output match {
          case Some(o) if o.stderrLines.isEmpty => output.map(_.stdoutLines.flatMap(l => findModuleIdentifiers(project, l, moduleName))) match {
            case Some(ids) => Right(ids)
            case None => Left(NoInfoAvailable)
          }
          case _ => Left(ReplNotAvailable)
        }
      case None => findLibraryModuleIdentifiers(project, moduleName)
    }
  }

  private def findLibraryModuleIdentifiers(project: Project, moduleName: String): Either[NoBrowseInfo, Seq[ModuleIdentifier]] = {
    StackReplsManager.getGlobalRepl(project).flatMap(_.getModuleIdentifiers(moduleName)) match {
      case None => Left(ReplNotAvailable)
      case Some(o) if o.stdoutLines.nonEmpty => Right(o.stdoutLines.flatMap(l => findModuleIdentifiers(project, l, moduleName).toSeq))
      case _ => Left(NoInfoAvailable)
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
    ModuleIdentifier(StringUtil.removeOuterParens(name), moduleName, declaration, isOperator = DeclarationLineUtil.isOperator(name))
  }
}

case class ModuleIdentifier(name: String, moduleName: String, declaration: String, isOperator: Boolean)
