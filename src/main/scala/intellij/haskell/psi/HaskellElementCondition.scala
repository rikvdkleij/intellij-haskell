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

package intellij.haskell.psi

import com.intellij.openapi.util.Condition
import com.intellij.psi.PsiElement

object HaskellElementCondition {

  final val QualifiedNameElementCondition = new Condition[PsiElement]() {
    override def value(psiElement: PsiElement): Boolean = {
      psiElement match {
        case _: HaskellQualifiedNameElement => true
        case _ => false
      }
    }
  }

  final val DeclarationElementCondition = new Condition[PsiElement]() {
    override def value(psiElement: PsiElement): Boolean = {
      psiElement match {
        case _: HaskellDeclarationElement => true
        case _ => false
      }
    }
  }

  final val HighestDeclarationElementCondition = new Condition[PsiElement]() {
    override def value(psiElement: PsiElement): Boolean = {
      psiElement match {
        case _: HaskellModuleDeclaration => true
        case e: HaskellDeclarationElement if e.getParent.getNode.getElementType == HaskellTypes.HS_TOP_DECLARATION => true
        case _ => false
      }
    }
  }

  final val NamedElementCondition = new Condition[PsiElement]() {
    override def value(psiElement: PsiElement): Boolean = {
      psiElement match {
        case _: HaskellNamedElement => true
        case _ => false
      }
    }
  }
}
