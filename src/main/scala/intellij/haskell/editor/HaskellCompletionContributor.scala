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

package intellij.haskell.editor

import java.util
import java.util.concurrent.{Executors, TimeUnit}

import com.intellij.codeInsight.completion._
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.text.StringUtil
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.impl.source.tree.TreeUtil
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.{PsiElement, PsiFile, TokenType}
import com.intellij.util.ProcessingContext
import intellij.haskell.external.component.{HaskellComponentsManager, ModuleIdentifier}
import intellij.haskell.psi.HaskellElementCondition._
import intellij.haskell.psi.HaskellPsiUtil._
import intellij.haskell.psi.HaskellTypes._
import intellij.haskell.psi._
import intellij.haskell.util.HaskellProjectUtil
import intellij.haskell.{HaskellIcons, HaskellParserDefinition}

import scala.collection.JavaConverters._
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future, TimeoutException}

class HaskellCompletionContributor extends CompletionContributor {

  private final val ExecutorService = Executors.newCachedThreadPool()
  implicit private final val ExecContext = ExecutionContext.fromExecutorService(ExecutorService)

  private final val HaskellWhere = Stream("where")
  private final val HaskellLet = Stream("let")
  private final val haskellDeclKeywords = Stream("family", "data", "type", "module", "class", "instance", "newtype", "deriving", "in")
  private final val HaskellDefault = Stream("default")
  private final val HaskellImportKeywords = Stream("import", "qualified", "as", "hiding")
  private final val HaskellForeignKeywords = Stream("foreign", "export", "ccall", "safe", "unsafe", "interruptible", "capi", "prim")
  private final val HaskellKeyword = Stream("do", "case", "of")
  private final val HaskellStatic = Stream("static")
  private final val HaskellConditional = Stream("if", "then", "else")
  private final val HaskellInfix = Stream("infix", "infixl", "infixr")
  private final val HaskellBottom = Stream("undefined", "error")
  private final val HaskellTodo = Stream("TODO", "FIXME")
  private final val HaskellTypeRoles = Stream("phantom", "representational", "nominal")
  private final val HaskellForall = Stream("forall")
  private final val HaskellRecursiveDo = Stream("mdo", "rec")
  private final val HaskellArrowSyntax = Stream("proc")
  private final val HaskellPatternKeyword = Stream("pattern")

  private final val Keywords = HaskellWhere ++ HaskellLet ++ haskellDeclKeywords ++ HaskellDefault ++ HaskellImportKeywords ++
    HaskellForeignKeywords ++ HaskellKeyword ++ HaskellStatic ++ HaskellConditional ++ HaskellInfix ++ HaskellBottom ++
    HaskellTodo ++ HaskellTypeRoles ++ HaskellForall ++ HaskellRecursiveDo ++ HaskellArrowSyntax ++ HaskellPatternKeyword

  private final val SpecialReservedIds = Stream("safe", "unsafe")
  private final val PragmaIds = Stream("{-#", "#-}")
  private final val FileHeaderPragmaIds = Stream("LANGUAGE", "OPTIONS_HADDOCK", "INCLUDE", "OPTIONS", "OPTIONS_GHC", "ANN")
  private final val ModulePragmaIds = Stream("ANN", "DEPRECATED", "WARING", "INLINE", "NOINLINE", "NOTINLINE", "INLINABEL", "LINE", "RULES",
    "SPECIALIZE", "SPECIALISE", "MINIMAL", "SOURCE", "UNPACK", "NOUNPACK")
  private final val InsideImportKeywords = Stream("as", "hiding", "qualified")
  private final val CommentIds = Stream("{-", "-}", "--")
  private final val HaddockIds = Stream("{-|", "-- |", "-- ^")

  def findQualifiedNamedElementToComplete(element: PsiElement): Option[HaskellQualifiedNameElement] = {
    val elementType = Option(element.getNode.getElementType)
    val psiFile = element.getContainingFile
    (for {
      et <- elementType
      if et == HS_DOT
      e <- Option(psiFile.findElementAt(element.getTextOffset - 1))
      p <- HaskellPsiUtil.findQualifiedNameElement(e)
    } yield p).orElse(for {
      et <- elementType
      if et == HS_NEWLINE || et == TokenType.WHITE_SPACE
      d <- Option(psiFile.findElementAt(element.getTextOffset - 1))
      if d.getNode.getElementType == HS_DOT
      e <- Option(psiFile.findElementAt(element.getTextOffset - 2))
      p <- HaskellPsiUtil.findQualifiedNameElement(e)
    } yield p).
      orElse(HaskellPsiUtil.findQualifiedNameElement(element))
  }

  val provider = new CompletionProvider[CompletionParameters] {
    def addCompletions(parameters: CompletionParameters, context: ProcessingContext, originalResultSet: CompletionResultSet) {

      val project = parameters.getPosition.getProject
      val psiFile = parameters.getOriginalFile

      if (HaskellProjectUtil.isLibraryFile(psiFile).getOrElse(true) || Option(parameters.getOriginalPosition).map(_.getNode.getElementType).exists(t => HaskellParserDefinition.Literals.contains(t) || HaskellParserDefinition.Comments.contains(t))) {
        return
      }

      // In case element before caret is a qualifier, module name or a dot we have to "help" IntelliJ to get the right preselected elements behavior
      // For example, asking for completion in case typing `Data.List.` will give without this help no prefix.
      val positionElement = Option(parameters.getOriginalPosition).orElse(Option(parameters.getPosition))
      val prefixText = (
        for {
          e <- positionElement
          p <- HaskellPsiUtil.findModIdElement(e)
          start = p.getTextRange.getStartOffset
          end = parameters.getOffset
        } yield psiFile.getText.substring(start, end)
        ).orElse({
        for {
          e <- positionElement
          p <- findQualifiedNamedElementToComplete(e)
          start = if (p.getText.startsWith("(")) p.getTextRange.getStartOffset + 1 else p.getTextRange.getStartOffset
          end = parameters.getOffset
        } yield psiFile.getText.substring(start, end)
      }).orElse(
        for {
          e <- positionElement
          if e.getNode.getElementType != HS_RIGHT_PAREN
          t <- Option(e.getText).filter(_.trim.nonEmpty)
        } yield t
      ).flatMap(pt => if (pt.trim.isEmpty) None else Some(pt))

      val resultSet = prefixText match {
        case Some(t) if !t.startsWith("`") => originalResultSet.withPrefixMatcher(originalResultSet.getPrefixMatcher.cloneWithPrefix(t))
        case _ => originalResultSet
      }

      positionElement match {
        case Some(e) if isPragmaInProgress(e) =>
          resultSet.addAllElements(getModulePragmaIds.asJavaCollection)
          resultSet.addAllElements(getPragmaStartEndIds.asJavaCollection)
        case Some(e) if isFileHeaderPragmaInProgress(e) =>
          resultSet.addAllElements(getLanguageExtensions(project).asJavaCollection)
          resultSet.addAllElements(getPragmaStartEndIds.asJavaCollection)
          resultSet.addAllElements(getFileHeaderPragmaIds.asJavaCollection)
        case Some(e) if isImportSpecInProgress(e) =>
          resultSet.addAllElements(findAvailableIdsForImportModuleSpec(project, e, psiFile).asJavaCollection)
        case Some(e) if isImportModuleDeclarationInProgress(e) =>
          // Do not give suggestions when defining import qualifier
          if (e.getParent.getNode.getElementType != HS_QUALIFIER) {
            resultSet.addAllElements(findAvailableModuleNames(project, psiFile).asJavaCollection)
            resultSet.addAllElements(getInsideImportClauses.asJavaCollection)
            resultSet.addElement(createKeywordLookupElement("import"))
          }
        case Some(e) if isNCommentInProgress(e) =>
          resultSet.addAllElements(getPragmaStartEndIds.asJavaCollection)
          resultSet.addAllElements(getCommentIds.asJavaCollection)
        case op =>
          val importDeclarations = findImportDeclarations(psiFile)

          resultSet.addAllElements(getReservedNames.asJavaCollection)
          resultSet.addAllElements(getSpecialReservedIds.asJavaCollection)
          resultSet.addAllElements(getPragmaStartEndIds.asJavaCollection)
          resultSet.addAllElements(getCommentIds.asJavaCollection)
          resultSet.addAllElements(getHaddockIds.asJavaCollection)
          resultSet.addAllElements(getIdsFromFullImportedModules(project, psiFile, importDeclarations).asJavaCollection)
          resultSet.addAllElements(getIdsFromHidingIdsImportedModules(project, psiFile, importDeclarations).asJavaCollection)
          resultSet.addAllElements(getIdsFromSpecIdsImportedModules(project, psiFile, importDeclarations).asJavaCollection)

          val moduleName = findModuleName(psiFile)
          val topLevelLookupElements = moduleName.map(mn => HaskellComponentsManager.findAllTopLevelModuleIdentifiers(project, psiFile, mn).map(mi => createTopLevelLookupElement(mi))).getOrElse(Iterable())
          if (topLevelLookupElements.isEmpty) {
            resultSet.addAllElements(findTopLevelLookupElements(psiFile).asJavaCollection)
          } else {
            resultSet.addAllElements(topLevelLookupElements.asJavaCollection)
            resultSet.addAllElements(findTopLevelTypeSignatureLookupElements(psiFile).asJavaCollection)
          }

          op match {
            case Some(e) =>
              val localElements = if (e.getText.trim.isEmpty)
                findLocalElements(e)
              else
                findLocalElements(e).filterNot(_.getText == e.getText)
              ApplicationManager.getApplication.invokeLater(() => localElements.foreach(HaskellComponentsManager.findTypeInfoForElement))
              val filteredLocalElements = prefixText match {
                case Some(pt) => localElements.filter(ne => ne.getName.startsWith(pt))
                case None if !parameters.isAutoPopup => localElements
                case _ => Stream()
              }
              resultSet.addAllElements(filteredLocalElements.map(createLocalLookupElement).asJavaCollection)
            case _ => ()
          }
      }
    }
  }

  extend(CompletionType.BASIC, PlatformPatterns.psiElement(), provider)

  override def beforeCompletion(context: CompletionInitializationContext): Unit = {
    val psiFile = context.getFile
    val contextElement = Option(psiFile.findElementAt(context.getStartOffset - 1))
    contextElement match {
      case None => context.setDummyIdentifier("a")
      case Some(ce) if ce.getNode.getElementType == HS_NEWLINE | ce.getNode.getElementType == HS_LEFT_PAREN => context.setDummyIdentifier("a")
      case Some(ce) =>
        HaskellPsiUtil.findModIdElement(ce) match {
          case Some(modid) => context.setDummyIdentifier(modid.getName)
          case _ => findQualifiedNamedElementToComplete(ce) match {
            case Some(qualifiedNameElement) =>
              context.setDummyIdentifier(qualifiedNameElement.getNameWithoutParens)
              context.setReplacementOffset(qualifiedNameElement.getTextRange.getEndOffset)
            case _ => ce.getText.trim match {
              case t if t.nonEmpty => context.setDummyIdentifier(t)
              case _ => context.setDummyIdentifier("a")
            }
          }
        }
    }
  }

  // Type signatures in case they exist without accompanying implementation
  private def findTopLevelTypeSignatureLookupElements(psiFile: PsiFile): Iterable[LookupElementBuilder] = {
    val typeSignatures = HaskellPsiUtil.findTopLevelTypeSignatures(psiFile)
    typeSignatures.flatMap(ts => ts.getIdentifierElements.map(e => createTopDeclarationLookupElement(e.getName, ts.getText)))
  }

  private def findTopLevelLookupElements(psiFile: PsiFile): Iterable[LookupElementBuilder] = {
    val topLevelDeclarations = HaskellPsiUtil.findTopLevelDeclarations(psiFile)
    topLevelDeclarations.flatMap(ts => ts.getIdentifierElements.map(e => createTopDeclarationLookupElement(e.getName, ts.getText)))
  }

  private def createLocalLookupElement(namedElement: HaskellNamedElement): LookupElementBuilder = {
    LookupElementBuilder.create(namedElement.getName).withTypeText(HaskellComponentsManager.findTypeInfoForElement(namedElement).map(ti => StringUtil.unescapeXml(ti.typeSignature)).getOrElse("")).withIcon(HaskellIcons.HaskellSmallBlueLogo)
  }

  private def createTopLevelLookupElement(moduleIdentifier: ModuleIdentifier): LookupElementBuilder = {
    LookupElementBuilder.create(moduleIdentifier.name).withTypeText(moduleIdentifier.declaration).withIcon(findIcon(moduleIdentifier))
  }

  private def createTopDeclarationLookupElement(name: String, declaration: String): LookupElementBuilder = {
    LookupElementBuilder.create(name).withTypeText(declaration).withIcon(HaskellIcons.HaskellSmallBlueLogo)
  }

  private def findLocalElements(element: PsiElement) = {
    HaskellPsiUtil.findExpressionParent(element).toStream.flatMap(e => HaskellPsiUtil.findNamedElements(e))
  }

  private def isImportSpecInProgress(element: PsiElement): Boolean = {
    Option(PsiTreeUtil.findFirstParent(element, ImportSpecCondition)).isDefined
  }

  private def isFileHeaderPragmaInProgress(element: PsiElement): Boolean = {
    Option(PsiTreeUtil.findFirstParent(element, FileHeaderCondition)).isDefined
  }

  private def isPragmaInProgress(element: PsiElement): Boolean = {
    Option(TreeUtil.findSiblingBackward(element.getNode, HS_PRAGMA_START)).isDefined
  }

  private def isImportModuleDeclarationInProgress(element: PsiElement): Boolean = {
    Option(PsiTreeUtil.findFirstParent(element, ImportDeclarationCondition)).isDefined ||
      Option(TreeUtil.findSiblingBackward(element.getNode, HS_IMPORT)).isDefined ||
      isImportIdInProgressInsideImportModuleDeclaration(element)
  }

  private def isImportIdInProgressInsideImportModuleDeclaration(element: PsiElement): Boolean = {
    val prevNode = Option(TreeUtil.prevLeaf(element.getNode))
    prevNode.exists(node => Option(PsiTreeUtil.findFirstParent(node.getPsi, ImportDeclarationCondition)).isDefined)
  }

  private def isNCommentInProgress(element: PsiElement): Boolean = {
    element.getNode.getElementType == HS_NCOMMENT
  }

  private def findAvailableIdsForImportModuleSpec(project: Project, element: PsiElement, psiFile: PsiFile) = {
    (for {
      moduleName <- HaskellPsiUtil.findImportDeclarationParent(element).flatMap(_.getModuleName)
    } yield HaskellComponentsManager.findImportedModuleIdentifiers(project, moduleName).map(m => createLookupElement(m, addParens = true))).getOrElse(Stream())
  }

  private def findAvailableModuleNames(project: Project, psiFile: PsiFile) = {
    val moduleNames = HaskellComponentsManager.findAvailableModuleNames(psiFile)
    moduleNames.map(m => LookupElementBuilder.create(m).withTailText(" module", true))
  }

  private def getInsideImportClauses = {
    InsideImportKeywords.map(c => LookupElementBuilder.create(c).withTailText(" clause", true))
  }

  private def getLanguageExtensions(project: Project) = {
    HaskellComponentsManager.findGlobalProjectInfo(project).map(_.languageExtensions.map(n => LookupElementBuilder.create(n).withIcon(HaskellIcons.HaskellSmallBlueLogo).withTailText(" language extension", true))).getOrElse(Iterable())
  }

  private def getPragmaStartEndIds = {
    PragmaIds.map(p => LookupElementBuilder.create(p).withIcon(HaskellIcons.HaskellSmallBlueLogo).withTailText(" pragma", true))
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

  private def getHaddockIds = {
    HaddockIds.map(p => LookupElementBuilder.create(p).withIcon(HaskellIcons.HaskellSmallBlueLogo).withTailText(" haddock", true))
  }

  private def getFullImportedModules(psiFile: PsiFile, importDeclarations: Iterable[HaskellImportDeclaration]): Iterable[ImportFull] = {
    val moduleNames = for {
      id <- importDeclarations
      if Option(id.getImportSpec).isEmpty
      mn <- id.getModuleName
    } yield ImportFull(mn, Option(id.getImportQualified).isDefined, Option(id.getImportQualifiedAs).map(_.getQualifier.getName))

    if (importDeclarations.exists(_.getModuleName == HaskellProjectUtil.Prelude) || isNoImplicitPreludeActive(psiFile)) {
      moduleNames
    } else {
      Iterable(ImportFull(HaskellProjectUtil.Prelude, qualified = false, None)) ++ moduleNames
    }
  }

  private def isNoImplicitPreludeActive(psiFile: PsiFile): Boolean = {
    HaskellComponentsManager.findGlobalProjectInfo(psiFile.getProject).exists(_.noImplicitPreludeActive) ||
      HaskellPsiUtil.findLanguageExtensions(psiFile).exists(_.getQNameList.asScala.exists(_.getName == "NoImplicitPrelude"))
  }

  private def findImportIds(importIdList: util.List[HaskellImportId]): Iterable[String] = {
    importIdList.asScala.flatMap(ii => Iterable(ii.getCname.getName) ++ ii.getCnameDotDotList.asScala.flatMap(cndd => Option(cndd.getCname).map(_.getName)))
  }

  private def getImportedModulesWithHidingIdsSpec(psiFile: PsiFile, importDeclarations: Iterable[HaskellImportDeclaration]): Iterable[ImportWithHiding] = {
    for {
      importDeclaration <- importDeclarations.filter(i => Option(i.getImportSpec).flatMap(is => Option(is.getImportHidingSpec)).isDefined)
      importIdList = importDeclaration.getImportSpec.getImportHidingSpec.getImportIdList
      mn <- importDeclaration.getModuleName
    } yield ImportWithHiding(
      mn,
      findImportIds(importIdList),
      Option(importDeclaration.getImportQualified).isDefined,
      Option(importDeclaration.getImportQualifiedAs).map(_.getQualifier).map(_.getName)
    )
  }

  private def getImportedModulesWithSpecIds(psiFile: PsiFile, importDeclarations: Iterable[HaskellImportDeclaration]): Iterable[ImportWithIds] = {
    for {
      importDeclaration <- importDeclarations.filter(i => Option(i.getImportSpec).flatMap(is => Option(is.getImportIdsSpec)).isDefined)
      importIdList = importDeclaration.getImportSpec.getImportIdsSpec.getImportIdList
      mn <- importDeclaration.getModuleName
    } yield ImportWithIds(
      mn,
      findImportIds(importIdList),
      Option(importDeclaration.getImportQualified).isDefined,
      Option(importDeclaration.getImportQualifiedAs).map(_.getQualifier).map(_.getName)
    )
  }

  private final val Timeout = Duration.create(1, TimeUnit.SECONDS)

  private def getIdsFromFullImportedModules(project: Project, psiFile: PsiFile, importDeclarations: Iterable[HaskellImportDeclaration]) = {
    val importInfos = getFullImportedModules(psiFile, importDeclarations)

    val lookupElements = importInfos.map(importInfo => Future {
      val moduleIdentifiers = HaskellComponentsManager.findImportedModuleIdentifiers(project, importInfo.moduleName)
      createLookupElements(importInfo, moduleIdentifiers)
    })
    waitForResult(lookupElements)
  }

  private def getIdsFromHidingIdsImportedModules(project: Project, psiFile: PsiFile, importDeclarations: Iterable[HaskellImportDeclaration]) = {
    val importInfos = getImportedModulesWithHidingIdsSpec(psiFile, importDeclarations)

    val lookupElements = importInfos.map(importInfo => Future {
      val moduleIdentifiers = HaskellComponentsManager.findImportedModuleIdentifiers(project, importInfo.moduleName)
      createLookupElements(importInfo, moduleIdentifiers.filterNot(mi => importInfo.ids.exists(_ == mi.name)))
    })
    waitForResult(lookupElements)
  }

  private def getIdsFromSpecIdsImportedModules(project: Project, psiFile: PsiFile, importDeclarations: Iterable[HaskellImportDeclaration]) = {
    val importInfos = getImportedModulesWithSpecIds(psiFile, importDeclarations)

    val lookupElements = importInfos.map(importInfo => Future {
      val moduleIdentifiers = HaskellComponentsManager.findImportedModuleIdentifiers(project, importInfo.moduleName)
      createLookupElements(importInfo, moduleIdentifiers.filter(mi => importInfo.ids.exists(id => if (mi.isOperator) s"(${mi.name})" == id else id == mi.name)))
    })
    waitForResult(lookupElements)
  }

  private def waitForResult(lookupElements: Iterable[Future[Iterable[LookupElementBuilder]]]): Iterable[LookupElementBuilder] = {
    try {
      Await.result(Future.sequence(lookupElements), Timeout).flatten
    } catch {
      case _: TimeoutException => Iterable()
    }
  }

  private def createLookupElements(importInfo: ImportInfo, moduleIdentifiers: Iterable[ModuleIdentifier]): Iterable[LookupElementBuilder] = {
    moduleIdentifiers.flatMap(mi => {
      (importInfo.as, importInfo.qualified) match {
        case (None, false) => Iterable(createLookupElement(mi), createQualifiedLookUpElement(mi, mi.moduleName))
        case (None, true) => Iterable(createQualifiedLookUpElement(mi, mi.moduleName))
        case (Some(q), false) => Iterable(createLookupElement(mi), createQualifiedLookUpElement(mi, q))
        case (Some(q), true) => Iterable(createQualifiedLookUpElement(mi, q))
      }
    })
  }

  private def getReservedNames = {
    Keywords.map(createKeywordLookupElement)
  }

  private def createKeywordLookupElement(keyword: String) = {
    LookupElementBuilder.create(keyword).withIcon(HaskellIcons.HaskellSmallBlueLogo).withTailText(" keyword", true)
  }

  private def getSpecialReservedIds = {
    SpecialReservedIds.map(sr => LookupElementBuilder.create(sr).withIcon(HaskellIcons.HaskellSmallBlueLogo).withTailText(" special keyword", true))
  }

  private def createLookupElement(moduleIdentifier: ModuleIdentifier, addParens: Boolean = false) = {
    LookupElementBuilder.create(
      if (moduleIdentifier.isOperator && addParens)
        s"""(${moduleIdentifier.name})"""
      else
        moduleIdentifier.name
    ).withTailText(" " + moduleIdentifier.moduleName, true).
      withIcon(findIcon(moduleIdentifier)).
      withTypeText(moduleIdentifier.declaration)
  }

  private def createQualifiedLookUpElement(moduleIdentifier: ModuleIdentifier, qualifier: String) = {
    LookupElementBuilder.create(qualifier + "." + moduleIdentifier.name).
      withTailText(" " + moduleIdentifier.moduleName, true).
      withIcon(findIcon(moduleIdentifier)).
      withTypeText(moduleIdentifier.declaration)
  }

  private def findIcon(moduleIdentifier: ModuleIdentifier) = {
    import intellij.haskell.HaskellIcons._
    moduleIdentifier.declaration match {
      case d if d.startsWith("class ") => Class
      case d if d.startsWith("data ") => Data
      case d if d.startsWith("default ") => Default
      case d if d.startsWith("foreign ") => Foreign
      case d if d.startsWith("instance ") => Instance
      case d if d.startsWith("newtype ") => NewType
      case d if d.startsWith("type family ") => TypeFamily
      case d if d.startsWith("type instance ") => TypeInstance
      case d if d.startsWith("type ") => Type
      case d if d.startsWith("module ") => Module
      case _ => HaskellSmallBlueLogo
    }
  }

  private sealed trait ImportInfo {
    def moduleName: String

    def qualified: Boolean

    def as: Option[String]
  }

  private case class ImportFull(moduleName: String, qualified: Boolean, as: Option[String]) extends ImportInfo

  private case class ImportWithHiding(moduleName: String, ids: Iterable[String], qualified: Boolean, as: Option[String]) extends ImportInfo

  private case class ImportWithIds(moduleName: String, ids: Iterable[String], qualified: Boolean, as: Option[String]) extends ImportInfo

}
