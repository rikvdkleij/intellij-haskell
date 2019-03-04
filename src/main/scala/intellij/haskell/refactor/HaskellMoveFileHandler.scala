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

import com.intellij.psi.{PsiDirectory, PsiElement, PsiFile}
import com.intellij.refactoring.move.moveFilesOrDirectories.MoveFileHandler
import com.intellij.usageView.UsageInfo
import intellij.haskell.HaskellFile
import intellij.haskell.annotator.HaskellAnnotator
import intellij.haskell.external.component.HaskellComponentsManager
import intellij.haskell.psi.HaskellPsiUtil

class HaskellMoveFileHandler extends MoveFileHandler {
  override def prepareMovedFile(file: PsiFile, moveDestination: PsiDirectory, oldToNewMap: util.Map[PsiElement, PsiElement]): Unit = {}

  override def retargetUsages(usageInfos: util.List[UsageInfo], oldToNewMap: util.Map[PsiElement, PsiElement]): Unit = {}

  override def canProcessElement(psiFile: PsiFile): Boolean = {
    psiFile.isInstanceOf[HaskellFile]
  }

  override def findUsages(psiFile: PsiFile, newParent: PsiDirectory, searchInComments: Boolean, searchInNonJavaFiles: Boolean): util.List[UsageInfo] = {
    java.util.Collections.emptyList()
  }

  override def updateMovedFile(psiFile: PsiFile): Unit = {
    HaskellPsiUtil.invalidateModuleName(psiFile)
    HaskellComponentsManager.clearLoadedModule(psiFile)
    HaskellComponentsManager.invalidateHaskellFileInfoCache(psiFile)
    HaskellAnnotator.restartDaemonCodeAnalyzerForFile(psiFile)
  }

}