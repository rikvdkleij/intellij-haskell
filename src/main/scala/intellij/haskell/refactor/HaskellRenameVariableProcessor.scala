/*
 * Copyright 2016 Rik van der Kleij
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
import com.intellij.usageView.UsageInfo
import intellij.haskell.util.{HaskellFileUtil, HaskellProjectUtil}

class HaskellRenameVariableProcessor extends RenamePsiElementProcessor {

  override def canProcessElement(psiElement: PsiElement): Boolean = {
    HaskellProjectUtil.isHaskellProject(psiElement.getProject) &&
      (psiElement match {
        case pf: PsiFile => HaskellProjectUtil.isProjectFile(pf).getOrElse(false)
        case _ => Option(psiElement.getReference) match {
          case Some(e: PsiElement) => HaskellProjectUtil.isProjectFile(e.getContainingFile).getOrElse(false)
          case _ => false
        }
      })
  }

  override def renameElement(psiElement: PsiElement, newName: String, usages: Array[UsageInfo], listener: RefactoringElementListener): Unit = {
    super.renameElement(psiElement, newName, usages, listener)
    val psiFile = usages.headOption.map(_.getFile).map(_.getOriginalFile)
    HaskellFileUtil.saveAllFiles(psiFile)
  }
}
