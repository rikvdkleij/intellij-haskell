/*
 * Copyright 2014-2019 Rik van der Kleij
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
import intellij.haskell.{HaskellFile, HaskellLexer, HaskellParserDefinition}

import scala.annotation.tailrec
import scala.collection.mutable.ListBuffer

class HaskellFindUsagesProvider extends FindUsagesProvider {

  override def getWordsScanner: WordsScanner = {
    (fileText: CharSequence, processor: Processor[WordOccurrence]) => {
      val lexer = new HaskellLexer
      lexer.start(fileText)
      processTokens(lexer, fileText, processor, ListBuffer.empty)
    }
  }

  @tailrec
  private def processTokens(lexer: HaskellLexer, fileText: CharSequence, processor: Processor[WordOccurrence], prevDots: ListBuffer[IElementType]): Unit = {
    val tokenType = lexer.getTokenType
    if (tokenType != null) {
      if (HaskellParserDefinition.Ids.contains(tokenType) || tokenType == HS_DOT) {
        if (tokenType == HS_DOT) {
          prevDots.+=(tokenType)
          lexer.advance()
          processTokens(lexer, fileText, processor, prevDots)
        } else {
          val text = if (tokenType == HS_VARSYM_ID || tokenType == HS_CONSYM_ID) {
            fileText.subSequence(lexer.getTokenStart - prevDots.length, lexer.getTokenEnd).toString
          } else {
            fileText.subSequence(lexer.getTokenStart, lexer.getTokenEnd).toString
          }

          // A workaround to get Find usages working for identifiers which contain single quotes
          val text1 = if (text.contains("'")) {
            text.replaceAll("'", "")
          } else {
            text
          }

          val wo = new WordOccurrence(text1, 0, text1.length, WordOccurrence.Kind.CODE)
          processor.process(wo)
          lexer.advance()
          processTokens(lexer, fileText, processor, ListBuffer.empty)
        }
      } else {
        if (prevDots.nonEmpty) {
          val wo = new WordOccurrence("." * prevDots.length, 0, prevDots.length, WordOccurrence.Kind.CODE)
          processor.process(wo)
        }

        lexer.advance()
        processTokens(lexer, fileText, processor, ListBuffer.empty)
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

  override def getDescriptiveName(psiElement: PsiElement): String = {
    psiElement match {
      case ne: HaskellNamedElement => ne.getName
      case f: HaskellFile => f.getName
      case _ => psiElement.getText
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
