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

import com.intellij.psi.PsiElement
import com.intellij.refactoring.listeners.RefactoringElementListener
import com.intellij.refactoring.rename.RenamePsiElementProcessor
import com.intellij.usageView.UsageInfo
import intellij.haskell.HaskellFile
import intellij.haskell.navigate.{HaskellFileResolveResult, HaskellGlobalResolveResult}
import intellij.haskell.psi.HaskellNamedElement
import intellij.haskell.util.FileUtil

class HaskellRenameVariableProcessor extends RenamePsiElementProcessor {

  private var isShowPreviewForced: Boolean = _

  override def prepareRenaming(element: PsiElement, newName: String, allRenames: java.util.Map[PsiElement, String]): Unit = {
    if (element.isInstanceOf[HaskellNamedElement]) {
      isShowPreviewForced = element.getReference.resolve() match {
        case _: HaskellGlobalResolveResult | _: HaskellFileResolveResult => true
        case _ => false
      }
    }
  }

  override def canProcessElement(element: PsiElement): Boolean = {
    element.isInstanceOf[HaskellNamedElement] || element.isInstanceOf[HaskellFile]
  }

  override def forcesShowPreview(): Boolean = {
    isShowPreviewForced
  }

  override def renameElement(element: PsiElement, newName: String, usages: Array[UsageInfo], listener: RefactoringElementListener): Unit = {
    super.renameElement(element, newName, usages, listener)
    FileUtil.saveAllFiles()
  }
}
