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

package intellij.haskell.navigation

import com.intellij.openapi.application.QueryExecutorBase
import com.intellij.psi.PsiElement
import com.intellij.util.Processor
import intellij.haskell.psi.HaskellPsiUtil

class InstanceDeclarationsSearch extends QueryExecutorBase[PsiElement, PsiElement](true) {
  override def processQuery(sourceElement: PsiElement, consumer: Processor[PsiElement]): Unit = {
    sourceElement.getReference match {
      case hr: HaskellReference =>
        val namedElement = hr.getElement
        val resolvedElements = HaskellReference.resolveResults(namedElement, namedElement.getContainingFile, namedElement.getProject).map(_.getElement).distinct
        val identifierElements = Option(namedElement.getReference).flatMap(e => HaskellPsiUtil.findHighestDeclarationElementParent(e.getElement)).map(p => p.getIdentifierElements).getOrElse(Iterable()).toSeq
        resolvedElements.diff(identifierElements).foreach(consumer.process)
      case _ => ()
    }
  }
}
