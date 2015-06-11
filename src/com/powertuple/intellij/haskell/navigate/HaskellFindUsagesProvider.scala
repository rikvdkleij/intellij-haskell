/*
 * Copyright 2015 Rik van der Kleij
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

package com.powertuple.intellij.haskell.navigate

import com.intellij.lang.cacheBuilder.{WordOccurrence, WordsScanner}
import com.intellij.lang.findUsages.FindUsagesProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.IElementType
import com.intellij.util.Processor
import com.powertuple.intellij.haskell.{HaskellFile, HaskellLexer}
import com.powertuple.intellij.haskell.psi.HaskellTypes._
import com.powertuple.intellij.haskell.psi._

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
      if (tokenType == HS_VARID_ID || tokenType == HS_CONID_ID || tokenType == HS_VARSYM_ID || tokenType == HS_CONSYM_ID || tokenType == HS_DOT) {
        val wo: WordOccurrence =
          if (tokenType == HS_VARSYM_ID && prevIdTokenType == HS_DOT) {
            new WordOccurrence(fileText, lexer.getTokenStart - 1, lexer.getTokenEnd, WordOccurrence.Kind.CODE)
          } else {
            new WordOccurrence(fileText, lexer.getTokenStart, lexer.getTokenEnd, WordOccurrence.Kind.CODE)
          }
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
      case HS_VAR_ID => "variable"
      case HS_QVAR_ID => "qualified variable"
      case HS_CON_ID => "constructor"
      case HS_QCON_ID => "qualified constructor"
      case HS_VAR_SYM => " variable operator"
      case HS_VAR_DOT_SYM => "variable operator"
      case HS_QVAR_SYM => "qualified variable operator"
      case HS_CON_SYM => "constructor operator"
      case HS_QCON_SYM => "qualified constructor operator"
      case HS_QUALIFIER => "qualifier"
      case HS_MOD_ID => "module"
      case _ => psiElement.getText
    }
  }

  override def getDescriptiveName(element: PsiElement): String = {
    val name = element match {
      case hv: HaskellNamedElement => hv.getName
      case hv: HaskellFile => hv.getName
      case _ => element.getText
    }
    Option(name) getOrElse "anonymous"
  }

  override def getHelpId(psiElement: PsiElement): String = null

  override def canFindUsagesFor(psiElement: PsiElement): Boolean = {
    psiElement match {
      case _: HaskellNamedElement => true
      case _ => false
    }
  }

  override def getNodeText(psiElement: PsiElement, useFullName: Boolean): String = {
    val text = psiElement match {
      case hv: HaskellNamedElement => hv.getName
      case _ => psiElement.getText
    }
    Option(text) getOrElse "anonymous"
  }
}
