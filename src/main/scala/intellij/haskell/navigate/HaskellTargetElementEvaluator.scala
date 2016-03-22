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

package intellij.haskell.navigate

import com.intellij.codeInsight.TargetElementEvaluatorEx
import com.intellij.psi.{PsiElement, PsiFile, PsiReference}

class HaskellTargetElementEvaluator extends TargetElementEvaluatorEx {
  def isIdentifierPart(element: PsiFile, text: CharSequence, offset: Int): Boolean = {
    import intellij.haskell.psi.HaskellTypes._
    element.findElementAt(offset).getNode.getElementType match {
      case HS_VARID_ID => true
      case HS_CONID_ID => true
      case HS_VARSYM_ID => true
      case HS_CONSYM_ID => true
      case _ => false
    }
  }

  def includeSelfInGotoImplementation(element: PsiElement): Boolean = {
    false
  }

  def getElementByReference(ref: PsiReference, flags: Int): PsiElement = {
    null
  }
}