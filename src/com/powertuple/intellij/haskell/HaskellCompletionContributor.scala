/*
 * Copyright 2014 Rik van der Kleij

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

import com.intellij.codeInsight.completion._
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.patterns.PlatformPatterns
import com.intellij.util.ProcessingContext
import com.powertuple.intellij.haskell.psi.HaskellTokenType

class HaskellCompletionContributor extends CompletionContributor {

  private final val ReservedIdNames = HaskellParserDefinition.RESERVED_IDS.getTypes.map(_.asInstanceOf[HaskellTokenType].getName)

  extend(CompletionType.BASIC, PlatformPatterns.psiElement(), new CompletionProvider[CompletionParameters] {
    def addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
      ReservedIdNames.foreach(k => result.addElement(LookupElementBuilder.create(k).
          withIcon(HaskellIcons.HASKELL_SMALL_LOGO).withTypeText("Reserved id")))
    }
  })

  override def beforeCompletion(context: CompletionInitializationContext): Unit = {
    context.setDummyIdentifier("a")
  }
}
