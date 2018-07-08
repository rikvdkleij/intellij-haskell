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

package intellij.haskell.refactor

import com.intellij.psi.{PsiElement, PsiFile}
import com.intellij.refactoring.listeners.RefactoringElementListener
import com.intellij.refactoring.rename.RenamePsiElementProcessor
import intellij.haskell.util.{HaskellFileUtil, HaskellProjectUtil, ScalaUtil}

class HaskellRenameVariableProcessor extends RenamePsiElementProcessor {

  override def canProcessElement(psiElement: PsiElement): Boolean = {
    HaskellProjectUtil.isHaskellProject(psiElement.getProject) &&
      (psiElement match {
        case psiFile: PsiFile => HaskellProjectUtil.isProjectFile(psiFile)
        case _ => Option(psiElement.getReference).map(_.getElement) match {
          case Some(e: PsiElement) => HaskellProjectUtil.isProjectFile(e.getContainingFile)
          case _ => false
        }
      })
  }

  override def getPostRenameCallback(element: PsiElement, newName: String, elementListener: RefactoringElementListener): Runnable = {
    ScalaUtil.runnable {
      val psiFile = element.getContainingFile.getOriginalFile
      val project = element.getProject
      HaskellFileUtil.saveAllFiles(project, psiFile)
    }
  }
}
