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
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Condition
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.impl.source.tree.TreeUtil
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.{PsiElement, PsiFile}
import com.intellij.util.ProcessingContext
import com.powertuple.intellij.haskell.external.{BrowseInfo, GhcMod}
import com.powertuple.intellij.haskell.psi._
import com.powertuple.intellij.haskell.{HaskellIcons, HaskellParserDefinition}

import scala.collection.JavaConversions._

class HaskellCompletionContributor extends CompletionContributor {

  private final val ReservedIds = HaskellParserDefinition.ALL_RESERVED_IDS.getTypes.map(_.asInstanceOf[HaskellTokenType].getName).toSeq
  private final val PragmaIds = Seq("{-# ", "LANGUAGE ", "#-}", "{-# LANGUAGE ")
  private final val InsideImportClauses = Seq("as ", "hiding ", "qualified ")

  extend(CompletionType.BASIC, PlatformPatterns.psiElement(), new CompletionProvider[CompletionParameters] {
    def addCompletions(parameters: CompletionParameters, context: ProcessingContext, originalResultSet: CompletionResultSet) {

      // To get right completion behavior (especially for operators) we have to find the right prefix
      val originalPrefix = createPrefix(Option(parameters.getOriginalPosition))
      val currentPrefix = createPrefix(Option(parameters.getPosition))
      val prefix = if (originalPrefix.isEmpty) currentPrefix else originalPrefix
      val resultSet = if (originalResultSet.getPrefixMatcher.getPrefix.isEmpty && !currentPrefix.isEmpty && currentPrefix != "," && currentPrefix != "(") {
        originalResultSet.withPrefixMatcher(new PlainPrefixMatcher(prefix))
      } else {
        originalResultSet
      }

      val project = parameters.getPosition.getProject
      val file = parameters.getOriginalFile
      val position = Option(parameters.getOriginalPosition).orElse(Option(parameters.getPosition))
      position match {
        case Some(p) if isImportSpecInProgress(p) =>
          resultSet.addAllElements(findIdsForInImportModuleSpec(project, p))
        case Some(p) if isImportModuleDeclarationInProgress(p) =>
          resultSet.addAllElements(findModulesToImport(project))
          resultSet.addAllElements(getInsideImportClauses)
        case Some(p) if isPragmaInProgress(p) =>
          resultSet.addAllElements(getLanguageExtensionNames)
          resultSet.addAllElements(getPragmaIds)
        case _ =>
          resultSet.addAllElements(getReservedIds)
          resultSet.addAllElements(getPragmaIds)
          resultSet.addAllElements(getIdsFromFullScopeImportedModules(project, file))
          resultSet.addAllElements(getIdsFromHidingIdsImportedModules(project, file))
          resultSet.addAllElements(getIdsFromSpecIdsImportedModules(project, file))
      }
    }
  })

  override def beforeCompletion(context: CompletionInitializationContext) {
    val file = context.getFile
    val startOffset = context.getStartOffset
    val caretElement = Option(file.findElementAt(startOffset - 1))
    context.setDummyIdentifier(caretElement.map(_.getText).getOrElse("a"))
  }

  private def isImportSpecInProgress(position: PsiElement): Boolean = {
    Option(TreeUtil.findParent(position.getNode, HaskellTypes.HS_IMPORT_SPEC)).isDefined
  }

  private def findIdsForInImportModuleSpec(project: Project, position: PsiElement): Seq[LookupElementBuilder] = {
    (for {
      importDeclaration <- Option(TreeUtil.findParent(position.getNode, HaskellTypes.HS_IMPORT_DECLARATION))
      moduleName <- Option(PsiTreeUtil.findChildOfType(importDeclaration.getPsi, classOf[HaskellImportModule])).map(_.getQcon.getName)
    } yield GhcMod.browseInfo(project, Seq(moduleName), removeParensFromOperator = false)).map(_.map(createLookUpElementForBrowseInfo)).getOrElse(Seq())
  }

  private def isImportModuleDeclarationInProgress(position: PsiElement): Boolean = {
    Option(TreeUtil.findSiblingBackward(position.getNode, HaskellTypes.HS_IMPORT)).
        orElse(Option(TreeUtil.findParent(position.getNode, HaskellTypes.HS_IMPORT_DECLARATIONS))).isDefined
  }

  private def findModulesToImport(project: Project) = {
    GhcMod.listAvailableModules(project).map(m => LookupElementBuilder.create(m + " ").withTailText(" module", true))
  }

  private def getInsideImportClauses = {
    InsideImportClauses.map(c => LookupElementBuilder.create(c).withTailText(" clause", true))
  }

  private def isPragmaInProgress(position: PsiElement): Boolean = {
    Option(TreeUtil.findSiblingBackward(position.getNode, HaskellTypes.HS_PRAGMA_START)).
        orElse(Option(TreeUtil.findSibling(position.getNode, HaskellTypes.HS_PRAGMA_END))).
        orElse(Option(TreeUtil.findSiblingBackward(position.getParent.getNode, HaskellTypes.HS_PRAGMA_START))).isDefined
  }

  private def getLanguageExtensionNames = {
    LanguageExtensions.Names.map(n => LookupElementBuilder.create(n).withIcon(HaskellIcons.HaskellSmallBlueLogo))
  }

  private def getPragmaIds: Seq[LookupElementBuilder] = {
    PragmaIds.map(p => LookupElementBuilder.create(p).withIcon(HaskellIcons.HaskellSmallBlueLogo).withTailText(" pragma", true))
  }

  private def getFullScopeImportedModuleNames(psiFile: PsiFile): Seq[String] = {
    val importDeclarations = findImportDeclarations(psiFile)
    val moduleNames = importDeclarations.filter(i => Option(i.getImportSpec).isEmpty).map(_.getModuleName)
    Seq("Prelude") ++ moduleNames
  }

  private case class ImportHidingSpec(moduleName: String, ids: Seq[String])

  private def getModuleNamesWithHidingIdsSpec(psiFile: PsiFile): Iterable[ImportHidingSpec] = {
    val importDeclarations = findImportDeclarations(psiFile)
    for {
      importDeclaration <- importDeclarations.filter(i => Option(i.getImportSpec).flatMap(is => Option(is.getImportHidingSpec)).isDefined)
      importId <- importDeclaration.getImportSpec.getImportHidingSpec.getImportIdList
      v = Option(importId.getQvar).map(_.getName).toSeq
      c = Option(importId.getQcon).map(_.getName).toSeq
    } yield ImportHidingSpec(importDeclaration.getModuleName, v ++ c)
  }

  private case class ImportIdsSpec(moduleName: String, ids: Seq[String])

  private def getImportedModulesWithSpecIds(psiFile: PsiFile): Iterable[ImportIdsSpec] = {
    val importDeclarations = findImportDeclarations(psiFile)
    for {
      importDeclaration <- importDeclarations.filter(i => Option(i.getImportSpec).flatMap(is => Option(is.getImportIdsSpec)).isDefined)
      importId <- importDeclaration.getImportSpec.getImportIdsSpec.getImportIdList
      v = Option(importId.getQvar).map(_.getName).toSeq
      c = Option(importId.getQcon).map(_.getName).toSeq
    } yield ImportIdsSpec(importDeclaration.getModuleName, v ++ c)
  }

  private def getIdsFromSpecIdsImportedModules(project: Project, file: PsiFile) = {
    val browseInfos = for {
      idsSpec <- getImportedModulesWithSpecIds(file).toSeq
      bi <- GhcMod.browseInfo(project, Seq(idsSpec.moduleName), removeParensFromOperator = true)
      bisInScope <- if (idsSpec.ids.contains(bi.name)) Seq(bi) else Seq()
    } yield bisInScope

    browseInfos.map(createLookUpElementForBrowseInfo) ++ browseInfos.map(createQualifiedLookUpElementForBrowseInfo)
  }

  private def getIdsFromFullScopeImportedModules(project: Project, file: PsiFile) = {
    val fullScopeBrowseInfos = GhcMod.browseInfo(project, getFullScopeImportedModuleNames(file), removeParensFromOperator = true)

    fullScopeBrowseInfos.map(createLookUpElementForBrowseInfo) ++ fullScopeBrowseInfos.map(createQualifiedLookUpElementForBrowseInfo)
  }

  private def getIdsFromHidingIdsImportedModules(project: Project, file: PsiFile) = {
    val browseInfos = for {
      hidingIdsSpec <- getModuleNamesWithHidingIdsSpec(file).toSeq
      bi <- GhcMod.browseInfo(project, Seq(hidingIdsSpec.moduleName), removeParensFromOperator = true)
      bisInScope <- if (hidingIdsSpec.ids.contains(bi.name)) Seq() else Seq(bi)
    } yield bisInScope

    browseInfos.map(createLookUpElementForBrowseInfo) ++ browseInfos.map(createQualifiedLookUpElementForBrowseInfo)
  }

  private def getReservedIds = {
    ReservedIds.map(r => LookupElementBuilder.create(r + " ").withIcon(HaskellIcons.HaskellSmallBlueLogo).withTailText(" keyword", true))
  }

  private def findImportDeclarations(psiFile: PsiFile) = {
    PsiTreeUtil.findChildrenOfType(psiFile, classOf[HaskellImportDeclaration])
  }

  private def createLookUpElementForBrowseInfo(browseInfo: BrowseInfo) = {
    LookupElementBuilder.create(browseInfo.name).withTypeText(browseInfo.declaration).withTailText(" " + browseInfo.moduleName, true).withIcon(findIcon(browseInfo))
  }

  private def createQualifiedLookUpElementForBrowseInfo(browseInfo: BrowseInfo) = {
    LookupElementBuilder.create(browseInfo.moduleName + "." + browseInfo.name).withTypeText(browseInfo.declaration).withTailText(" " + browseInfo.moduleName, true).withIcon(findIcon(browseInfo))
  }

  private def createPrefix(position: Option[PsiElement]): String = {
    position match {
      case Some(p) => p.getText.trim.takeWhile(c => c != ' ' && c != '\n')
      case None => ""
    }
  }

  private def findIcon(browseInfo: BrowseInfo) = {
    import com.powertuple.intellij.haskell.HaskellIcons._
    browseInfo.declaration match {
      case d if d.startsWith("class ") => Class
      case d if d.startsWith("data ") => Data
      case d if d.startsWith("default ") => Default
      case d if d.startsWith("foreign ") => Foreign
      case d if d.startsWith("instance ") => Instance
      case d if d.startsWith("new type ") => NewType
      case d if d.startsWith("type ") => Type
      case d if d.startsWith("type family ") => TypeFamily
      case d if d.startsWith("type instance ") => TypeInstance
      case _ => TypeSignature
    }
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