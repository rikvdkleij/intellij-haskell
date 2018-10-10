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

import java.util

import com.intellij.psi.{PsiElement, PsiFile}
import com.intellij.refactoring.listeners.RefactoringElementListener
import com.intellij.refactoring.rename.RenamePsiElementProcessor
import intellij.haskell.HaskellNotificationGroup
import intellij.haskell.external.component.HaskellComponentsManager
import intellij.haskell.util.{HaskellFileUtil, HaskellProjectUtil, ScalaUtil}

class HaskellRenameVariableProcessor extends RenamePsiElementProcessor {

  // Target element is element of the definition
  // Invalidate cache is necessary because during (inline) renaming the id of psi element is changed
  override def prepareRenaming(targetElement: PsiElement, newName: String, allRenames: util.Map[PsiElement, String]): Unit = {
    val targetFile = targetElement.getContainingFile.getOriginalFile
    val usageElements = HaskellComponentsManager.findReferencesInCache(targetFile).filterNot(_._1 == targetFile).map(_._2)
    if (usageElements.nonEmpty) {
      HaskellComponentsManager.invalidateDefinitionLocationCache(usageElements)
    }
  }

  override def canProcessElement(psiElement: PsiElement): Boolean = {
    if (HaskellComponentsManager.isReplBusy(psiElement.getProject)) {
      HaskellNotificationGroup.logInfoEvent(psiElement.getProject, "Renaming is not available while REPL is busy")
      false
    } else {
      val project = psiElement.getProject
      Option(psiElement.getContainingFile).exists { psiFile =>
        HaskellProjectUtil.isHaskellProject(project) &&
          (psiElement match {
            case pf: PsiFile => HaskellProjectUtil.isSourceFile(pf)
            case _ =>
              Option(psiElement.getReference).map(_.getElement) match {
                case Some(e: PsiElement) => HaskellProjectUtil.isSourceFile(psiFile)
                case _ => false
              }
          })
      }
    }
  }

  override def getPostRenameCallback(element: PsiElement, newName: String, elementListener: RefactoringElementListener): Runnable = {
    ScalaUtil.runnable {
      val psiFile = element.getContainingFile.getOriginalFile
      val project = element.getProject
      HaskellFileUtil.saveAllFiles(project, psiFile)
    }
  }
}
