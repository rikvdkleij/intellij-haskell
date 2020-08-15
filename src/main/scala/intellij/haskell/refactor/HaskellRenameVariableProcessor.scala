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

package intellij.haskell.refactor

import java.util

import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.psi.{PsiElement, PsiFile}
import com.intellij.refactoring.listeners.RefactoringElementListener
import com.intellij.refactoring.rename.RenamePsiElementProcessor
import intellij.haskell.HaskellFile
import intellij.haskell.external.component.{HaskellComponentsManager, ProjectLibraryBuilder}
import intellij.haskell.external.repl.StackRepl.LibType
import intellij.haskell.psi.HaskellModid
import intellij.haskell.util.{HaskellFileUtil, HaskellProjectUtil, ScalaUtil}

class HaskellRenameVariableProcessor extends RenamePsiElementProcessor {

  // Target element is element of the definition
  // Invalidate cache is necessary because during (inline) renaming the id of psi element is changed
  override def prepareRenaming(targetElement: PsiElement, newName: String, allRenames: util.Map[PsiElement, String]): Unit = {
    val project = targetElement.getProject
    for {
      cf <- getCurrentFile(project)
      () = HaskellComponentsManager.invalidateDefinitionLocations(project)
      tf <- Option(targetElement.getContainingFile).map(_.getOriginalFile)
      componentTarget <- HaskellComponentsManager.findStackComponentInfo(tf)
      currentComponentTarget <- HaskellComponentsManager.findStackComponentInfo(cf)
    } yield if (componentTarget != currentComponentTarget && componentTarget.stanzaType == LibType)
      ProjectLibraryBuilder.addBuild(project, Set(componentTarget)) else ()
  }

  override def canProcessElement(psiElement: PsiElement): Boolean = {
    if (!psiElement.isInstanceOf[HaskellFile] && !psiElement.isInstanceOf[HaskellModid] && psiElement.isValid) {
      val project = psiElement.getProject
      Option(psiElement.getContainingFile).exists { psiFile =>
        HaskellProjectUtil.isHaskellProject(project) &&
          (psiElement match {
            case pf: PsiFile => HaskellProjectUtil.isSourceFile(pf)
            case _ =>
              Option(psiElement.getReference).map(_.getElement) match {
                case Some(_: PsiElement) => HaskellProjectUtil.isSourceFile(psiFile)
                case _ => false
              }
          })
      }
    } else {
      false
    }
  }

  override def getPostRenameCallback(targetElement: PsiElement, newName: String, elementListener: RefactoringElementListener): Runnable = {
    ScalaUtil.runnable {
      val project = targetElement.getProject
      HaskellComponentsManager.invalidateDefinitionLocations(project)
      HaskellFileUtil.saveFiles(project)
    }
  }

  private def getCurrentFile(project: Project) = {
    FileEditorManager.getInstance(project).getSelectedFiles.headOption.flatMap(f => HaskellFileUtil.convertToHaskellFileDispatchThread(project, f))
  }
}
