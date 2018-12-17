/*
 * Copyright 2014-2018 Rik van der Kleij

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

package intellij.haskell.psi.impl

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import intellij.haskell.psi._

class HaskellCompositeElementImpl(node: ASTNode) extends ASTWrapperPsiElement(node) with HaskellCompositeElement {

  override def toString: String = {
    getNode.getElementType.toString
  }
}

abstract class HaskellCNameElementImpl private[impl](node: ASTNode) extends HaskellCompositeElementImpl(node) with HaskellCNameElement

abstract class HaskellExpressionElementImpl private[impl](node: ASTNode) extends HaskellCompositeElementImpl(node) with HaskellExpressionElement

abstract class HaskellNamedElementImpl private[impl](node: ASTNode) extends HaskellCompositeElementImpl(node) with HaskellNamedElement

abstract class HaskellQualifierElementImpl private[impl](node: ASTNode) extends HaskellNamedElementImpl(node) with HaskellQualifierElement
