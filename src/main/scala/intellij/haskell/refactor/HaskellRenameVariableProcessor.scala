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

import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.psi.{PsiElement, PsiReference}
import com.intellij.refactoring.listeners.RefactoringElementListener
import com.intellij.refactoring.rename.RenamePsiElementProcessor
import com.intellij.usageView.UsageInfo
import intellij.haskell.util.{HaskellFileIndex, HaskellFileUtil, HaskellProjectUtil}

import scala.collection.JavaConversions._

class HaskellRenameVariableProcessor extends RenamePsiElementProcessor {

  override def canProcessElement(element: PsiElement): Boolean = HaskellProjectUtil.isHaskellStackProject(element.getProject)

  override def renameElement(element: PsiElement, newName: String, usages: Array[UsageInfo], listener: RefactoringElementListener): Unit = {
    super.renameElement(element, newName, usages, listener)
    HaskellFileUtil.saveAllFiles()
  }

  override def findReferences(element: PsiElement): java.util.Collection[PsiReference] = {
    val project = element.getProject
    ReferencesSearch.search(element, GlobalSearchScope.filesScope(project, HaskellFileIndex.findProjectFiles(project))).findAll
  }
}
