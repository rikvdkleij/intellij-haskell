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

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.{PsiElement, PsiFile}
import intellij.haskell.external.execution.{CompilationResult, HaskellCompilationResultHelper}
import intellij.haskell.external.repl.ProjectStackRepl.{Failed, Loaded}
import intellij.haskell.external.repl.StackReplsManager.StackComponentInfo
import intellij.haskell.external.repl._
import intellij.haskell.util.ScalaUtil
import intellij.haskell.util.index.HaskellFilePathIndex

private[component] object LoadComponent {

  def isFileLoaded(psiFile: PsiFile): Boolean = {
    val projectRepl = StackReplsManager.getProjectRepl(psiFile)
    projectRepl.map(_.isFileLoaded(psiFile)).contains(Loaded)
  }

  def isFileLoadedFailed(psiFile: PsiFile): Boolean = {
    val projectRepl = StackReplsManager.getProjectRepl(psiFile)
    projectRepl.map(_.isFileLoaded(psiFile)).contains(Failed)
  }

  def isModuleLoaded(moduleName: Option[String], psiFile: PsiFile): Boolean = {
    isFileLoaded(psiFile) || {
      for {
        mn <- moduleName
        repl <- StackReplsManager.getProjectRepl(psiFile)
      } yield repl.isModuleLoaded(mn)
    }.contains(true)
  }

  def isBusy(project: Project, stackComponentInfo: StackComponentInfo): Boolean = {
    val projectRepl = StackReplsManager.getRunningProjectRepl(project, stackComponentInfo)
    projectRepl.exists(_.isBusy)
  }

  def isBusy(psiFile: PsiFile): Boolean = {
    val projectRepl = StackReplsManager.getRunningProjectRepl(psiFile)
    projectRepl.exists(_.isBusy)
  }

  def load(psiFile: PsiFile, currentElement: Option[PsiElement]): Option[CompilationResult] = {
    val project = psiFile.getProject

    StackReplsManager.getProjectRepl(psiFile).flatMap(projectRepl => {

      // The REPL is not started if target which it's depends on has compile errors at the moment of start.
      synchronized {
        if (!projectRepl.available && !projectRepl.starting) {
          projectRepl.clearLoadedModules()
          projectRepl.start()
        }
      }

      projectRepl.load(psiFile) match {
        case Some((loadOutput, loadFailed)) =>
          ApplicationManager.getApplication.executeOnPooledThread(ScalaUtil.runnable {

            TypeInfoComponent.invalidate(psiFile)
            DefinitionLocationComponent.invalidate(psiFile)

            if (!loadFailed) {
              NameInfoComponent.invalidate(psiFile)

              val moduleName = HaskellFilePathIndex.findModuleName(psiFile, GlobalSearchScope.projectScope(project))
              moduleName.foreach(mn => {
                BrowseModuleComponent.refreshTopLevel(project, mn, psiFile)
                BrowseModuleComponent.invalidateForModuleName(project, mn, psiFile)
              })
            }
          })
          Some(HaskellCompilationResultHelper.createCompilationResult(psiFile, loadOutput.stderrLines, loadFailed))
        case _ => None
      }
    })
  }
}
