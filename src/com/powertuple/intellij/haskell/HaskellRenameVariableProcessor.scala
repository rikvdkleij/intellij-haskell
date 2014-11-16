/*
 * Copyright 2014 Rik van der Kleij
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

package com.powertuple.intellij.haskell

import com.intellij.psi.search.LocalSearchScope
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.{PsiElement, PsiReference}
import com.intellij.refactoring.rename.RenamePsiElementProcessor
import com.powertuple.intellij.haskell.external.{GhcModiManager, IdentifierInfo}
import com.powertuple.intellij.haskell.psi.{HaskellExpression, HaskellNamedElement}

import scala.collection.JavaConversions._

class HaskellRenameVariableProcessor extends RenamePsiElementProcessor {

  private var ghcModInfo: Seq[IdentifierInfo] = _

  override def prepareRenaming(element: PsiElement, newName: String, allRenames: java.util.Map[PsiElement, String]): Unit = {
    ghcModInfo = GhcModiManager.findInfoFor(element.getContainingFile, element.asInstanceOf[HaskellNamedElement])
  }

  override def canProcessElement(element: PsiElement): Boolean = element.isInstanceOf[HaskellNamedElement]

  override def forcesShowPreview(): Boolean = {
    ghcModInfo.nonEmpty
  }

  override def findReferences(element: PsiElement): java.util.Collection[PsiReference] = {
    element match {
      case ne: HaskellNamedElement => findReferencesForNamedElement(ne)
      case _ => Iterable()
    }
  }

  private def findReferencesForNamedElement(element: HaskellNamedElement): Iterable[PsiReference] = {
    val expressionParent = Option(PsiTreeUtil.getParentOfType(element, classOf[HaskellExpression]))
    (ghcModInfo, expressionParent) match {
      case (Seq(), Some(p)) => ReferencesSearch.search(element, new LocalSearchScope(p)).findAll()
      case (_, _) => ReferencesSearch.search(element, element.getUseScope).findAll()
    }
  }
}
