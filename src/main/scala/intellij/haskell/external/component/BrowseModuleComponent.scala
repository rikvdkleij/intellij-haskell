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

import com.github.blemale.scaffeine.{LoadingCache, Scaffeine}
import com.intellij.openapi.project.{DumbService, Project}
import com.intellij.psi.PsiFile
import com.intellij.psi.search.GlobalSearchScopesCore
import intellij.haskell.external.repl.ProjectStackRepl.Failed
import intellij.haskell.external.repl._
import intellij.haskell.util.index.HaskellModuleNameIndex
import intellij.haskell.util.{ScalaUtil, StringUtil}

private[component] object BrowseModuleComponent {

  private case class Key(project: Project, moduleName: String, psiFile: Option[PsiFile])

  type BrowseModuleResult = Either[NoInfo, Iterable[ModuleIdentifier]]

  private final val Cache: LoadingCache[Key, BrowseModuleResult] = Scaffeine().build((k: Key) => findModuleIdentifiers(k))

  def findModuleIdentifiers(project: Project, moduleName: String, psiFile: Option[PsiFile]): Iterable[ModuleIdentifier] = {
    val key = Key(project, moduleName, psiFile)
    Cache.get(key) match {
      case Right(ids) => ids
      case Left(NoInfoAvailable) =>
        Iterable()
      case Left(ReplNotAvailable) =>
        Cache.invalidate(key)
        Iterable()
      case Left(ReplIsBusy) =>
        Cache.invalidate(key)
        Iterable()
    }
  }

  def findModuleNamesInCache(project: Project): Iterable[String] = {
    Cache.asMap().filter(_._1.project == project).map(_._1.moduleName)
  }

  def invalidateTopLevel(project: Project, psiFile: PsiFile): Unit = {
    val keys = Cache.asMap().keys.filter(k => k.project == project && k.psiFile.contains(psiFile))
    keys.foreach(k => Cache.invalidate(k))
  }

  def refreshTopLevel(project: Project, psiFile: PsiFile): Unit = {
    val keys = Cache.asMap().keys.filter(k => k.project == project && k.psiFile.contains(psiFile))
    keys.foreach(k => Cache.refresh(k))
  }

  def invalidateForModuleName(project: Project, moduleName: String): Unit = {
    val keys = Cache.asMap().keys.filter(k => k.project == project && k.moduleName == moduleName && k.psiFile.isEmpty)
    keys.foreach(k => Cache.invalidate(k))
  }

  def invalidate(project: Project): Unit = {
    val keys = Cache.asMap().keys.filter(k => k.project == project)
    keys.foreach(k => Cache.invalidate(k))
  }

  private def findModuleIdentifiers(key: Key): BrowseModuleResult = {
    val project = key.project
    val moduleName = key.moduleName

    key.psiFile match {
      case Some(psiFile) =>
        if (LoadComponent.isBusy(psiFile)) {
          Left(ReplIsBusy)
        } else if (LoadComponent.isLoaded(psiFile).exists(_ != Failed)) {
          StackReplsManager.getProjectRepl(psiFile).flatMap(_.getLocalModuleIdentifiers(moduleName, psiFile)).map { output =>
            Right(output.stdoutLines.takeWhile(l => !l.startsWith("-- imported via")).flatMap(l => findModuleIdentifiers(project, l, moduleName)))
          }.getOrElse(Left(ReplNotAvailable))
        } else {
          Left(NoInfoAvailable)
        }
      case None =>
        val projectHaskellFiles =
          if (!project.isDisposed) {
            val productionFile = DumbService.getInstance(project).tryRunReadActionInSmartMode(ScalaUtil.computable(
              HaskellModuleNameIndex.findHaskellFileByModuleName(project, moduleName, GlobalSearchScopesCore.projectProductionScope(project))
            ), null)

            if (productionFile.isEmpty) {
              DumbService.getInstance(project).tryRunReadActionInSmartMode(ScalaUtil.computable(
                HaskellModuleNameIndex.findHaskellFileByModuleName(project, moduleName, GlobalSearchScopesCore.projectTestScope(project))
              ), null)
            } else {
              productionFile
            }
          } else {
            None
          }

        if (projectHaskellFiles.nonEmpty) {
          if (LoadComponent.isBusy(project)) {
            Left(ReplIsBusy)
          } else {
            val output = projectHaskellFiles.flatMap(f => StackReplsManager.getProjectRepl(f).flatMap(_.getModuleIdentifiers(moduleName, f)))
            output match {
              case Some(o) if o.stderrLines.isEmpty => output.map(_.stdoutLines.flatMap(l => findModuleIdentifiers(project, l, moduleName))) match {
                case Some(ids) => Right(ids)
                case None => Left(NoInfoAvailable)
              }
              case _ => Left(ReplNotAvailable)
            }
          }
        } else {
          val moduleIdentifiers =
            StackReplsManager.getGlobalRepl(project).flatMap(_.getModuleIdentifiers(moduleName)) match {
              case None => Left(ReplNotAvailable)
              case Some(o) if o.stdoutLines.nonEmpty => Right(o.stdoutLines.flatMap(l => findModuleIdentifiers(project, l, moduleName).toSeq))
              case _ => Left(NoInfoAvailable)
            }

          moduleIdentifiers
        }
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
