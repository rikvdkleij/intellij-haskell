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

import com.intellij.lang.refactoring.RefactoringSupportProvider
import com.intellij.psi._
import intellij.haskell.util.HaskellProjectUtil

class HaskellRefactoringSupportProvider extends RefactoringSupportProvider {

  override def isMemberInplaceRenameAvailable(psiElement: PsiElement, context: PsiElement): Boolean = {
    !psiElement.isInstanceOf[PsiFile] && isDefinedInProject(psiElement)
  }

  private def isDefinedInProject(psiElement: PsiElement) = {
    Option(psiElement.getReference).map(_.getElement) match {
      case Some(e) => HaskellProjectUtil.isProjectFile(e.getContainingFile)
      case _ => false
    }
  }
}