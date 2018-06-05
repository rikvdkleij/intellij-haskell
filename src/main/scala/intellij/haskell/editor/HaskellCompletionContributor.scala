/*
 * Copyright 2014-2018 Rik van der Kleij
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
import java.util.concurrent.TimeUnit

import com.intellij.codeInsight.completion._
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.text.StringUtil
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.impl.source.tree.TreeUtil
import com.intellij.psi.search.GlobalSearchScope
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
import intellij.haskell.util.index.HaskellFilePathIndex
import intellij.haskell.{HaskellFile, HaskellIcons, HaskellNotificationGroup, HaskellParserDefinition}

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
  private final val Ann = "ANN"
  private final val FileHeaderPragmaIds = Stream("LANGUAGE", "OPTIONS_HADDOCK", "INCLUDE", "OPTIONS", "OPTIONS_GHC", Ann)
  private final val ModulePragmaIds = Stream(Ann, "DEPRECATED", "WARING", "INLINE", "INLINE_FUSED", "INLINE_INNER", "NOINLINE", "NOTINLINE", "INLINABEL", "LINE", "RULES",
    "SPECIALIZE", "SPECIALISE", "MINIMAL", "SOURCE", "UNPACK", "NOUNPACK", "OVERLAPPING", "OVERLAPPABLE", "OVERLAPS", "CONSTANT_FOLDED", "SCC", "INCOHERENT", "CFILES")
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


  val provider: CompletionProvider[CompletionParameters] = new CompletionProvider[CompletionParameters] {
    def addCompletions(parameters: CompletionParameters, context: ProcessingContext, originalResultSet: CompletionResultSet) {

      ProgressManager.checkCanceled()

      val psiFile: HaskellFile = parameters.getOriginalFile.asInstanceOf[HaskellFile]

      val project = parameters.getPosition.getProject

      val isConsoleFile = HaskellConsoleView.isConsoleFile(psiFile)
      if (!isConsoleFile && (HaskellProjectUtil.isLibraryFile(psiFile) || Option(parameters.getOriginalPosition).map(_.getNode.getElementType).exists(t => HaskellParserDefinition.Literals.contains(t) || HaskellParserDefinition.Comments.contains(t)))) {
        return
      }

      ProgressManager.checkCanceled()

      val positionElement = if (isConsoleFile) {
        None
      } else {
        // In case element before caret is a qualifier, module name or a dot we have to "help" IntelliJ to get the right preselected elements behavior
        // For example, asking for completion in case typing `Data.List.` will give without this help no prefix.
        Option(parameters.getOriginalPosition).orElse(Option(parameters.getPosition))
      }
      val prefixText = (
        for {
          e <- positionElement
          mie <- HaskellPsiUtil.findModIdElement(e)
          start = mie.getTextRange.getStartOffset
          end = parameters.getOffset
        } yield psiFile.getText.substring(start, end)
        ).orElse(
        for {
          e <- positionElement
          if isFileHeaderPragmaInProgress(e) || isPragmaInProgress(e)
          start = e.getTextRange.getStartOffset
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
        case Some(e) if isFileHeaderPragmaInProgress(e) =>
          resultSet.addAllElements(getLanguageExtensionsLookupElements(project).asJavaCollection)
          resultSet.addAllElements(getPragmaStartEndIdsLookupElements.asJavaCollection)
          resultSet.addAllElements(getFileHeaderPragmaIdsLookupElements.asJavaCollection)
        case Some(e) if isPragmaInProgress(e) =>
          resultSet.addAllElements(getModulePragmaIdsLookupElements.asJavaCollection)
          resultSet.addAllElements(getPragmaStartEndIdsLookupElements.asJavaCollection)
        case Some(e) if isImportSpecInProgress(e) =>
          resultSet.addAllElements(findAvailableIdsForImportModuleSpec(e, psiFile).asJavaCollection)
        case Some(e) if isImportModuleDeclarationInProgress(e) =>
          // Do not give suggestions when defining import qualifier
          if (e.getParent.getNode.getElementType != HS_QUALIFIER) {
            resultSet.addAllElements(findAvailableModuleNamesLookupElements(project, psiFile).asJavaCollection)
            resultSet.addAllElements(getInsideImportClausesLookupElements.asJavaCollection)
            resultSet.addElement(createKeywordLookupElement("import"))
          }
        case Some(e) if isNCommentInProgress(e) =>
          resultSet.addAllElements(getPragmaStartEndIdsLookupElements.asJavaCollection)
          resultSet.addAllElements(getCommentIdsLookupElements.asJavaCollection)
        case op =>
          ProgressManager.checkCanceled()

          // If file is console file, find the project file which corresponds to loaded file in console
          val projectFile =
            if (isConsoleFile) {
              val stackComponentInfo = HaskellComponentsManager.findStackComponentInfo(psiFile)
              stackComponentInfo.foreach(info => resultSet.addAllElements(HaskellComponentsManager.findAvailableProjectModuleNamesWithIndex(info).map(createModuleLookupElement).asJavaCollection))
              for {
                consoleInfo <- HaskellConsoleView.findConsoleInfo(psiFile)
                configName = consoleInfo.configurationName
                haskellFile <- HaskellConsoleViewMap.projectFileByConfigName.get(configName)
              } yield haskellFile
            } else {
              Some(psiFile)
            }

          ProgressManager.checkCanceled()

          // Retrieve identifiers in scope by always using the project file
          projectFile.foreach(file => {
            resultSet.addAllElements(getAvailableLookupElements(file).asJava)
          })

          ProgressManager.checkCanceled()

          resultSet.addAllElements(getKeywordLookupElements.asJavaCollection)
          resultSet.addAllElements(getSpecialReservedIdLookupElements.asJavaCollection)
          resultSet.addAllElements(getPragmaStartEndIdsLookupElements.asJavaCollection)
          resultSet.addAllElements(getCommentIdsLookupElements.asJavaCollection)
          resultSet.addAllElements(getHaddockIdsLookupElements.asJavaCollection)

          ProgressManager.checkCanceled()

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

  private def createLocalLookupElement(namedElement: HaskellNamedElement): LookupElementBuilder = {
    val typeSignature = for {
      result <- HaskellComponentsManager.findTypeInfoForElement(namedElement)
      ts <- result.toOption.map(_.typeSignature)
    } yield {
      ts
    }
    LookupElementBuilder.create(namedElement.getName).withTypeText(typeSignature.map(StringUtil.unescapeXml).getOrElse("")).withIcon(HaskellIcons.HaskellSmallBlueLogo)
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

  private def findAvailableIdsForImportModuleSpec(element: PsiElement, psiFile: PsiFile) = {
    import scala.concurrent.ExecutionContext.Implicits.global

    HaskellPsiUtil.findImportDeclarationParent(element).flatMap(_.getModuleName) match {
      case Some(moduleName) =>
        val ids = HaskellComponentsManager.findModuleIdentifiers(psiFile.getProject, moduleName).map(_.map(i => createLookupElement(i, addParens = true)))
        try {
          Await.result(ids, Timeout)
        } catch {
          case _: TimeoutException => Iterable()
        }
      case None => Iterable()
    }
  }

  private def findAvailableModuleNamesLookupElements(project: Project, psiFile: PsiFile) = {
    val moduleNames = HaskellComponentsManager.findAvailableModuleNamesWithIndex(psiFile)
    moduleNames.map(createModuleLookupElement)
  }

  private def createModuleLookupElement(moduleName: String) = {
    LookupElementBuilder.create(moduleName).withTailText(" module", true)
  }

  private def getInsideImportClausesLookupElements = {
    InsideImportKeywords.map(c => LookupElementBuilder.create(c).withTailText(" clause", true))
  }

  private def getLanguageExtensionsLookupElements(project: Project) = {
    HaskellComponentsManager.getSupportedLanguageExtension(project).map(n => LookupElementBuilder.create(n).withIcon(HaskellIcons.HaskellSmallBlueLogo).withTailText(" language extension", true))
  }

  private def getPragmaStartEndIdsLookupElements = {
    PragmaIds.map(p => LookupElementBuilder.create(p).withIcon(HaskellIcons.HaskellSmallBlueLogo).withTailText(" pragma", true))
  }

  private def getFileHeaderPragmaIdsLookupElements = {
    FileHeaderPragmaIds.map(p => LookupElementBuilder.create(p).withIcon(HaskellIcons.HaskellSmallBlueLogo).withTailText(" pragma", true))
  }

  private def getModulePragmaIdsLookupElements = {
    ModulePragmaIds.map(p => LookupElementBuilder.create(p).withIcon(HaskellIcons.HaskellSmallBlueLogo).withTailText(" pragma", true))
  }

  private def getCommentIdsLookupElements = {
    CommentIds.map(p => LookupElementBuilder.create(p).withIcon(HaskellIcons.HaskellSmallBlueLogo).withTailText(" comment", true))
  }

  private def getHaddockIdsLookupElements = {
    HaddockIds.map(p => LookupElementBuilder.create(p).withIcon(HaskellIcons.HaskellSmallBlueLogo).withTailText(" haddock", true))
  }

  private def getAvailableLookupElements(psiFile: PsiFile): Iterable[LookupElementBuilder] = {
    val moduleName = HaskellFilePathIndex.findModuleName(psiFile, GlobalSearchScope.projectScope(psiFile.getProject))
    useAvailableModuleIdentifiers(psiFile, moduleName, (f1, f2, f3, f4) => (f1 ++ f2 ++ f3).map(mi => createLookupElement(mi)) ++ getLocalTopLevelLookupElments(psiFile, moduleName, f4))
  }

  private def getKeywordLookupElements = {
    Keywords.map(createKeywordLookupElement)
  }

  private def createKeywordLookupElement(keyword: String) = {
    LookupElementBuilder.create(keyword).withIcon(HaskellIcons.HaskellSmallBlueLogo).withTailText(" keyword", true)
  }

  private def getSpecialReservedIdLookupElements = {
    SpecialReservedIds.map(sr => LookupElementBuilder.create(sr).withIcon(HaskellIcons.HaskellSmallBlueLogo).withTailText(" special keyword", true))
  }
}

object HaskellCompletionContributor {

  private final val Timeout = Duration.create(1, TimeUnit.SECONDS)

  import scala.concurrent.ExecutionContext.Implicits.global

  private def useAvailableModuleIdentifiers[A](psiFile: PsiFile, moduleName: Option[String],
                                               doIt: (Iterable[ModuleIdentifier], Iterable[ModuleIdentifier], Iterable[ModuleIdentifier], Iterable[ModuleIdentifier]) => Iterable[A]): Iterable[A] = {
    val importDeclarations = findImportDeclarations(psiFile)

    val idsF1 = getModuleIdentifiersFromFullImportedModules(psiFile, importDeclarations)
    val idsF2 = getModuleIdentifiersFromHidingIdsImportedModules(psiFile, importDeclarations)
    val idsF3 = getModuleIdentifiersFromSpecIdsImportedModules(psiFile, importDeclarations)
    val idsF4 = moduleName.map(mn => HaskellComponentsManager.findLocalModuleIdentifiers(psiFile.getProject, psiFile, mn)).getOrElse(Future.successful(Iterable()))

    val f = for {
      f1 <- idsF1
      f2 <- idsF2
      f3 <- idsF3
      f4 <- idsF4
    } yield doIt(f1, f2, f3, f4)
    try {
      Await.result(f, Timeout)
    } catch {
      case _: TimeoutException =>
        HaskellNotificationGroup.logInfoEvent(psiFile.getProject, s"Timout while getting module identifiers for ${psiFile.getName}")
        doIt(getSuccessValue(idsF1), getSuccessValue(idsF2), getSuccessValue(idsF3), getSuccessValue(idsF4))
    }
  }

  private def getSuccessValue[A](f: Future[Iterable[ModuleIdentifier]]) = {
    if (f.isCompleted) {
      f.value.map(_.getOrElse(Iterable())).getOrElse(Iterable())
    } else {
      Iterable()
    }
  }

  def getAvailableModuleIdentifiers(psiFile: PsiFile, moduleName: Option[String]): Iterable[ModuleIdentifier] = {
    useAvailableModuleIdentifiers(psiFile, moduleName, (f1, f2, f3, f4) => f1 ++ f2 ++ f3 ++ f4)
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
    val stackComponentInfo = HaskellComponentsManager.findStackComponentInfo(psiFile)
    val globalInfo = stackComponentInfo.flatMap(HaskellComponentsManager.findStackComponentGlobalInfo)
    globalInfo.exists(_.noImplicitPreludeActive) || HaskellPsiUtil.findLanguageExtensions(psiFile).flatMap(_.getGeneralPragmaContentList.asScala).exists(_.getText.contains("NoImplicitPrelude"))
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

  private def getModuleIdentifiersFromFullImportedModules(psiFile: PsiFile, importDeclarations: Iterable[HaskellImportDeclaration]): Future[Iterable[ModuleIdentifier]] = {
    val importInfos = getFullImportedModules(psiFile, importDeclarations)

    Future.sequence(importInfos.map(importInfo => {
      val allModuleIdentifiers = HaskellComponentsManager.findModuleIdentifiers(psiFile.getProject, importInfo.moduleName)
      allModuleIdentifiers.map(mi => createQualifiedModuleIdentifiers(importInfo, mi))
    })).map(_.flatten)
  }

  private def getModuleIdentifiersFromHidingIdsImportedModules(psiFile: PsiFile, importDeclarations: Iterable[HaskellImportDeclaration]): Future[Iterable[ModuleIdentifier]] = {
    val importInfos = getImportedModulesWithHidingIdsSpec(psiFile, importDeclarations)

    Future.sequence(importInfos.map(importInfo => {
      val allModuleIdentifiers = HaskellComponentsManager.findModuleIdentifiers(psiFile.getProject, importInfo.moduleName)
      allModuleIdentifiers.map(ids => createQualifiedModuleIdentifiers(importInfo, ids.filterNot(mi => importInfo.ids.exists(_ == mi.name))))
    })).map(_.flatten)
  }

  private def getModuleIdentifiersFromSpecIdsImportedModules(psiFile: PsiFile, importDeclarations: Iterable[HaskellImportDeclaration]): Future[Iterable[ModuleIdentifier]] = {
    val importInfos = getImportedModulesWithSpecIds(psiFile, importDeclarations)

    Future.sequence(importInfos.map(importInfo => {
      val allModuleIdentifiers = HaskellComponentsManager.findModuleIdentifiers(psiFile.getProject, importInfo.moduleName)
      allModuleIdentifiers.map(ids => createQualifiedModuleIdentifiers(importInfo, ids.filter(mi => importInfo.ids.exists(id => if (mi.isOperator) s"(${mi.name})" == id else id == mi.name))))
    })).map(_.flatten)
  }

  private def createLocalTopLevelLookupElement(moduleIdentifier: ModuleIdentifier): LookupElementBuilder = {
    LookupElementBuilder.create(moduleIdentifier.name).withTypeText(moduleIdentifier.declaration).withIcon(findIcon(moduleIdentifier))
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
      case d if d.startsWith("module ") => HaskellIcons.Module
      case _ => HaskellSmallBlueLogo
    }
  }


  private def createLookupElement(moduleIdentifier: ModuleIdentifier, addParens: Boolean = false) = {
    addWiths(LookupElementBuilder.create(
      if (moduleIdentifier.isOperator && addParens)
        s"""(${moduleIdentifier.name})"""
      else
        moduleIdentifier.name
    ), moduleIdentifier)
  }

  private def getLocalTopLevelLookupElments(psiFile: PsiFile, moduleName: Option[String], localIdentifiers: Iterable[ModuleIdentifier]): Iterable[LookupElementBuilder] = {
    moduleName match {
      case Some(_) =>

        ProgressManager.checkCanceled()

        val importDeclarations = HaskellPsiUtil.findImportDeclarations(psiFile)
        val localLookupElements = localIdentifiers.map(mi =>
          if (moduleName.contains(mi.moduleName)) {
            createLocalTopLevelLookupElement(mi)
          } else {
            val qualifier = importDeclarations.find(id => id.getModuleName.contains(mi.moduleName)).flatMap(id => Option(id.getImportQualifiedAs).map(_.getQualifier.getName))
            qualifier match {
              case Some(q) => createLookupElement(mi.copy(name = s"$q.${mi.name}"))
              case None => createLookupElement(mi)
            }
          }
        )
        localLookupElements
      case None =>
        HaskellNotificationGroup.logWarningEvent(psiFile.getProject, s"No support for suggesting local toplevel identtifiers because no module defined in ${psiFile.getName}")
        Iterable()
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
