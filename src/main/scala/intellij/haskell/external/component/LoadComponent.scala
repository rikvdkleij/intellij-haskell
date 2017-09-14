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
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.progress.{ProgressIndicator, ProgressManager, Task}
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.libraries.LibraryUtil
import com.intellij.psi.{PsiElement, PsiFile}
import com.intellij.util.WaitFor
import intellij.haskell.annotator.HaskellAnnotator
import intellij.haskell.external.execution.{CompilationResult, HaskellCompilationResultHelper, StackCommandLine}
import intellij.haskell.external.repl._
import intellij.haskell.psi.HaskellPsiUtil
import intellij.haskell.util.{HaskellFileUtil, HaskellProjectUtil, TypeInfoUtil}

private[component] object LoadComponent {

  def isLoaded(psiFile: PsiFile): Option[IsFileLoaded] = {
    val projectRepl = StackReplsManager.getProjectRepl(psiFile)
    projectRepl.map(_.isLoaded(psiFile))
  }

  def isBusy(project: Project): Boolean = {
    val projectRepl = StackReplsManager.getProjectLibraryRepl(project)
    projectRepl.exists(_.isBusy)
  }

  def isBusy(psiFile: PsiFile): Boolean = {
    val projectRepl = StackReplsManager.getProjectRepl(psiFile)
    projectRepl.exists(_.isBusy)
  }

  def load(psiFile: PsiFile, currentElement: Option[PsiElement]): Option[CompilationResult] = {
    val project = psiFile.getProject
    val stackComponentInfo = HaskellComponentsManager.findStackComponentInfo(psiFile)
    val projectRepl = StackReplsManager.getProjectRepl(psiFile)

    val fileOfSelectedEditor = isFileOfSelectedEditor(psiFile)
    if (fileOfSelectedEditor) {
      stackComponentInfo.foreach(info => {
        if (info.stanzaType != LibType) {
          val module = HaskellProjectUtil.findModule(psiFile)
          val namesOfPackagesToRebuild = ProjectLibraryFileWatcher.changedLibrariesByPackageName.filter(pn => pn._1 == info.packageName || module.exists(mn => LibraryUtil.findLibrary(mn, pn._1) != null)).keys
          namesOfPackagesToRebuild.foreach(nameOfPackageToRebuild => {
            val stackComponentInfo = ProjectLibraryFileWatcher.changedLibrariesByPackageName.remove(nameOfPackageToRebuild)
            stackComponentInfo match {
              case Some(libraryInfo) =>
                ApplicationManager.getApplication.executeOnPooledThread(new Runnable() {

                  override def run(): Unit = {
                    StackCommandLine.executeInMessageView(project, Seq("build", libraryInfo.target, "--fast"))
                    StackReplsManager.getReplsManager(project).foreach(_.restartProjectNonLibraryRepl())
                    HaskellAnnotator.restartDaemonCodeAnalyzerForFile(psiFile)
                  }
                })
              case None => ()
            }
          })
        }
      })

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
    }

    projectRepl.flatMap(_.load(psiFile)) match {
      case Some((loadOutput, loadFailed)) =>
        if (!loadFailed && fileOfSelectedEditor) {
          val moduleName = HaskellPsiUtil.findModuleName(psiFile, runInRead = true)
          ApplicationManager.getApplication.executeOnPooledThread(new Runnable {

            override def run(): Unit = {
              DefinitionLocationComponent.invalidate(psiFile)
              NameInfoComponent.invalidate(psiFile)

              BrowseModuleComponent.refreshTopLevel(project, psiFile)
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

  private def isFileOfSelectedEditor(psiFile: PsiFile): Boolean = {
    var fileOfSelectedEditor: Option[Boolean] = None
    ApplicationManager.getApplication.invokeLater(() => {
      fileOfSelectedEditor = Option(FileEditorManager.getInstance(psiFile.getProject).getSelectedTextEditor).map(e => HaskellFileUtil.findDocument(psiFile).contains(e.getDocument)).orElse(Some(false))
    })

    new WaitFor(5000, 1) {
      override def condition(): Boolean = {
        fileOfSelectedEditor.isDefined
      }
    }
    fileOfSelectedEditor.getOrElse(false)
  }
}
