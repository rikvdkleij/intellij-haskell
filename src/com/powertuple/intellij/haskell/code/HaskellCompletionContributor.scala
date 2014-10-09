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
  private final val SpecialReservedIds = Seq("forall", "safe", "unsafe")
  private final val PragmaIds = Seq("{-# ", "LANGUAGE ", "#-}", "{-# LANGUAGE ")
  private final val InsideImportClauses = Seq("as ", "hiding ", "qualified ")

  extend(CompletionType.BASIC, PlatformPatterns.psiElement(), new CompletionProvider[CompletionParameters] {
    def addCompletions(parameters: CompletionParameters, context: ProcessingContext, resultSet: CompletionResultSet) {

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
          resultSet.addAllElements(getLanguageExtensions(project))
          resultSet.addAllElements(getPragmaIds)
        case _ =>
          resultSet.addAllElements(getReservedIds)
          resultSet.addAllElements(getSpecialReservedIds)
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
    caretElement match {
      case Some(e) if e.getText.trim.size > 0 => context.setDummyIdentifier(e.getText.substring(0, 1))
      case _ => context.setDummyIdentifier("a")
    }
  }

  private def isImportSpecInProgress(position: PsiElement): Boolean = {
    Option(TreeUtil.findParent(position.getNode, HaskellTypes.HS_IMPORT_SPEC)).isDefined
  }

  private def findIdsForInImportModuleSpec(project: Project, position: PsiElement): Seq[LookupElementBuilder] = {
    (for {
      importDeclaration <- Option(TreeUtil.findParent(position.getNode, HaskellTypes.HS_IMPORT_DECLARATION))
      moduleName <- Option(PsiTreeUtil.findChildOfType(importDeclaration.getPsi, classOf[HaskellImportModule])).map(_.getQconId.getName)
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

  private def getLanguageExtensions(project: Project) = {
    GhcMod.listLanguageExtensions(project).map(n => LookupElementBuilder.create(n).withIcon(HaskellIcons.HaskellSmallBlueLogo).withTailText(" language extension", true))
  }

  private def getPragmaIds: Seq[LookupElementBuilder] = {
    PragmaIds.map(p => LookupElementBuilder.create(p).withIcon(HaskellIcons.HaskellSmallBlueLogo).withTailText(" pragma", true))
  }

  private def getImportedModulesWithFullScope(psiFile: PsiFile): Iterable[ImportFullSpec] = {
    val importDeclarations = findImportDeclarations(psiFile)
    val moduleNames = importDeclarations.filter(i => Option(i.getImportSpec).isEmpty).
        map(i => ImportFullSpec(i.getModuleName, Option(i.getImportQualified).isDefined, Option(i.getImportQualifiedAs).map(_.getQconId).map(_.getName)))
    Iterable(ImportFullSpec("Prelude", qualified = false, None)) ++ moduleNames
  }

  private def getImportedModulesWithHidingIdsSpec(psiFile: PsiFile): Iterable[ImportHidingIdsSpec] = {
    val importDeclarations = findImportDeclarations(psiFile)
    for {
      importDeclaration <- importDeclarations.filter(i => Option(i.getImportSpec).flatMap(is => Option(is.getImportHidingSpec)).isDefined)
      importId <- importDeclaration.getImportSpec.getImportHidingSpec.getImportIdList
      v = Option(importId.getQvar).map(_.getName).toSeq
      c = Option(importId.getQcon).map(_.getName).toSeq
    } yield ImportHidingIdsSpec(
      importDeclaration.getModuleName,
      v ++ c,
      Option(importDeclaration.getImportQualified).isDefined,
      Option(importDeclaration.getImportQualifiedAs).map(_.getQconId).map(_.getName)
    )
  }

  private def getImportedModulesWithSpecIds(psiFile: PsiFile): Iterable[ImportIdsSpec] = {
    val importDeclarations = findImportDeclarations(psiFile)
    for {
      importDeclaration <- importDeclarations.filter(i => Option(i.getImportSpec).flatMap(is => Option(is.getImportIdsSpec)).isDefined)
      importId <- importDeclaration.getImportSpec.getImportIdsSpec.getImportIdList
      v = Option(importId.getQvar).map(_.getName).toSeq
      c = Option(importId.getQcon).map(_.getName).toSeq
    } yield ImportIdsSpec(
      importDeclaration.getModuleName,
      v ++ c, Option(importDeclaration.getImportQualified).isDefined,
      Option(importDeclaration.getImportQualifiedAs).map(_.getQconId).map(_.getName)
    )
  }

  private def getIdsFromFullScopeImportedModules(project: Project, file: PsiFile) = {
    val importFullSpecs = getImportedModulesWithFullScope(file).toSeq
    for {
      ifs <- importFullSpecs
      bi <- GhcMod.browseInfo(project, ifs.moduleName, removeParensFromOperator = true)
      le <- createLookupElements(bi, ifs)
    } yield le
  }

  private def getIdsFromHidingIdsImportedModules(project: Project, file: PsiFile) = {
    val importHidingIdsSpec = getImportedModulesWithHidingIdsSpec(file).toSeq
    for {
      ihis <- importHidingIdsSpec
      bi <- GhcMod.browseInfo(project, ihis.moduleName, removeParensFromOperator = true)
      biInScope <- if (ihis.ids.contains(bi.name)) Seq() else Seq(bi)
      le <- createLookupElements(bi, ihis)
    } yield le
  }

  private def getIdsFromSpecIdsImportedModules(project: Project, file: PsiFile) = {
    val importIdsSpec = getImportedModulesWithSpecIds(file).toSeq
    for {
      iis <- importIdsSpec
      bi <- GhcMod.browseInfo(project, iis.moduleName, removeParensFromOperator = true)
      biInScope <- if (iis.ids.contains(bi.name)) Seq(bi) else Seq()
      le <- createLookupElements(bi, iis)
    } yield le
  }

  private def createLookupElements(browseInfo: BrowseInfo, importSpec: ImportSpec): Seq[LookupElementBuilder] = {
    if (importSpec.qualified)
      Seq(createQualifiedLookUpElementForBrowseInfo(browseInfo, importSpec.as))
    else
      Seq(createQualifiedLookUpElementForBrowseInfo(browseInfo, importSpec.as), createLookUpElementForBrowseInfo(browseInfo))
  }

  private def getReservedIds = {
    ReservedIds.map(r => LookupElementBuilder.create(r + " ").withIcon(HaskellIcons.HaskellSmallBlueLogo).withTailText(" keyword", true))
  }

  private def getSpecialReservedIds = {
    SpecialReservedIds.map(sr => LookupElementBuilder.create(sr + " ").withIcon(HaskellIcons.HaskellSmallBlueLogo).withTailText(" special keyword", true))
  }

  private def findImportDeclarations(psiFile: PsiFile) = {
    PsiTreeUtil.findChildrenOfType(psiFile, classOf[HaskellImportDeclaration])
  }

  private def createLookUpElementForBrowseInfo(browseInfo: BrowseInfo) = {
    val leb = LookupElementBuilder.create(browseInfo.name).withTailText(" " + browseInfo.moduleName, true).withIcon(findIcon(browseInfo))
    withTypeText(leb, browseInfo)
  }

  private def createQualifiedLookUpElementForBrowseInfo(browseInfo: BrowseInfo, as: Option[String]) = {
    val le = LookupElementBuilder.create(as.getOrElse(browseInfo.moduleName) + "." + browseInfo.name).withTailText(" " + browseInfo.moduleName, true).withIcon(findIcon(browseInfo))
    withTypeText(le, browseInfo)
  }

  private def withTypeText(lookupElement: LookupElementBuilder, browseInfo: BrowseInfo) = {
    browseInfo.declaration match {
      case Some(d) => lookupElement.withTypeText(d)
      case None => lookupElement
    }
  }

  private def findIcon(browseInfo: BrowseInfo) = {
    import com.powertuple.intellij.haskell.HaskellIcons._
    browseInfo.declaration match {
      case Some(d) if d.startsWith("class ") => Class
      case Some(d) if d.startsWith("data ") => Data
      case Some(d) if d.startsWith("default ") => Default
      case Some(d) if d.startsWith("foreign ") => Foreign
      case Some(d) if d.startsWith("instance ") => Instance
      case Some(d) if d.startsWith("new type ") => NewType
      case Some(d) if d.startsWith("type ") => Type
      case Some(d) if d.startsWith("type family ") => TypeFamily
      case Some(d) if d.startsWith("type instance ") => TypeInstance
      case _ => TypeSignature
    }
  }

  private sealed abstract class ImportSpec {
    def moduleName: String

    def qualified: Boolean

    def as: Option[String]
  }

  private case class ImportFullSpec(moduleName: String, qualified: Boolean, as: Option[String]) extends ImportSpec

  private case class ImportHidingIdsSpec(moduleName: String, ids: Seq[String], qualified: Boolean, as: Option[String]) extends ImportSpec

  private case class ImportIdsSpec(moduleName: String, ids: Seq[String], qualified: Boolean, as: Option[String]) extends ImportSpec

}
