/*
 * Copyright 2014-2017 Rik van der Kleij
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
import com.intellij.psi._
import intellij.haskell.util.HaskellProjectUtil

class HaskellVetoRenameCondition extends Condition[PsiElement] {
  override def value(psiElement: PsiElement): Boolean = {
    psiElement match {
      case pf: PsiFile => HaskellProjectUtil.isLibraryFile(pf).getOrElse(true)
      case _ => Option(psiElement.getReference) match {
        case Some(e: PsiElement) => HaskellProjectUtil.isLibraryFile(e.getContainingFile).getOrElse(true)
        case _ => true
      }
    }
  }
}
