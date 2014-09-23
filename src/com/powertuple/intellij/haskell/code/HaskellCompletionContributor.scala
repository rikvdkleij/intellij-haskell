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

package com.powertuple.intellij.haskell.code

import com.intellij.codeInsight.completion._
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.util.Condition
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.impl.source.tree.TreeUtil
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.{PsiElement, PsiFile}
import com.intellij.util.ProcessingContext
import com.powertuple.intellij.haskell.external.GhcMod
import com.powertuple.intellij.haskell.psi._
import com.powertuple.intellij.haskell.{HaskellIcons, HaskellParserDefinition}

import scala.collection.JavaConversions._

class HaskellCompletionContributor extends CompletionContributor {

  private final val ReservedIdNames = HaskellParserDefinition.ALL_RESERVED_IDS.getTypes.map(_.asInstanceOf[HaskellTokenType].getName)

  extend(CompletionType.BASIC, PlatformPatterns.psiElement(), new CompletionProvider[CompletionParameters] {
    def addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
      val project = parameters.getPosition.getProject

      val position = Option(parameters.getOriginalPosition).orElse(Option(parameters.getPosition))

      position match {
        case Some(p) if isModuleDeclarationInProgress(p) => result.addAllElements(GhcMod.listAvailableModules(project).map(LookupElementBuilder.create))
        case _ => {
          ReservedIdNames.foreach(k => result.addElement(LookupElementBuilder.create(k).
              withIcon(HaskellIcons.HaskellSmallLogo).withTypeText("keyword")))
          val browseInfo = GhcMod.browseInfo(project, getImportedModuleNames(parameters.getOriginalFile))
          browseInfo.foreach(bi => result.addElement(LookupElementBuilder.create(bi.name).withTypeText(bi.typeSignature).withTailText(" " + bi.moduleName, true).withIcon(HaskellIcons.HaskellSmallLogo)))
        }
      }
    }
  })

  override def beforeCompletion(context: CompletionInitializationContext): Unit = {
    val file = context.getFile
    val startOffset = context.getStartOffset
    val caretElement = Option(file.findElementAt(startOffset - 1))
    context.setDummyIdentifier(caretElement.map(_.getText.headOption).map(_.toString).getOrElse("a"))
  }

  def isModuleDeclarationInProgress(position: PsiElement): Boolean = {
    Option(TreeUtil.findSiblingBackward(position.getNode, HaskellTypes.HS_IMPORT)).
        orElse(Option(PsiTreeUtil.findFirstParent(position, importDeclarationCondition))).isDefined ||
        position.getPrevSibling.isInstanceOf[HaskellImportDeclaration]
  }

  private def getImportedModuleNames(psiFile: PsiFile): Seq[String] = {
    Seq("Prelude") ++ (
        Option(PsiTreeUtil.findChildrenOfType(psiFile, classOf[HaskellImportDeclaration])) match {
          case None => Seq()
          case Some(m) => m.map(_.getModuleName).toSeq
        })
  }

  private final val importDeclarationCondition = new Condition[PsiElement]() {
    override def value(psiElement: PsiElement): Boolean = {
      psiElement match {
        case _: HaskellImportDeclaration => true
        case _ => false
      }
    }
  }
}
