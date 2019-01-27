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
import java.util.concurrent.TimeoutException

import com.github.blemale.scaffeine.{LoadingCache, Scaffeine}
import com.intellij.codeInsight.completion._
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.text.StringUtil
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.impl.source.tree.TreeUtil
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.{PsiElement, PsiFile, TokenType}
import com.intellij.util.{ProcessingContext, WaitFor}
import icons.HaskellIcons
import intellij.haskell.annotator.HaskellAnnotator
import intellij.haskell.external.component.HaskellComponentsManager.StackComponentInfo
import intellij.haskell.external.component._
import intellij.haskell.psi.HaskellElementCondition._
import intellij.haskell.psi.HaskellPsiUtil._
import intellij.haskell.psi.HaskellTypes._
import intellij.haskell.psi._
import intellij.haskell.runconfig.console.{HaskellConsoleView, HaskellConsoleViewMap}
import intellij.haskell.util._
import intellij.haskell.{HaskellFile, HaskellNotificationGroup, HaskellParserDefinition}
import org.apache.commons.lang.StringEscapeUtils

import scala.collection.JavaConverters._
import scala.concurrent._
import scala.concurrent.duration._

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
  private final val HaskellBottom = Stream("undefined")
  private final val HaskellTodo = Stream("TODO", "FIXME")
  private final val HaskellTypeRoles = Stream("phantom", "representational", "nominal")
  private final val HaskellForall = Stream("forall")
  private final val HaskellRecursiveDo = Stream("mdo", "rec")
  private final val HaskellArrowSyntax = Stream("proc")

  private final val Keywords = HaskellWhere ++ HaskellLet ++ haskellDeclKeywords ++ HaskellDefault ++ HaskellImportKeywords ++
    HaskellForeignKeywords ++ HaskellKeyword ++ HaskellStatic ++ HaskellConditional ++ HaskellInfix ++ HaskellBottom ++
    HaskellTodo ++ HaskellTypeRoles ++ HaskellForall ++ HaskellRecursiveDo ++ HaskellArrowSyntax

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
    val psiFile = element.getContainingFile.getOriginalFile
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


  private val provider: CompletionProvider[CompletionParameters] = new CompletionProvider[CompletionParameters] {
    def addCompletions(parameters: CompletionParameters, context: ProcessingContext, originalResultSet: CompletionResultSet) {

      ProgressManager.checkCanceled()

      val psiFile: HaskellFile = parameters.getOriginalFile.asInstanceOf[HaskellFile]

      val project = parameters.getPosition.getProject

      val isConsoleFile = HaskellConsoleView.isConsoleFile(psiFile)

      if (StackProjectManager.isInitializing(project)) {
        HaskellEditorUtil.showHaskellSupportIsNotAvailableWhileInitializing(project)
      } else if (!isConsoleFile && (!HaskellProjectUtil.isSourceFile(psiFile) || Option(parameters.getOriginalPosition).map(_.getNode.getElementType).exists(t => HaskellParserDefinition.Literals.contains(t) || HaskellParserDefinition.Comments.contains(t)))) {
        ()
      } else {

        ProgressManager.checkCanceled()

        val positionElement = if (isConsoleFile) {
          None
        } else {
          // In case element before caret is a qualifier, module name or a dot we have to "help" IntelliJ to get the right preselected elements behavior
          // For example, asking for completion in case typing `Data.List.` will give without this help no prefix.
          if (Option(parameters.getOriginalPosition).exists(_.getNode.getElementType == HS_NEWLINE)) {
            Option(parameters.getPosition)
          } else {
            Option(parameters.getOriginalPosition).orElse(Option(parameters.getPosition))
          }
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

        lazy val (stackComponentInfo, globalInfo) = getGlobalInfo(psiFile) match {
          case Some((info, gInfo)) => (Some(info), Some(gInfo))
          case _ => (None, None)
        }

        positionElement match {
          case Some(e) if isFileHeaderPragmaInProgress(e) =>
            resultSet.addAllElements(getLanguageExtensionsLookupElements(project).asJavaCollection)
            resultSet.addAllElements(getPragmaStartEndIdsLookupElements.asJavaCollection)
            resultSet.addAllElements(getFileHeaderPragmaIdsLookupElements.asJavaCollection)
          case Some(e) if isPragmaInProgress(e) =>
            resultSet.addAllElements(getModulePragmaIdsLookupElements.asJavaCollection)
            resultSet.addAllElements(getPragmaStartEndIdsLookupElements.asJavaCollection)
          case Some(e) if isImportSpecInProgress(e) =>
            globalInfo.foreach(gi => resultSet.addAllElements(findAvailableIdsForImportModuleSpec(gi, psiFile, e).asJavaCollection))
          case Some(e) if isImportModuleDeclarationInProgress(e) =>
            // Do not give suggestions when defining import qualifier
            if (e.getParent.getNode.getElementType != HS_QUALIFIER) {
              stackComponentInfo.foreach(info => resultSet.addAllElements(HaskellComponentsManager.findAvailableModuleNamesWithIndex(info).map(createModuleLookupElement).asJavaCollection))
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
                for {
                  consoleInfo <- HaskellConsoleView.findConsoleInfo(psiFile)
                  configName = consoleInfo.configurationName
                  haskellFile <- HaskellConsoleViewMap.projectFileByConfigName.get(configName)
                } yield haskellFile
              } else {
                Some(psiFile)
              }

            ProgressManager.checkCanceled()

            for {
              file <- projectFile
              info <- stackComponentInfo
              gInfo <- globalInfo
            } yield
              resultSet.addAllElements(getAvailableLookupElements(gInfo, info, file).asJava)

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
  }

  val smartProvider: CompletionProvider[CompletionParameters] = new CompletionProvider[CompletionParameters] {

    import intellij.haskell.external.execution.HaskellCompilationResultHelper.LayoutSpaceChar

    def addCompletions(parameters: CompletionParameters, context: ProcessingContext, originalResultSet: CompletionResultSet): Unit = {
      ProgressManager.checkCanceled()

      Option(parameters.getOriginalPosition) match {
        case Some(position) if HaskellPsiUtil.findExpressionParent(position).isDefined & position.getNode.getElementType == HaskellTypes.HS_UNDERSCORE =>
          val project = parameters.getPosition.getProject
          val offset = position.getTextOffset
          val editor = parameters.getEditor
          HaskellAnnotator.findHighlightInfo(project, offset, editor) match {
            case Some(highlightInfo) =>
              val message = highlightInfo.getToolTip
              val typedHoleLines = message.split("\n").
                map(_.replaceAll(s"$LayoutSpaceChar{2,}", "").
                  replaceAll(s"$LayoutSpaceChar", " "))
              val typedHoleSuggestionsWithHeader = typedHoleLines.dropWhile(_ != "Valid substitutions include")
              if (typedHoleSuggestionsWithHeader.nonEmpty) {
                val typedHoleSuggestionLines = typedHoleSuggestionsWithHeader.tail
                val typedHoleSuggestions = typedHoleSuggestionLines.filterNot(_.trim.startsWith("("))
                val lookupElements = typedHoleSuggestions.flatMap(createLookupElement).toStream
                originalResultSet.addAllElements(lookupElements.asJavaCollection)
              }
            case None => ()
          }

        case _ => ()
      }
    }

    private def createLookupElement(typeSignature: String): Option[LookupElementBuilder] = {
      typeSignature.split("::", 2).toSeq match {
        case Seq(n, t) => Some(LookupElementBuilder.create(n.trim).withTypeText(StringEscapeUtils.unescapeHtml(typeSignature)))
        case _ => None
      }
    }
  }

  extend(CompletionType.BASIC, PlatformPatterns.psiElement(), provider)

  extend(CompletionType.SMART, PlatformPatterns.psiElement(), smartProvider)

  override def beforeCompletion(context: CompletionInitializationContext): Unit = {
    val psiFile = context.getFile
    val contextElement = Option(psiFile.findElementAt(context.getStartOffset - 1))
    contextElement match {
      case None => context.setDummyIdentifier("a")
      case Some(ce) if ce.getNode.getElementType == HS_NEWLINE | ce.getNode.getElementType == HS_LEFT_PAREN => context.setDummyIdentifier("a")
      case Some(ce) =>
        HaskellPsiUtil.findModIdElement(ce) match {
          case Some(modid) =>
            val start = modid.getTextRange.getStartOffset
            val end = context.getCaret.getOffset
            context.setDummyIdentifier(psiFile.getText.substring(start, end))
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
    val typeSignature = HaskellComponentsManager.findTypeInfoForElement(namedElement).map(_.typeSignature)
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

  private def getGlobalInfo(psiFile: PsiFile): Option[(StackComponentInfo, StackComponentGlobalInfo)] = {
    val globalInfo = ApplicationManager.getApplication.executeOnPooledThread(ScalaUtil.callable {
      for {
        info <- HaskellComponentsManager.findStackComponentInfo(psiFile)
        globalInfo <- HaskellComponentsManager.findStackComponentGlobalInfo(info)
      } yield (info, globalInfo)
    })

    new WaitFor(ApplicationUtil.timeout, 1) {
      override def condition(): Boolean = {
        ProgressManager.checkCanceled()
        globalInfo.isDone
      }
    }

    if (globalInfo.isDone) {
      globalInfo.get()
    } else {
      HaskellNotificationGroup.logInfoEvent(psiFile.getProject, "Timeout in getGlobalInfo for " + psiFile.getName)
      None
    }
  }

  private def findAvailableIdsForImportModuleSpec(stackComponentGlobalInfo: StackComponentGlobalInfo, psiFile: PsiFile, element: PsiElement) = {
    import scala.concurrent.ExecutionContext.Implicits.global

    HaskellPsiUtil.findImportDeclarationParent(element).flatMap(_.getModuleName) match {
      case Some(moduleName) =>
        val ids = HaskellComponentsManager.findExportedModuleIdentifiers(psiFile, moduleName).map(_.map(i => i.map(x => createLookupElement(x, addParens = true))).getOrElse(Iterable()))

        new WaitFor(ApplicationUtil.timeout, 1) {
          override def condition(): Boolean = {
            ProgressManager.checkCanceled()
            ids.isCompleted
          }
        }

        if (ids.isCompleted) {
          Await.result(ids, 1.milli)
        } else {
          HaskellNotificationGroup.logInfoEvent(psiFile.getProject, "Timeout in findAvailableIdsForImportModuleSpec for " + psiFile.getName)
          Iterable()
        }

      case None => Iterable()
    }
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

  private def getAvailableLookupElements(globalInfo: StackComponentGlobalInfo, info: StackComponentInfo, psiFile: PsiFile): Iterable[LookupElementBuilder] = {
    val moduleName = HaskellPsiUtil.findModuleName(psiFile)
    ProgressManager.checkCanceled()

    useAvailableModuleIdentifiers(globalInfo, info, psiFile, moduleName, (f1, f2) => f1.map(mi => createLookupElement(mi)) ++ getLocalTopLevelLookupElements(psiFile, moduleName, f2))
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

object FileModuleIdentifiers {

  private case class Key(psiFile: PsiFile)

  private type Result = Option[ModuleIdentifiers]

  // TODO Maybe use the buildAsyncFuture
  private final val Cache: LoadingCache[Key, Result] = Scaffeine().build((k: Key) => findModuleIdentifiers(k))

  import scala.concurrent.ExecutionContext.Implicits.global

  def invalidate(psiFile: PsiFile): Unit = {
    Cache.invalidate(Key(psiFile))
  }

  def refresh(psiFile: PsiFile): Unit = {
    Cache.refresh(Key(psiFile))
  }

  def invalidateAll(project: Project): Unit = {
    Cache.asMap().filter(_._1.psiFile.getProject == project).keys.foreach(Cache.invalidate)
  }

  def getModuleIdentifiers(psiFile: PsiFile): Option[Iterable[ModuleIdentifier]] = {
    val key = Key(psiFile)
    Cache.getIfPresent(key) match {
      case Some(Some(x)) =>
        if (x.toSeq.contains(None)) {
          Cache.invalidate(key)
        }
        Some(x.flatten.flatten)
      case Some(None) =>
        Cache.invalidate(key)
        None
      case None =>
        if (ApplicationManager.getApplication.isReadAccessAllowed) {
          val f = Future(Cache.get(key))
          ScalaFutureUtil.waitWithCheckCancelled(psiFile.getProject, f, "getModuleIdentifiers", 4900.millis).flatten match {
            case Some(x) =>
              if (x.toSeq.contains(None)) {
                Cache.invalidate(key)
              }
              Some(x.flatten.flatten)
            case None =>
              Cache.invalidate(key)
              None
          }
        } else {
          Cache.get(key) match {
            case Some(x) =>
              if (x.toSeq.contains(None)) {
                Cache.invalidate(key)
              }
              Some(x.flatten.flatten)
            case None =>
              Cache.invalidate(key)
              None
          }
        }
    }
  }

  private type ModuleIdentifiers = Iterable[Option[Iterable[ModuleIdentifier]]]

  private def findModuleIdentifiers(k: Key): Option[ModuleIdentifiers] = {
    val project = k.psiFile.getProject
    val psiFile = k.psiFile

    val importDeclarations = ApplicationUtil.runReadAction(findImportDeclarations(psiFile))

    val noImplicitPrelude = if (HaskellProjectUtil.isSourceFile(psiFile)) {
      HaskellComponentsManager.findStackComponentInfo(psiFile).exists(info => HaskellCompletionContributor.isNoImplicitPreludeActive(info, psiFile))
    } else {
      false
    }
    val idsF1 = HaskellCompletionContributor.getModuleIdentifiersFromFullImportedModules(noImplicitPrelude, psiFile, importDeclarations)

    val idsF2 = HaskellCompletionContributor.getModuleIdentifiersFromHidingIdsImportedModules(psiFile, importDeclarations)

    val idsF3 = HaskellCompletionContributor.getModuleIdentifiersFromSpecIdsImportedModules(psiFile, importDeclarations)

    val f = for {
      f1 <- idsF1
      f2 <- idsF2
      f3 <- idsF3
    } yield (f1, f2, f3)

    new WaitFor(4800, 1) {
      override def condition(): Boolean = {
        f.isCompleted
      }
    }

    try {
      val (x, y, z) = Await.result(f, 1.milli)
      Some(x ++ y ++ z)
    } catch {
      case _: TimeoutException =>
        HaskellNotificationGroup.logInfoEvent(project, s"Timeout while find module identifiers for file ${k.psiFile.getName}")
        None
    }
  }
}

object HaskellCompletionContributor {

  import scala.concurrent.ExecutionContext.Implicits.global

  // TODO Create cache for this one instead of the downstream one???
  private def useAvailableModuleIdentifiers[A](globalInfo: StackComponentGlobalInfo, info: StackComponentInfo, psiFile: PsiFile, moduleName: Option[String],
                                               doIt: (Iterable[ModuleIdentifier], Iterable[ModuleIdentifier]) => Iterable[A]): Iterable[A] = {

    val fileModuleIdentifiers = Future(FileModuleIdentifiers.getModuleIdentifiers(psiFile))
    val moduleIdentifiers = moduleName.map(mn => HaskellComponentsManager.findTopLevelModuleIdentifiers(psiFile, mn)).getOrElse(Future.successful(None))

    val result = for {
      r1 <- fileModuleIdentifiers
      r2 <- moduleIdentifiers
    } yield {
      (r1, r2)
    }

    ScalaFutureUtil.waitWithCheckCancelled(psiFile.getProject, result, s"In useAvailableModuleIdentifiers wait for all module identifiers for ${psiFile.getName} ", 5.seconds) match {
      case Some((result1, result2)) =>
        (result1, result2) match {
          case (Some(x), Some(y)) => doIt(x, y)
          case (_, Some(y)) => doIt(Iterable(), y)
          case (Some(x), _) => doIt(x, Iterable())
          case (_, _) => Iterable()
        }
      case _ => Iterable()
    }
  }

  def getAvailableModuleIdentifiers(globalInfo: StackComponentGlobalInfo, info: StackComponentInfo, psiFile: PsiFile, moduleName: Option[String]): Iterable[ModuleIdentifier] = {
    useAvailableModuleIdentifiers(globalInfo, info, psiFile, moduleName, (f1, f2) => f1 ++ f2)
  }

  private sealed trait ImportInfo {
    def moduleName: String

    def qualified: Boolean

    def as: Option[String]
  }

  private case class ImportFull(moduleName: String, qualified: Boolean, as: Option[String]) extends ImportInfo

  private case class ImportWithHiding(moduleName: String, ids: Iterable[String], qualified: Boolean, as: Option[String]) extends ImportInfo

  private case class ImportWithIds(moduleName: String, ids: Iterable[String], qualified: Boolean, as: Option[String]) extends ImportInfo

  def isNoImplicitPreludeActive(info: StackComponentInfo, psiFile: PsiFile): Boolean = {
    info.isImplicitPreludeActive || ApplicationUtil.runReadAction(HaskellPsiUtil.findLanguageExtensions(psiFile)).flatMap(_.getGeneralPragmaContentList.asScala).exists(p => ApplicationUtil.runReadAction(p.getText).contains("NoImplicitPrelude"))
  }

  private def getFullImportedModules(implicitPreludeActive: Boolean, psiFile: PsiFile, importDeclarations: Iterable[HaskellImportDeclaration]): Iterable[ImportFull] = {
    val moduleNames = for {
      id <- importDeclarations
      if Option(id.getImportSpec).isEmpty
      mn <- ApplicationUtil.runReadAction(id.getModuleName)
    } yield ImportFull(mn, Option(id.getImportQualified).isDefined, Option(id.getImportQualifiedAs).map(qa => ApplicationUtil.runReadAction(qa.getQualifier.getName)))

    if (moduleNames.map(_.moduleName).toSeq.contains(HaskellProjectUtil.Prelude) || implicitPreludeActive) {
      moduleNames
    } else {
      Iterable(ImportFull(HaskellProjectUtil.Prelude, qualified = false, None)) ++ moduleNames
    }
  }

  private def getImportedModulesWithHidingIdsSpec(psiFile: PsiFile, importDeclarations: Iterable[HaskellImportDeclaration]): Iterable[ImportWithHiding] = {
    for {
      importDeclaration <- importDeclarations.filter(i => Option(i.getImportSpec).flatMap(is => Option(is.getImportHidingSpec)).isDefined)
      importIdList = importDeclaration.getImportSpec.getImportHidingSpec.getImportIdList
      mn <- ApplicationUtil.runReadAction(importDeclaration.getModuleName)
    } yield ImportWithHiding(
      mn,
      findImportIds(importIdList),
      Option(importDeclaration.getImportQualified).isDefined,
      Option(importDeclaration.getImportQualifiedAs).map(_.getQualifier).map(q => ApplicationUtil.runReadAction(q.getName))
    )
  }

  private def getImportedModulesWithSpecIds(psiFile: PsiFile, importDeclarations: Iterable[HaskellImportDeclaration]): Iterable[ImportWithIds] = {
    for {
      importDeclaration <- importDeclarations.filter(i => Option(i.getImportSpec).flatMap(is => Option(is.getImportIdsSpec)).isDefined)
      importIdList = importDeclaration.getImportSpec.getImportIdsSpec.getImportIdList
      mn <- ApplicationUtil.runReadAction(importDeclaration.getModuleName)
    } yield ImportWithIds(
      mn,
      findImportIds(importIdList),
      Option(importDeclaration.getImportQualified).isDefined,
      Option(importDeclaration.getImportQualifiedAs).map(_.getQualifier).map(q => ApplicationUtil.runReadAction(q.getName))
    )
  }

  def getModuleIdentifiersFromFullImportedModules(impliciPrelueActive: Boolean, psiFile: PsiFile, importDeclarations: Iterable[HaskellImportDeclaration]): Future[Iterable[Option[Iterable[ModuleIdentifier]]]] = {
    val importInfos = getFullImportedModules(impliciPrelueActive, psiFile, importDeclarations)

    Future.sequence(importInfos.map(importInfo => {
      val allModuleIdentifiers = HaskellComponentsManager.findExportedModuleIdentifiers(psiFile, importInfo.moduleName)
      allModuleIdentifiers.map(mi => mi.map(i => createQualifiedModuleIdentifiers(importInfo, i)))
    }))
  }

  def getModuleIdentifiersFromHidingIdsImportedModules(psiFile: PsiFile, importDeclarations: Iterable[HaskellImportDeclaration]): Future[Iterable[Option[Iterable[ModuleIdentifier]]]] = {
    val importInfos = getImportedModulesWithHidingIdsSpec(psiFile, importDeclarations)

    Future.sequence(importInfos.map(importInfo => {
      val allModuleIdentifiers = HaskellComponentsManager.findExportedModuleIdentifiers(psiFile, importInfo.moduleName)
      allModuleIdentifiers.map(ids => ids.map(is => createQualifiedModuleIdentifiers(importInfo, is.filterNot(mi => importInfo.ids.exists(_ == mi.name)))))
    }))
  }

  def getModuleIdentifiersFromSpecIdsImportedModules(psiFile: PsiFile, importDeclarations: Iterable[HaskellImportDeclaration]): Future[Iterable[Option[Iterable[ModuleIdentifier]]]] = {
    val importInfos = getImportedModulesWithSpecIds(psiFile, importDeclarations)

    Future.sequence(importInfos.map(importInfo => {
      val allModuleIdentifiers = HaskellComponentsManager.findExportedModuleIdentifiers(psiFile, importInfo.moduleName)
      allModuleIdentifiers.map(ids => ids.map(is => createQualifiedModuleIdentifiers(importInfo, is.filter(mi => importInfo.ids.exists(id => if (mi.isOperator) s"(${mi.name})" == id else id == mi.name)))))
    }))
  }

  private def createLocalTopLevelLookupElement(moduleIdentifier: ModuleIdentifier): LookupElementBuilder = {
    createLocalTopLevelLookupElement(moduleIdentifier.name, moduleIdentifier.declaration, moduleIdentifier.moduleName)
  }

  private def createLocalTopLevelLookupElement(name: String, declaration: String, module: String): LookupElementBuilder = {
    LookupElementBuilder.create(name).withTypeText(declaration).withIcon(findIcon(declaration))
  }

  private def addWiths(lookupElementBuilder: LookupElementBuilder, moduleIdentifier: ModuleIdentifier) = {
    lookupElementBuilder.
      withTailText(" " + moduleIdentifier.moduleName, true).
      withIcon(findIcon(moduleIdentifier.declaration)).
      withTypeText(moduleIdentifier.declaration)
  }

  private def findIcon(declaration: String) = {
    import icons.HaskellIcons._
    declaration match {
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

  private def getLocalTopLevelLookupElements(psiFile: PsiFile, moduleName: Option[String], localIdentifiers: Iterable[ModuleIdentifier]): Iterable[LookupElementBuilder] = {
    moduleName match {
      case Some(_) =>
        ProgressManager.checkCanceled()

        val importDeclarations = ApplicationUtil.runReadAction(HaskellPsiUtil.findImportDeclarations(psiFile))
        val localLookupElements = localIdentifiers.map(mi =>
          if (moduleName.contains(mi.moduleName)) {
            createLocalTopLevelLookupElement(mi)
          } else {
            val qualifier = importDeclarations.find(id => ApplicationUtil.runReadAction(id.getModuleName).contains(mi.moduleName)).flatMap(id => Option(id.getImportQualifiedAs).map(q => ApplicationUtil.runReadAction(q.getQualifier.getName)))
            qualifier match {
              case Some(q) => createLookupElement(mi.copy(name = s"$q.${mi.name}"))
              case None => createLookupElement(mi)
            }
          }
        )

        localLookupElements ++ findRemainingTopLeveLookupElements(psiFile, moduleName, localIdentifiers)
      case None =>
        HaskellNotificationGroup.logWarningEvent(psiFile.getProject, s"No REPL support for suggesting local top level identifiers because no module defined in `${psiFile.getName}`")
        findRemainingTopLeveLookupElements(psiFile, None, Iterable())
    }
  }

  private def findRemainingTopLeveLookupElements(psiFile: PsiFile, moduleName: Option[String], localIdentifiers: Iterable[ModuleIdentifier]) = {
    val allLocalNames = localIdentifiers.map(_.name)
    ApplicationUtil.runReadAction(HaskellPsiUtil.findTopLevelDeclarations(psiFile)).filterNot(d => d.isInstanceOf[HaskellModuleDeclaration] || allLocalNames.exists(n => getDeclarationName(d).contains(n))).
      map(d => createLocalTopLevelLookupElement(getDeclarationName(d).getOrElse("-"), ApplicationUtil.runReadAction(d.getPresentation.getPresentableText), moduleName.getOrElse("-")))
  }

  private def getDeclarationName(declarationElement: HaskellDeclarationElement) = {
    ApplicationUtil.runReadAction(declarationElement.getIdentifierElements.headOption.map(_.getName))
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
    importIdList.asScala.flatMap(importId => Iterable(ApplicationUtil.runReadAction(importId.getCname.getName)) ++ importId.getCnameDotDotList.asScala.flatMap(cndd => Option(cndd.getCname).map(cn => ApplicationUtil.runReadAction(cn.getName))))
  }
}
