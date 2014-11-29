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

import com.intellij.openapi.util.TextRange
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi._
import com.intellij.util.ProcessingContext
import com.powertuple.intellij.haskell.navigate.HaskellReference
import com.powertuple.intellij.haskell.psi._
import org.jetbrains.annotations.NotNull

class HaskellReferenceContributor extends PsiReferenceContributor {
  def registerReferenceProviders(registrar: PsiReferenceRegistrar) {
    registrar.registerReferenceProvider(PlatformPatterns.psiElement(classOf[HaskellNamedElement]), new PsiReferenceProvider {

      @NotNull
      def getReferencesByElement(@NotNull element: PsiElement, @NotNull context: ProcessingContext): Array[PsiReference] = {
        element match {
          case namedElement: HaskellNamedElement =>
            Array(new HaskellReference(namedElement, TextRange.from(0, element.getTextLength)))
          case _ =>
            PsiReference.EMPTY_ARRAY
        }
      }
    })
  }
}