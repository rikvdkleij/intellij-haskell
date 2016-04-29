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

package intellij.haskell.code

import java.util.concurrent.{Executors, TimeUnit}

import com.intellij.codeInsight.completion._
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.impl.source.tree.TreeUtil
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.{PsiElement, PsiFile}
import com.intellij.util.ProcessingContext
import intellij.haskell.external.{BrowseInfo, GhcModBrowseInfo, GhcModHaskellInfo, GhcModModulesInfo}
import intellij.haskell.psi.HaskellTypes._
import intellij.haskell.psi._
import intellij.haskell.util.{HaskellElementCondition, OSUtil}
import intellij.haskell.{HaskellIcons, HaskellParserDefinition}

import scala.annotation.tailrec
import scala.collection.JavaConversions._
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

class HaskellCompletionContributor extends CompletionContributor {

  private final val ExecutorService = Executors.newCachedThreadPool()
  implicit private final val ExecContext = ExecutionContext.fromExecutorService(ExecutorService)

  private final val ReservedIds = HaskellParserDefinition.ALL_RESERVED_IDS.getTypes.map(_.asInstanceOf[HaskellTokenType].getName).toSeq
  private final val SpecialReservedIds = Seq("forall", "safe", "unsafe")
  private final val PragmaStartEndIds = Seq("{-#", "#-}")
  private final val FileHeaderPragmaIds = Seq("LANGUAGE", "OPTIONS_HADDOCK", "INCLUDE", "OPTIONS", "OPTIONS_GHC", "ANN")
  private final val ModulePragmaIds = Seq("ANN", "DEPRECATED", "WARING", "INLINE", "NOINLINE", "NOTINLINE", "INLINABEL", "LINE", "RULES",
    "SPECIALIZE", "SPECIALISE", "MINIMAL", "SOURCE", "UNPACK", "NOUNPACK")
  private final val InsideImportClauses = Seq("as", "hiding", "qualified")
  private final val CommentIds = Seq("{-", "-}", "--")

  private final val PlainPrefixElementTypes = Seq(HS_COMMENT, HS_NCOMMENT, HS_PRAGMA_START, HS_PRAGMA_END)

  val provider = new CompletionProvider[CompletionParameters] {
    def addCompletions(parameters: CompletionParameters, context: ProcessingContext, originalResultSet: CompletionResultSet) {
      val project = parameters.getPosition.getProject
      val file = parameters.getOriginalFile

      val resultSet = getNonEmptyElement(parameters.getOriginalPosition).orElse(getNonEmptyElement(parameters.getPosition)) match {
        case Some(e) if PlainPrefixElementTypes.contains(e.getNode.getElementType) | e.getText.startsWith("{") | e.getText.startsWith("#") | e.getText.startsWith("-") => originalResultSet.withPrefixMatcher(new PlainPrefixMatcher(getTextAtCaret(parameters.getEditor, file)))
        case Some(e) if e.getNode.getElementType == HS_CONID_ID && getTextAtCaret(parameters.getEditor, file).endsWith(".") => originalResultSet.withPrefixMatcher(new PlainPrefixMatcher(getTextAtCaret(parameters.getEditor, file)))
        case _ => originalResultSet
      }

      val completionPosition = Option(parameters.getOriginalPosition).orElse(Option(parameters.getPosition))
      completionPosition match {
        case Some(p) if isFileHeaderPragmaInProgress(p) =>
          resultSet.addAllElements(getLanguageExtensions(project).toSeq)
          resultSet.addAllElements(getPragmaStartEndIds)
          resultSet.addAllElements(getFileHeaderPragmaIds)
        case Some(p) if isPragmaInProgress(p) =>
          resultSet.addAllElements(getModulePragmaIds)
          resultSet.addAllElements(getPragmaStartEndIds)
        case Some(p) if isImportSpecInProgress(p) =>
          resultSet.addAllElements(findIdsForInImportModuleSpec(project, p).toSeq)
        case Some(p) if isImportQualifiedAsInProgress(p) =>
          resultSet.addAllElements(Seq[LookupElementBuilder]())
        case Some(p) if isImportModuleDeclarationInProgress(p) =>
          resultSet.addAllElements(findModulesToImport(project).toSeq)
          resultSet.addAllElements(getInsideImportClauses)
        case Some(p) if isNCommentInProgress(p) =>
          resultSet.addAllElements(getPragmaStartEndIds)
          resultSet.addAllElements(getCommentIds)
        case Some(p) =>
          val importDeclarations = findImportDeclarations(file)
          getQualifiedIdentifierInProgress(p) match {
            case Some(qid) =>
              val fullScopeIds = getIdsFromFullScopeImportedModules(project, file, importDeclarations, Some(qid))
              val specIdsScopeIds = getIdsFromSpecIdsImportedModules(project, file, importDeclarations, Some(qid))
              resultSet.addAllElements(if (fullScopeIds.isEmpty) specIdsScopeIds else fullScopeIds)
            case _ =>
              resultSet.addAllElements(getReservedIds)
              resultSet.addAllElements(getSpecialReservedIds)
              resultSet.addAllElements(getPragmaStartEndIds)
              resultSet.addAllElements(getCommentIds)
              resultSet.addAllElements(getIdsFromFullScopeImportedModules(project, file, importDeclarations))
              resultSet.addAllElements(getIdsFromHidingIdsImportedModules(project, file, importDeclarations))
              resultSet.addAllElements(getIdsFromSpecIdsImportedModules(project, file, importDeclarations))
          }
        case _ => ()
      }
    }
  }

  Seq(HS_VARID_ID, HS_CONID_ID, HS_VARSYM_ID, HS_CONSYM_ID).foreach(t =>
    extend(CompletionType.BASIC, PlatformPatterns.psiElement().withElementType(t), provider)
  )

  private def getTextAtCaret(editor: Editor, file: PsiFile): String = {
    val caretOffset = editor.getCaretModel.getOffset
    getTextUntilNoChar(file.getText, if (caretOffset > 0) caretOffset - 1 else caretOffset, "")
  }

  @tailrec
  private def getTextUntilNoChar(fileText: String, offset: Int, text: String): String = {
    val c = fileText.charAt(offset)
    if (c < ' ' || c == OSUtil.LineSeparator || c == '\r') {
      text
    } else {
      if (offset > 0) {
        getTextUntilNoChar(fileText, offset - 1, c.toString + text)
      } else {
        c.toString + text
      }
    }
  }

  private def getNonEmptyElement(element: PsiElement) = {
    Option(element) match {
      case Some(s) if !s.getText.trim.isEmpty => Some(s)
      case _ => None
    }
  }

  private def isImportSpecInProgress(position: PsiElement): Boolean = {
    Option(PsiTreeUtil.findFirstParent(position, HaskellElementCondition.ImportSpecCondition)).isDefined
  }

  private def findIdsForInImportModuleSpec(project: Project, position: PsiElement) = {
    (for {
      importDeclaration <- Option(TreeUtil.findParent(position.getNode, HS_IMPORT_DECLARATION))
      moduleName <- Option(PsiTreeUtil.findChildOfType(importDeclaration.getPsi, classOf[HaskellImportModule])).map(_.getModId.getText)
    } yield GhcModBrowseInfo.browseInfo(project, moduleName, removeParensFromOperator = false)).map(_.map(createLookUpElementForBrowseInfo)).getOrElse(Seq())
  }

  private def isImportModuleDeclarationInProgress(position: PsiElement): Boolean = {
    Option(PsiTreeUtil.findFirstParent(position, HaskellElementCondition.ImportDeclarationCondition)).isDefined ||
      Option(TreeUtil.findSiblingBackward(position.getNode, HS_IMPORT)).isDefined
  }

  private def isImportQualifiedAsInProgress(position: PsiElement): Boolean = {
    Option(TreeUtil.findSiblingBackward(position.getNode, HS_IMPORT_QUALIFIED_AS)).isDefined
  }

  private def findModulesToImport(project: Project) = {
    GhcModModulesInfo.listAvailableModules(project).map(m => LookupElementBuilder.create(m).withTailText(" module", true))
  }

  private def getInsideImportClauses = {
    InsideImportClauses.map(c => LookupElementBuilder.create(c).withTailText(" clause", true))
  }

  private def isFileHeaderPragmaInProgress(position: PsiElement): Boolean = {
    Option(PsiTreeUtil.findFirstParent(position, HaskellElementCondition.FileHeaderCondition)).isDefined
  }

  private def isPragmaInProgress(position: PsiElement): Boolean = {
    Option(TreeUtil.findSiblingBackward(position.getNode, HS_PRAGMA_START)).isDefined
  }

  private def getQualifiedIdentifierInProgress(position: PsiElement): Option[String] = {
    val qualifiedElement = Option(PsiTreeUtil.findFirstParent(position, HaskellElementCondition.QualifiedElementCondition))
    qualifiedElement.map {
      case qe: HaskellQvarId => qe.getQualifier.getName
      case qe: HaskellQconId => qe.getQconIdQualifier.getText.init // init to remove trailing dot
      case qe: HaskellQvarSym => qe.getQualifier.getName
      case qe: HaskellQconSym => qe.getQualifier.getName
    }.orElse {
      val elementType = position.getNode.getElementType
      if (elementType == HS_DOT) {
        Option(position.getPrevSibling).flatMap(ps => {
          Option(PsiTreeUtil.findChildOfType(ps, classOf[HaskellQcon])).flatMap(_.getQualifier)
        })
      } else {
        None
      }
    }
  }

  private def isNCommentInProgress(position: PsiElement): Boolean = {
    position.getNode.getElementType == HS_NCOMMENT
  }

  private def getLanguageExtensions(project: Project) = {
    GhcModHaskellInfo.listLanguageExtensions(project).map(n => LookupElementBuilder.create(n).withIcon(HaskellIcons.HaskellSmallBlueLogo).withTailText(" language extension", true))
  }

  private def getPragmaStartEndIds = {
    PragmaStartEndIds.map(p => LookupElementBuilder.create(p).withIcon(HaskellIcons.HaskellSmallBlueLogo).withTailText(" pragma", true))
  }

  private def getFileHeaderPragmaIds = {
    FileHeaderPragmaIds.map(p => LookupElementBuilder.create(p).withIcon(HaskellIcons.HaskellSmallBlueLogo).withTailText(" pragma", true))
  }

  private def getModulePragmaIds = {
    ModulePragmaIds.map(p => LookupElementBuilder.create(p).withIcon(HaskellIcons.HaskellSmallBlueLogo).withTailText(" pragma", true))
  }

  private def getCommentIds = {
    CommentIds.map(p => LookupElementBuilder.create(p).withIcon(HaskellIcons.HaskellSmallBlueLogo).withTailText(" comment", true))
  }

  private def getImportedModulesWithFullScope(psiFile: PsiFile, importDeclarations: Iterable[HaskellImportDeclaration]): Iterable[ImportFullSpec] = {
    val moduleNames = importDeclarations.filter(i => Option(i.getImportSpec).isEmpty).
      map(i => ImportFullSpec(i.getModuleName, Option(i.getImportQualified).isDefined, Option(i.getImportQualifiedAs).map(_.getQualifier).map(_.getName)))
    if (importDeclarations.exists(_.getModuleName == "Prelude")) {
      moduleNames
    } else {
      Iterable(ImportFullSpec("Prelude", qualified = false, None)) ++ moduleNames
    }
  }

  private def getImportedModulesWithHidingIdsSpec(psiFile: PsiFile, importDeclarations: Iterable[HaskellImportDeclaration]): Iterable[ImportHidingIdsSpec] = {
    for {
      importDeclaration <- importDeclarations.filter(i => Option(i.getImportSpec).flatMap(is => Option(is.getImportHidingSpec)).isDefined)
      importIdList = importDeclaration.getImportSpec.getImportHidingSpec.getImportIdList
    } yield ImportHidingIdsSpec(
      importDeclaration.getModuleName,
      importIdList.map(id => Option(id.getQvar).map(_.getName).getOrElse(id.getQcon.getName)),
      Option(importDeclaration.getImportQualified).isDefined,
      Option(importDeclaration.getImportQualifiedAs).map(_.getQualifier).map(_.getName)
    )
  }

  private def getImportedModulesWithSpecIds(psiFile: PsiFile, importDeclarations: Iterable[HaskellImportDeclaration]): Iterable[ImportIdsSpec] = {
    for {
      importDeclaration <- importDeclarations.filter(i => Option(i.getImportSpec).flatMap(is => Option(is.getImportIdsSpec)).isDefined)
      importIdList = importDeclaration.getImportSpec.getImportIdsSpec.getImportIdList
    } yield ImportIdsSpec(
      importDeclaration.getModuleName,
      importIdList.map(id => Option(id.getQvar).map(_.getName).getOrElse(id.getQcon.getName)),
      Option(importDeclaration.getImportQualified).isDefined,
      Option(importDeclaration.getImportQualifiedAs).map(_.getQualifier).map(_.getName)
    )
  }

  private def getIdsFromFullScopeImportedModules(project: Project, file: PsiFile, importDeclarations: Iterable[HaskellImportDeclaration], qualifier: Option[String] = None) = {
    val importFullSpecs = getImportedModulesWithFullScope(file, importDeclarations).toSeq

    val browseInfosWithImportSpecFutures = importFullSpecs.
      map(ifs => Future {
        BrowseInfosForImportFullSpec(ifs, GhcModBrowseInfo.browseInfo(project, ifs.moduleName, removeParensFromOperator = true))
      }.map(bi => createLookupElements(bi.importSpec, bi.browseInfos, qualifier)))

    Await.result(Future.sequence(browseInfosWithImportSpecFutures), Duration.create(5, TimeUnit.SECONDS)).flatten
  }

  private def getIdsFromHidingIdsImportedModules(project: Project, file: PsiFile, importDeclarations: Iterable[HaskellImportDeclaration]) = {
    val importHidingIdsSpec = getImportedModulesWithHidingIdsSpec(file, importDeclarations).toSeq

    val browseInfosWithImportHidingIdsSpecFutures = importHidingIdsSpec.
      map(bi => Future {
        BrowseInfosForImportHidingIdsSpec(bi, GhcModBrowseInfo.browseInfo(project, bi.moduleName, removeParensFromOperator = true))
      }.map(bis => createLookupElements(bis.importSpec, bis.browseInfos.filterNot(bi => bis.importSpec.ids.contains(bi.name)), None)))

    Await.result(Future.sequence(browseInfosWithImportHidingIdsSpecFutures), Duration.create(5, TimeUnit.SECONDS)).flatten
  }

  private def getIdsFromSpecIdsImportedModules(project: Project, file: PsiFile, importDeclarations: Iterable[HaskellImportDeclaration], qualifier: Option[String] = None) = {
    val importIdsSpec = getImportedModulesWithSpecIds(file, importDeclarations).toSeq

    val browseInfosWithImportIdsSpecFutures = importIdsSpec.
      map(iis => Future {
        BrowseInfosForImportIdsSpec(iis, GhcModBrowseInfo.browseInfo(project, iis.moduleName, removeParensFromOperator = true))
      }.map(bis => createLookupElements(bis.importSpec, bis.browseInfos.filter(bi => bis.importSpec.ids.contains(bi.name)), qualifier)))

    Await.result(Future.sequence(browseInfosWithImportIdsSpecFutures), Duration.create(5, TimeUnit.SECONDS)).flatten
  }

  private def createLookupElements(importSpec: ImportSpec, browseInfos: Iterable[BrowseInfo], qualifier: Option[String]): Iterable[LookupElementBuilder] = {
    browseInfos.flatMap(bi => {
      (qualifier, importSpec.as) match {
        case (Some(q), Some(as)) if q == as => Iterable(createLookUpElementForBrowseInfo(bi))
        case (None, _) =>
          if (importSpec.qualified)
            Iterable(createQualifiedLookUpElementForBrowseInfo(bi, importSpec.as))
          else
            Iterable(createQualifiedLookUpElementForBrowseInfo(bi, importSpec.as), createLookUpElementForBrowseInfo(bi))
        case _ => Iterable()
      }
    })
  }

  private def getReservedIds = {
    ReservedIds.map(r => LookupElementBuilder.create(r).withIcon(HaskellIcons.HaskellSmallBlueLogo).withTailText(" keyword", true))
  }

  private def getSpecialReservedIds = {
    SpecialReservedIds.map(sr => LookupElementBuilder.create(sr).withIcon(HaskellIcons.HaskellSmallBlueLogo).withTailText(" special keyword", true))
  }

  private def findImportDeclarations(psiFile: PsiFile) = {
    HaskellPsiHelper.findImportDeclarations(psiFile)
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
    import intellij.haskell.HaskellIcons._
    browseInfo.declaration match {
      case Some(d) if d.startsWith("class ") => Class
      case Some(d) if d.startsWith("data ") => Data
      case Some(d) if d.startsWith("default ") => Default
      case Some(d) if d.startsWith("foreign ") => Foreign
      case Some(d) if d.startsWith("instance ") => Instance
      case Some(d) if d.startsWith("new type ") => NewType
      case Some(d) if d.startsWith("type family ") => TypeFamily
      case Some(d) if d.startsWith("type instance ") => TypeInstance
      case Some(d) if d.startsWith("type ") => Type
      case Some(d) if d.startsWith("module ") => Module
      case _ => TypeSignature
    }
  }

  private sealed abstract class ImportSpec {
    def moduleName: String

    def qualified: Boolean

    def as: Option[String]
  }

  private case class ImportFullSpec(moduleName: String, qualified: Boolean, as: Option[String]) extends ImportSpec

  private case class ImportHidingIdsSpec(moduleName: String, ids: Iterable[String], qualified: Boolean, as: Option[String]) extends ImportSpec

  private case class ImportIdsSpec(moduleName: String, ids: Iterable[String], qualified: Boolean, as: Option[String]) extends ImportSpec

  private case class BrowseInfosForImportFullSpec(importSpec: ImportSpec, browseInfos: Iterable[BrowseInfo])

  private case class BrowseInfosForImportHidingIdsSpec(importSpec: ImportHidingIdsSpec, browseInfos: Iterable[BrowseInfo])

  private case class BrowseInfosForImportIdsSpec(importSpec: ImportIdsSpec, browseInfos: Iterable[BrowseInfo])

}
