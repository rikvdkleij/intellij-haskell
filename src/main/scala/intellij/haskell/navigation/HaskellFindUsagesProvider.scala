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

import com.intellij.lang.cacheBuilder.{WordOccurrence, WordsScanner}
import com.intellij.lang.findUsages.FindUsagesProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.IElementType
import com.intellij.util.Processor
import intellij.haskell.psi.HaskellTypes._
import intellij.haskell.psi._
import intellij.haskell.{HaskellFile, HaskellLexer}

import scala.annotation.tailrec

class HaskellFindUsagesProvider extends FindUsagesProvider {

  override def getWordsScanner: WordsScanner = {
    new WordsScanner {
      def processWords(fileText: CharSequence, processor: Processor[WordOccurrence]) {
        val lexer = new HaskellLexer
        lexer.start(fileText)
        processTokens(lexer, fileText, processor, null)
      }
    }
  }

  @tailrec
  private def processTokens(lexer: HaskellLexer, fileText: CharSequence, processor: Processor[WordOccurrence], prevIdTokenType: IElementType) {
    val tokenType = lexer.getTokenType
    if (tokenType != null) {
      if (tokenType == HS_VAR_ID || tokenType == HS_CON_ID || tokenType == HS_VARSYM_ID || tokenType == HS_CONSYM_ID) {
        val wo: WordOccurrence = new WordOccurrence(fileText, lexer.getTokenStart, lexer.getTokenEnd, WordOccurrence.Kind.CODE)
        processor.process(wo)
        lexer.advance()
        processTokens(lexer, fileText, processor, tokenType)
      } else {
        lexer.advance()
        processTokens(lexer, fileText, processor, null)
      }
    }
  }

  override def getType(psiElement: PsiElement): String = {
    psiElement.getNode.getElementType match {
      case HS_VARID => "variable"
      case HS_CONID => "constructor"
      case HS_VARSYM => "variable operator"
      case HS_CONSYM => "constructor operator"
      case HS_QUALIFIER => "qualifier"
      case HS_MODID => "module"
      case _ => psiElement.getText
    }
  }

  override def getDescriptiveName(element: PsiElement): String = {
    element match {
      case ne: HaskellNamedElement => ne.getName
      case f: HaskellFile => f.getName
      case _ => element.getText
    }
  }

  override def getHelpId(psiElement: PsiElement): String = null

  override def canFindUsagesFor(psiElement: PsiElement): Boolean = {
    psiElement match {
      case _: HaskellNamedElement => true
      case _ => false
    }
  }

  override def getNodeText(psiElement: PsiElement, useFullName: Boolean): String = {
    psiElement match {
      case ne: HaskellNamedElement => ne.getName
      case _ => psiElement.getText
    }
  }
}
