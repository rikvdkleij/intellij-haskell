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

  private final val ReservedIds = HaskellParserDefinition.ALL_RESERVED_IDS.getTypes.map(_.asInstanceOf[HaskellTokenType].getName).toSeq
  private final val PragmaIds = Seq("{-#", "LANGUAGE", "#-}", "{-# LANGUAGE")

  extend(CompletionType.BASIC, PlatformPatterns.psiElement(), new CompletionProvider[CompletionParameters] {
    def addCompletions(parameters: CompletionParameters, context: ProcessingContext, originalResultSet: CompletionResultSet) {

      // To get right completion behavior (especially for operators) we have to find the right prefix
      val prefix1 = createPrefix(parameters.getOriginalPosition)
      val prefix2 = createPrefix(parameters.getPosition)
      val prefix = if (prefix1.isEmpty) prefix2 else prefix1
      val resultSet = if (originalResultSet.getPrefixMatcher.getPrefix.isEmpty && !prefix2.isEmpty && prefix2 != ",") {
        originalResultSet.withPrefixMatcher(new PlainPrefixMatcher(prefix))
      } else {
        originalResultSet
      }

      val project = parameters.getPosition.getProject
      val position = Option(parameters.getOriginalPosition).orElse(Option(parameters.getPosition))
      position match {
        case Some(p) if isModuleDeclarationInProgress(p) => resultSet.addAllElements(GhcMod.listAvailableModules(project).map(LookupElementBuilder.create))
        case Some(p) if isPragmaInProgress(p) =>
          resultSet.addAllElements(LanguageExtensions.Names.map(n => LookupElementBuilder.create(n).withIcon(HaskellIcons.HaskellSmallLogo)))
          addPragmaIdsToResultSet(resultSet)
        case _ =>
          ReservedIds.foreach(s => resultSet.addElement(LookupElementBuilder.create(s).withIcon(HaskellIcons.HaskellSmallLogo).withTypeText("keyword")))

          addPragmaIdsToResultSet(resultSet)

          val browseInfo = GhcMod.browseInfo(project, getImportedModuleNames(parameters.getOriginalFile))
          browseInfo.foreach(bi => resultSet.addElement(LookupElementBuilder.create(bi.name).withTypeText(bi.typeSignature).withTailText(" " + bi.moduleName, true).withIcon(HaskellIcons.HaskellSmallLogo)))
      }
    }
  })

  override def beforeCompletion(context: CompletionInitializationContext): Unit = {
    val file = context.getFile
    val startOffset = context.getStartOffset
    val caretElement = Option(file.findElementAt(startOffset - 1))
    context.setDummyIdentifier(caretElement.map(_.getText).getOrElse("a"))
  }

  private def createPrefix(position: PsiElement) = {
   position.getText.trim.takeWhile(c => c != ' ' && c != '\n')
  }

  private def addPragmaIdsToResultSet(resultSet: CompletionResultSet) = {
    PragmaIds.foreach(s => resultSet.addElement(LookupElementBuilder.create(s).withIcon(HaskellIcons.HaskellSmallLogo).withTypeText("pragma")))
  }

  private def isModuleDeclarationInProgress(position: PsiElement): Boolean = {
    Option(TreeUtil.findSiblingBackward(position.getNode, HaskellTypes.HS_IMPORT)).
        orElse(Option(PsiTreeUtil.findFirstParent(position, importDeclarationCondition))).isDefined ||
        position.getPrevSibling.isInstanceOf[HaskellImportDeclaration]
  }

  private def isPragmaInProgress(position: PsiElement): Boolean = {
    Option(TreeUtil.findSiblingBackward(position.getNode, HaskellTypes.HS_PRAGMA_START)).
        orElse(Option(TreeUtil.findSibling(position.getNode, HaskellTypes.HS_PRAGMA_END))).
        orElse(Option(TreeUtil.findSiblingBackward(position.getParent.getNode, HaskellTypes.HS_PRAGMA_START))).isDefined
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

object LanguageExtensions {

  final val Names = Seq(
    "AllowAmbiguousTypes",
    "Arrows",
    "AutoDeriveTypeable",
    "BangPatterns",
    "CApiFFI",
    "ConstrainedClassMethods",
    "ConstraintKinds",
    "CPP",
    "DataKinds",
    "DefaultSignatures",
    "DeriveDataTypeable",
    "DeriveFoldable",
    "DeriveFunctor",
    "DeriveGeneric",
    "DeriveTraversable",
    "DisambiguateRecordFields",
    "DoRec",
    "EmptyCase",
    "EmptyDataDecls",
    "ExistentialQuantification",
    "ExplicitForAll",
    "ExplicitNamespaces",
    "ExtendedDefaultRules",
    "FlexibleContexts",
    "FlexibleInstances",
    "ForeignFunctionInterface",
    "FunctionalDependencies",
    "GADTs",
    "GADTSyntax",
    "GeneralizedNewtypeDeriving",
    "Generics",
    "ImplicitParams",
    "ImpredicativeTypes",
    "InterruptibleFFI",
    "IncoherentInstances",
    "KindSignatures",
    "LambdaCase",
    "LiberalTypeSynonyms",
    "MagicHash",
    "MonadComprehensions",
    "MonoLocalBinds",
    "MultiParamTypeClasses",
    "MultiWayIf",
    "NamedFieldPuns",
    "NegativeLiterals",
    "NoImplicitPrelude",
    "NoMonoLocalBinds",
    "NoMonomorphismRestriction",
    "NoNPlusKPatterns",
    "NoTraditionalRecordSyntax",
    "NullaryTypeClasses",
    "NumDecimals",
    "OverlappingInstances",
    "OverloadedLists",
    "OverloadedStrings",
    "PackageImports",
    "ParallelArrays",
    "ParallelListComp",
    "PatternGuards",
    "PatternSynonyms",
    "PolyKinds",
    "PolymorphicComponents",
    "PostfixOperators",
    "QuasiQuotes",
    "Rank2Types",
    "RankNTypes",
    "RebindableSyntax",
    "RecordWildCards",
    "RecursiveDo",
    "RelaxedPolyRec",
    "Safe",
    "ScopedTypeVariables",
    "StandaloneDeriving",
    "TemplateHaskell",
    "TraditionalRecordSyntax",
    "TransformListComp",
    "Trustworthy",
    "TupleSections",
    "TypeFamilies",
    "TypeOperators",
    "TypeSynonymInstances",
    "UnboxedTuples",
    "UndecidableInstances",
    "UnicodeSyntax",
    "UnliftedFFITypes",
    "Unsafe",
    "ViewPatterns")


}