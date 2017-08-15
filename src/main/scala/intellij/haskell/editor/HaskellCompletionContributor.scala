/*
 * Copyright 2014-2017 Rik van der Kleij
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
import com.intellij.openapi.progress.ProgressManager
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
import intellij.haskell.runconfig.console.{HaskellConsoleView, HaskellConsoleViewMap}
import intellij.haskell.util.HaskellProjectUtil
import intellij.haskell.{HaskellFile, HaskellIcons, HaskellParserDefinition}

import scala.collection.JavaConverters._
import scala.concurrent._
import scala.concurrent.duration.Duration

class HaskellCompletionContributor extends CompletionContributor {

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
      p <- HaskellPsiUtil.findQualifiedNameParent(e)
    } yield p).orElse(for {
      et <- elementType
      if et == HS_NEWLINE || et == TokenType.WHITE_SPACE
      d <- Option(psiFile.findElementAt(element.getTextOffset - 1))
      if d.getNode.getElementType == HS_DOT
      e <- Option(psiFile.findElementAt(element.getTextOffset - 2))
      p <- HaskellPsiUtil.findQualifiedNameParent(e)
    } yield p).
      orElse(HaskellPsiUtil.findQualifiedNameParent(element))
  }


  val provider = new CompletionProvider[CompletionParameters] {
    def addCompletions(parameters: CompletionParameters, context: ProcessingContext, originalResultSet: CompletionResultSet) {

      ProgressManager.checkCanceled()

      val psiFile: HaskellFile = parameters.getOriginalFile.asInstanceOf[HaskellFile]
      val project = parameters.getPosition.getProject

      if (!HaskellConsoleView.isConsoleFile(psiFile) && (HaskellProjectUtil.isLibraryFile(psiFile).getOrElse(true) || Option(parameters.getOriginalPosition).map(_.getNode.getElementType).exists(t => HaskellParserDefinition.Literals.contains(t) || HaskellParserDefinition.Comments.contains(t)))) {
        return
      }

      ProgressManager.checkCanceled()

      // In case element before caret is a qualifier, module name or a dot we have to "help" IntelliJ to get the right preselected elements behavior
      // For example, asking for completion in case typing `Data.List.` will give without this help no prefix.
      val positionElement = Option(parameters.getOriginalPosition).orElse(Option(parameters.getPosition))
      val prefixText = (
        for {
          e <- positionElement
          mie <- HaskellPsiUtil.findModIdElement(e)
          start = mie.getTextRange.getStartOffset
          end = parameters.getOffset
        } yield psiFile.getText.substring(start, end)
        ).orElse({
        for {
          e <- positionElement
          if e.getNode.getElementType != HS_LEFT_PAREN && e.getNode.getElementType != HS_BACKQUOTE
          qne <- findQualifiedNamedElementToComplete(e)
          start = if (qne.getText.startsWith("(") || qne.getText.startsWith("`")) qne.getTextRange.getStartOffset + 1 else qne.getTextRange.getStartOffset
          end = parameters.getOffset
        } yield psiFile.getText.substring(start, end)
      }).orElse(
        for {
          e <- positionElement
          if e.getNode.getElementType != HS_RIGHT_PAREN && e.getNode.getElementType != HS_BACKQUOTE
          t <- Option(e.getText).filter(_.trim.nonEmpty)
        } yield t
      ).flatMap(pt => if (pt.trim.isEmpty) None else Some(pt))

      val resultSet = prefixText match {
        case Some(t) => originalResultSet.withPrefixMatcher(originalResultSet.getPrefixMatcher.cloneWithPrefix(t))
        case _ => originalResultSet
      }

      ProgressManager.checkCanceled()

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
          ProgressManager.checkCanceled()

          resultSet.addAllElements(getReservedNames.asJavaCollection)
          resultSet.addAllElements(getSpecialReservedIds.asJavaCollection)
          resultSet.addAllElements(getPragmaStartEndIds.asJavaCollection)
          resultSet.addAllElements(getCommentIds.asJavaCollection)
          resultSet.addAllElements(getHaddockIds.asJavaCollection)
          ProgressManager.checkCanceled()

          ProgressManager.checkCanceled()

          val file = HaskellConsoleViewMap.consoleFileViews.get(psiFile.getName)

          val bla = file.getOrElse(psiFile)


          resultSet.addAllElements(getAvailableImportedLookupElements(bla).asJavaCollection)
          val moduleName = findModuleName(bla)

          ProgressManager.checkCanceled()

          val currentFileModuleIdentifiers = moduleName.map(mn => HaskellComponentsManager.findExportedModuleIdentifiersOfCurrentFile(bla, mn))
          ProgressManager.checkCanceled()
          currentFileModuleIdentifiers.foreach(moduleIdentifiers => {
            val lookupElements = moduleIdentifiers.map(createTopLevelLookupElement)
            ProgressManager.checkCanceled()
            resultSet.addAllElements(lookupElements.asJavaCollection)
            ProgressManager.checkCanceled()
          })


          ProgressManager.checkCanceled()

          if (!HaskellConsoleView.isConsoleFile(psiFile)) {
            resultSet.addAllElements(findOtherLookupElements(psiFile, currentFileModuleIdentifiers.getOrElse(Iterable())).asJavaCollection)
            op.foreach(element => {
              val localElements = HaskellPsiUtil.findNamedElement(element) match {
                case Some(ne) => findLocalElements(element).filterNot(_ == ne)
                case None => findLocalElements(element)
              }
              resultSet.addAllElements(localElements.map(createLocalLookupElement).asJavaCollection)
            })
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
              context.setDummyIdentifier(qualifiedNameElement.getName)
              val endOffset = if (qualifiedNameElement.getText.endsWith("`") || qualifiedNameElement.getText.endsWith(")")) {
                qualifiedNameElement.getTextRange.getEndOffset - 1
              } else {
                qualifiedNameElement.getTextRange.getEndOffset
              }
              context.setReplacementOffset(endOffset)
            case _ => ce.getText.trim match {
              case t if t.nonEmpty => context.setDummyIdentifier(t)
              case _ => context.setDummyIdentifier("a")
            }
          }
        }
    }
  }

  import HaskellCompletionContributor._


  private def findOtherLookupElements(haskellFile: HaskellFile, currentModuleIdentifiers: Iterable[ModuleIdentifier]): Iterable[LookupElementBuilder] = {
    val currentNames = currentModuleIdentifiers.map(_.name)
    val topLevelDeclarations = HaskellPsiUtil.findHaskellDeclarationElements(haskellFile)
    topLevelDeclarations.flatMap(d => d.getIdentifierElements.filterNot(e => currentNames.exists(_ == e.getName)).map(e => createTopDeclarationLookupElement(e, d)))
  }

  private def createTopDeclarationLookupElement(namedElement: HaskellNamedElement, declarationElement: HaskellDeclarationElement): LookupElementBuilder = {
    val typeDeclaration = (if (namedElement.getText.trim.replace(namedElement.getName, "").isEmpty) {
      HaskellComponentsManager.findTypeInfoForElement(namedElement, forceGetInfo = false).map(_.typeSignature)
    } else {
      None
    }).getOrElse(declarationElement.getText.replaceAll("""\s+""", " "))
    LookupElementBuilder.create(namedElement.getName).withTypeText(typeDeclaration).withIcon(HaskellIcons.HaskellSmallBlueLogo)
  }

  private def createLocalLookupElement(namedElement: HaskellNamedElement): LookupElementBuilder = {
    LookupElementBuilder.create(namedElement.getName).withTypeText(HaskellComponentsManager.findTypeInfoForElement(namedElement, forceGetInfo = false).map(ti => StringUtil.unescapeXml(ti.typeSignature)).getOrElse("")).withIcon(HaskellIcons.HaskellSmallBlueLogo)
  }

  private def createTopLevelLookupElement(moduleIdentifier: ModuleIdentifier): LookupElementBuilder = {
    LookupElementBuilder.create(moduleIdentifier.name).withTypeText(moduleIdentifier.declaration).withIcon(findIcon(moduleIdentifier))
  }

  private def findLocalElements(element: PsiElement) = {
    HaskellPsiUtil.findExpressionParent(element).toStream.flatMap(e => HaskellPsiUtil.findNamedElements(e)).filter {
      case _: HaskellVarid => true
      case _: HaskellVarsym => true
      case _: HaskellConsym => true
      case _ => false
    }
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
    } yield HaskellComponentsManager.findExportedModuleIdentifiers(project, moduleName).map(m => createLookupElement(m, addParens = true))).getOrElse(Stream())
  }

  private def findAvailableModuleNames(project: Project, psiFile: PsiFile) = {
    val moduleNames = HaskellComponentsManager.findAvailableModuleNames(psiFile)
    moduleNames.map(m => LookupElementBuilder.create(m).withTailText(" module", true))
  }

  private def getInsideImportClauses = {
    InsideImportKeywords.map(c => LookupElementBuilder.create(c).withTailText(" clause", true))
  }

  private def getLanguageExtensions(project: Project) = {
    HaskellComponentsManager.getSupportedLanguageExtension(project).map(n => LookupElementBuilder.create(n).withIcon(HaskellIcons.HaskellSmallBlueLogo).withTailText(" language extension", true))
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

  private def getAvailableImportedLookupElements(psiFile: PsiFile) = {
    getAvailableImportedModuleIdentifiers(psiFile).map(mi => createLookupElement(mi))
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
    addWiths(LookupElementBuilder.create(
      if (moduleIdentifier.isOperator && addParens)
        s"""(${moduleIdentifier.name})"""
      else
        moduleIdentifier.name
    ), moduleIdentifier)
  }

  private def addWiths(lookupElementBuilder: LookupElementBuilder, moduleIdentifier: ModuleIdentifier) = {
    lookupElementBuilder.
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
}

object HaskellCompletionContributor {

  private final val Timeout = Duration.create(1, TimeUnit.SECONDS)

  private final val ExecutorService = Executors.newCachedThreadPool()
  implicit private final val ExecContext = ExecutionContext.fromExecutorService(ExecutorService)

  def getAvailableImportedModuleIdentifiers(psiFile: PsiFile): Iterable[ModuleIdentifier] = {
    val project = psiFile.getProject
    val importDeclarations = findImportDeclarations(psiFile)
    val moduleIdentifiers = getModuleIdentifiersFromFullImportedModules(project, psiFile, importDeclarations) ++ getModuleIdentifiersFromHidingIdsImportedModules(project, psiFile, importDeclarations) ++ getModuleIdentifiersFromSpecIdsImportedModules(project, psiFile, importDeclarations)
    waitForModuleIdentifiers(moduleIdentifiers)
  }

  private sealed trait ImportInfo {
    def moduleName: String

    def qualified: Boolean

    def as: Option[String]
  }

  private case class ImportFull(moduleName: String, qualified: Boolean, as: Option[String]) extends ImportInfo

  private case class ImportWithHiding(moduleName: String, ids: Iterable[String], qualified: Boolean, as: Option[String]) extends ImportInfo

  private case class ImportWithIds(moduleName: String, ids: Iterable[String], qualified: Boolean, as: Option[String]) extends ImportInfo

  private def isNoImplicitPreludeActive(psiFile: PsiFile): Boolean = {
    HaskellComponentsManager.findStackComponentGlobalInfo(psiFile).exists(_.noImplicitPreludeActive) ||
      HaskellPsiUtil.findLanguageExtensions(psiFile).exists(_.getQNameList.asScala.exists(_.getName == "NoImplicitPrelude"))
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

  private def getModuleIdentifiersFromFullImportedModules(project: Project, psiFile: PsiFile, importDeclarations: Iterable[HaskellImportDeclaration]) = {
    val importInfos = getFullImportedModules(psiFile, importDeclarations)

    importInfos.map(importInfo => Future {
      val mis = HaskellComponentsManager.findExportedModuleIdentifiers(project, importInfo.moduleName)
      createQualifiedModuleIdentifiers(importInfo, mis)
    })
  }

  private def getModuleIdentifiersFromHidingIdsImportedModules(project: Project, psiFile: PsiFile, importDeclarations: Iterable[HaskellImportDeclaration]) = {
    val importInfos = getImportedModulesWithHidingIdsSpec(psiFile, importDeclarations)

    importInfos.map(importInfo => Future {
      val moduleIdentifiers = HaskellComponentsManager.findExportedModuleIdentifiers(project, importInfo.moduleName)
      createQualifiedModuleIdentifiers(importInfo, moduleIdentifiers.filterNot(mi => importInfo.ids.exists(_ == mi.name)))
    })
  }

  private def getModuleIdentifiersFromSpecIdsImportedModules(project: Project, psiFile: PsiFile, importDeclarations: Iterable[HaskellImportDeclaration]) = {
    val importInfos = getImportedModulesWithSpecIds(psiFile, importDeclarations)

    importInfos.map(importInfo => Future {
      val moduleIdentifiers = HaskellComponentsManager.findExportedModuleIdentifiers(project, importInfo.moduleName)
      createQualifiedModuleIdentifiers(importInfo, moduleIdentifiers.filter(mi => importInfo.ids.exists(id => if (mi.isOperator) s"(${mi.name})" == id else id == mi.name)))
    })
  }

  private def waitForModuleIdentifiers(lookupElements: Iterable[Future[Iterable[ModuleIdentifier]]]): Iterable[ModuleIdentifier] = {
    try {
      Await.result(Future.sequence(lookupElements), Timeout).flatten
    } catch {
      case _: TimeoutException => Iterable()
    }
  }

  private def createQualifiedModuleIdentifiers(importInfo: ImportInfo, moduleIdentifiers: Iterable[ModuleIdentifier]): Iterable[ModuleIdentifier] = {
    moduleIdentifiers.flatMap(mi => {
      (importInfo.as, importInfo.qualified) match {
        case (None, false) => Iterable(mi, mi.copy(name = mi.moduleName + "." + mi.name))
        case (None, true) => Iterable(mi.copy(name = mi.moduleName + "." + mi.name))
        case (Some(q), false) => Iterable(mi, mi.copy(name = q + "." + mi.name))
        case (Some(q), true) => Iterable(mi.copy(name = q + "." + mi.name))
      }
    })
  }

  private def findImportIds(importIdList: util.List[HaskellImportId]): Iterable[String] = {
    importIdList.asScala.flatMap(importId => Iterable(importId.getCname.getName) ++ importId.getCnameDotDotList.asScala.flatMap(cndd => Option(cndd.getCname).map(_.getName)))
  }
}
