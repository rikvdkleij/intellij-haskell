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
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.libraries.LibraryUtil
import com.intellij.openapi.util.Computable
import com.intellij.psi.{PsiElement, PsiFile}
import com.intellij.util.WaitFor
import intellij.haskell.annotator.HaskellAnnotator
import intellij.haskell.external.execution.{CompilationResult, HaskellCompilationResultHelper}
import intellij.haskell.external.repl.ProjectStackRepl.{Failed, IsFileLoaded, Loaded}
import intellij.haskell.external.repl.StackRepl.LibType
import intellij.haskell.external.repl._
import intellij.haskell.psi.HaskellPsiUtil
import intellij.haskell.util.{HaskellFileUtil, HaskellProjectUtil}

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

    StackReplsManager.getProjectRepl(psiFile).flatMap(projectRepl => {
      val fileOfSelectedEditor = isFileOfSelectedEditor(psiFile)
      lazy val stackComponentInfo = HaskellComponentsManager.findStackComponentInfo(psiFile)
      if (fileOfSelectedEditor) {
        stackComponentInfo.foreach(componentInfo => {
          lazy val moduleName = findModule(psiFile)
          val libraryNames = ProjectLibraryFileWatcher.builtLibraries.filter {
            case (pn, _) => componentInfo.stanzaType != LibType && pn == componentInfo.packageName || moduleName.exists(m => LibraryUtil.findLibrary(m, pn) != null)
          }.keys

          if (libraryNames.nonEmpty) {
            libraryNames.foreach(ProjectLibraryFileWatcher.builtLibraries.remove)
            StackReplsManager.getReplsManager(project).foreach(_.restartProjectRepl(projectRepl))
            HaskellAnnotator.getDaemonCodeAnalyzer(project).restart(psiFile)
          }
        })

        // The REPL is not started if target which it's depends on has compile errors at the moment of start.
        synchronized {
          if (!projectRepl.available && !projectRepl.starting) {
            projectRepl.start()
          }
        }
      }

      val loaded = projectRepl.isLoaded(psiFile)
      val reload = loaded == Loaded || loaded == Failed
      projectRepl.load(psiFile, reload) match {
        case Some((loadOutput, loadFailed)) =>
          if (fileOfSelectedEditor) {
            ApplicationManager.getApplication.executeOnPooledThread(new Runnable {

              override def run(): Unit = {
                DefinitionLocationComponent.invalidate(psiFile)
                TypeInfoComponent.invalidate(psiFile)

                if (!loadFailed) {
                  NameInfoComponent.invalidate(psiFile)

                  BrowseModuleComponent.refreshTopLevel(project, psiFile)
                  val moduleName = HaskellPsiUtil.findModuleName(psiFile, runInRead = true)
                  moduleName.foreach(mn => BrowseModuleComponent.invalidateForModuleName(project, mn))

                  // FIXME For now disabled to improve to responsiveness
                  // Only preload types for Lib targets because expressions in hspec files can be large....
//                  if (stackComponentInfo.exists(_.stanzaType == LibType)) {
//                    currentElement.foreach(TypeInfoUtil.preloadTypesAround)
//                  }
                }
              }
            })
          }
          Some(HaskellCompilationResultHelper.createCompilationResult(Some(psiFile), loadOutput.stderrLines, loadFailed))
        case _ => None
      }
    })
  }


  private def isFileOfSelectedEditor(psiFile: PsiFile): Boolean = {
    var fileOfSelectedEditor: Option[Boolean] = None
    ApplicationManager.getApplication.invokeLater(() => {
      if (!psiFile.getProject.isDisposed) {
        fileOfSelectedEditor = Option(FileEditorManager.getInstance(psiFile.getProject).getSelectedTextEditor).map(e => HaskellFileUtil.findDocument(psiFile).contains(e.getDocument)).orElse(Some(false))
      }
    })

    new WaitFor(5000, 1) {
      override def condition(): Boolean = {
        fileOfSelectedEditor.isDefined
      }
    }
    fileOfSelectedEditor.getOrElse(false)
  }

  private def findModule(psiFile: PsiFile) = {
    ApplicationManager.getApplication.runReadAction(new Computable[Option[Module]] {
      override def compute(): Option[Module] = {
        HaskellProjectUtil.findModule(psiFile)
      }
    })
  }
}
