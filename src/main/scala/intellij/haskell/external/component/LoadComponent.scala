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

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.progress.{ProgressIndicator, ProgressManager, Task}
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.libraries.LibraryUtil
import com.intellij.psi.{PsiElement, PsiFile}
import intellij.haskell.annotator.HaskellAnnotator
import intellij.haskell.external.execution.{CompilationResult, HaskellCompilationResultHelper, StackCommandLine}
import intellij.haskell.external.repl._
import intellij.haskell.psi.HaskellPsiUtil
import intellij.haskell.util.TypeInfoUtil

private[component] object LoadComponent {

  def isLoaded(psiFile: PsiFile): Option[IsFileLoaded] = {
    val projectRepl = StackReplsManager.getProjectRepl(psiFile)
    projectRepl.map(_.isLoaded(psiFile))
  }

  def isBusy(project: Project): Boolean = {
    val projectRepl = StackReplsManager.getProjectRepl(project)
    projectRepl.exists(_.isBusy)
  }

  def isBusy(psiFile: PsiFile): Boolean = {
    val projectRepl = StackReplsManager.getProjectRepl(psiFile)
    projectRepl.exists(_.isBusy)
  }

  def load(psiFile: PsiFile, currentElement: Option[PsiElement]): Option[CompilationResult] = {
    val project = psiFile.getProject

    ProgressManager.checkCanceled()

    val stackComponentInfo = HaskellComponentsManager.findStackComponentInfo(psiFile)

    ProgressManager.checkCanceled()

    stackComponentInfo.foreach(info => {
      if (info.stanzaType != LibType) {
        val module = ModuleUtilCore.findModuleForPsiElement(psiFile)
        val namesOfPackagesToRebuild = ProjectLibraryFileWatcher.changedLibrariesByPackageName.filter(pn => pn._1 == info.packageName || LibraryUtil.findLibrary(module, pn._1) != null).keys
        namesOfPackagesToRebuild.foreach(nameOfPackageToRebuild => {
          val stackComponentInfo = ProjectLibraryFileWatcher.changedLibrariesByPackageName.remove(nameOfPackageToRebuild)
          stackComponentInfo match {
            case Some(libraryInfo) =>
              val progressManager = ProgressManager.getInstance()
              progressManager.run(new Task.Backgroundable(project, s"Busy building library ${libraryInfo.packageName}", false) {

                override def run(indicator: ProgressIndicator): Unit = {
                  StackCommandLine.executeInMessageView(project, Seq("build", libraryInfo.target, "--fast"), progressManager.getProgressIndicator)
                  StackReplsManager.getReplsManager(project).foreach(_.restartProjectTestRepl())
                  HaskellAnnotator.restartDaemonCodeAnalyzerForFile(psiFile)
                }
              })
            case None => ()
          }
        })
      }
    })

    ProgressManager.checkCanceled()

    val projectRepl = StackReplsManager.getProjectRepl(psiFile)

    // The REPL is not started if target has compile errors at the moment of start.
    projectRepl.foreach(repl => {
      if (!repl.available) {
        if (stackComponentInfo.exists(_.stanzaType != LibType)) {
          ProgressManager.getInstance().run(new Task.Backgroundable(project, "Building project", false) {

            def run(progressIndicator: ProgressIndicator): Unit = {
              val result = StackCommandLine.buildProjectInMessageView(project, progressIndicator)
              if (result.contains(true)) {
                repl.start()
                HaskellAnnotator.restartDaemonCodeAnalyzerForFile(psiFile)
              }
            }
          })
        }
      }
    })

    ProgressManager.checkCanceled()

    projectRepl.flatMap(_.load(psiFile)) match {
      case Some((loadOutput, loadFailed)) =>
        if (!loadFailed) {
          val moduleName = HaskellPsiUtil.findModuleName(psiFile, runInRead = true)
          ApplicationManager.getApplication.executeOnPooledThread(new Runnable {

            override def run(): Unit = {
              DefinitionLocationComponent.invalidate(psiFile)
              NameInfoComponent.invalidate(psiFile)

              BrowseModuleComponent.invalidateTopLevel(project, psiFile)
              moduleName.foreach(mn => BrowseModuleComponent.invalidateForModuleName(project, mn))

              TypeInfoComponent.invalidate(psiFile)
              currentElement.foreach(TypeInfoUtil.preloadTypesAround)
            }
          })
        }

        Some(HaskellCompilationResultHelper.createCompilationResult(Some(psiFile), loadOutput.stdErrLines, loadFailed))
      case _ => None
    }
  }
}
