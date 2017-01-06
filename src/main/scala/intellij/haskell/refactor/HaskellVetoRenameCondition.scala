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

import com.intellij.openapi.util.Condition
import com.intellij.psi.{PsiElement, PsiFile, PsiPolyVariantReference, ResolveResult}
import intellij.haskell.util.HaskellProjectUtil

class HaskellVetoRenameCondition extends Condition[PsiElement] {
  override def value(element: PsiElement): Boolean = {
    element match {
      case f: PsiFile => HaskellProjectUtil.isLibraryFile(f).getOrElse(true)
      case _ =>
        val resolveResult = Option(element.getReference).flatMap(r => r match {
          case r: PsiPolyVariantReference => r.multiResolve(false).headOption
          case _ => None
        })
        resolveResult match {
          case Some(rr: ResolveResult) => HaskellProjectUtil.isLibraryFile(rr.getElement.getContainingFile).getOrElse(true)
          case _ => true
        }
    }
  }
}
