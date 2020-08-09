/*
 * Copyright 2014-2020 Rik van der Kleij
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

import com.intellij.codeInsight.completion._
import com.intellij.codeInsight.lookup.{LookupElement, LookupElementBuilder, LookupElementPresentation}
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.text.StringUtil
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.impl.source.tree.TreeUtil
import com.intellij.psi.{PsiElement, PsiFile, TokenType}
import com.intellij.util.ProcessingContext
import icons.HaskellIcons
import intellij.haskell.annotator.HaskellAnnotator
import intellij.haskell.external.component.HaskellComponentsManager.ComponentTarget
import intellij.haskell.external.component._
import intellij.haskell.psi.HaskellTypes._
import intellij.haskell.psi._
import intellij.haskell.runconfig.console.{HaskellConsoleView, HaskellConsoleViewMap}
import intellij.haskell.util._
import intellij.haskell.{HaskellFile, HaskellParserDefinition}
import org.apache.commons.lang.StringEscapeUtils

import scala.jdk.CollectionConverters._
import scala.util.matching.Regex

class HaskellCompletionContributor extends CompletionContributor {

  private final val HaskellWhere = LazyList("where")
  private final val HaskellLet = LazyList("let")
  private final val haskellDeclarationKeywords = LazyList("family", "data", "type", "module", "class", "instance", "newtype", "deriving", "in")
  private final val HaskellDefault = LazyList("default")
  private final val HaskellImportKeywords = LazyList("import", "qualified", "as", "hiding")
  private final val HaskellForeignKeywords = LazyList("foreign", "export", "ccall", "safe", "unsafe", "interruptible", "capi", "prim")
  private final val HaskellKeyword = LazyList("do", "case", "of")
  private final val HaskellStatic = LazyList("static")
  private final val HaskellConditional = LazyList("if", "then", "else")
  private final val HaskellInfix = LazyList("infix", "infixl", "infixr")
  private final val HaskellBottom = LazyList("undefined")
  private final val HaskellTodo = LazyList("TODO", "FIXME")
  private final val HaskellTypeRoles = LazyList("phantom", "representational", "nominal")
  private final val HaskellForall = LazyList("forall")
  private final val HaskellRecursiveDo = LazyList("mdo", "rec")
  private final val HaskellArrowSyntax = LazyList("proc")

  private final val Keywords = HaskellWhere ++ HaskellLet ++ haskellDeclarationKeywords ++ HaskellDefault ++ HaskellImportKeywords ++
    HaskellForeignKeywords ++ HaskellKeyword ++ HaskellStatic ++ HaskellConditional ++ HaskellInfix ++ HaskellBottom ++
    HaskellTodo ++ HaskellTypeRoles ++ HaskellForall ++ HaskellRecursiveDo ++ HaskellArrowSyntax

  private final val SpecialReservedIds = LazyList("safe", "unsafe")
  private final val PragmaIds = LazyList("{-#", "#-}")
  private final val Ann = "ANN"
  private final val FileHeaderPragmaIds = LazyList("LANGUAGE", "OPTIONS_HADDOCK", "INCLUDE", "OPTIONS", "OPTIONS_GHC", Ann)
  private final val ModulePragmaIds = LazyList(Ann, "DEPRECATED", "WARING", "INLINE", "INLINE_FUSED", "INLINE_INNER", "NOINLINE", "NOTINLINE", "INLINABEL", "LINE", "RULES",
    "SPECIALIZE", "SPECIALISE", "MINIMAL", "SOURCE", "UNPACK", "NOUNPACK", "OVERLAPPING", "OVERLAPPABLE", "OVERLAPS", "CONSTANT_FOLDED", "SCC", "INCOHERENT", "CFILES")
  private final val InsideImportKeywords = LazyList("as", "hiding", "qualified")
  private final val CommentIds = LazyList("{-", "-}", "--")
  private final val HaddockIds = LazyList("{-|", "-- |", "-- ^")

  private final val TypeWildcard: Regex = """.*Found.type.wildcard.[`|‘][^'’]+['|’].standing.for.[`|‘]([^'’]+)['|’].*""".r

  private def findQualifiedNamedElementToComplete(element: PsiElement): Option[HaskellQualifiedNameElement] = {
    val elementType = Option(element.getNode.getElementType)
    val psiFile = element.getContainingFile.getOriginalFile
    (for {
      et <- elementType
      if et == HS_DOT
      e <- Option(psiFile.findElementAt(element.getTextOffset - 1))
      p <- HaskellPsiUtil.findQualifiedName(e)
    } yield p).orElse(for {
      et <- elementType
      if et == HS_NEWLINE || et == TokenType.WHITE_SPACE
      d <- Option(psiFile.findElementAt(element.getTextOffset - 1))
      if d.getNode.getElementType == HS_DOT
      e <- Option(psiFile.findElementAt(element.getTextOffset - 2))
      p <- HaskellPsiUtil.findQualifiedName(e)
    } yield p).
      orElse(HaskellPsiUtil.findQualifiedName(element))
  }


  private val provider: CompletionProvider[CompletionParameters] = new CompletionProvider[CompletionParameters] {
    def addCompletions(parameters: CompletionParameters, context: ProcessingContext, originalResultSet: CompletionResultSet): Unit = {

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
            mie <- HaskellPsiUtil.findModIdElement(e).orElse(HaskellPsiUtil.findQualifierElement(e))
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
            resultSet.addAllElements(findAvailableIdsForImportModuleSpec(psiFile, e).asJavaCollection)
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

            val currentElement = op.flatMap(HaskellPsiUtil.findNamedElement)

            projectFile.foreach(f => resultSet.addAllElements(getAvailableLookupElements(f, currentElement).asJava))

            ProgressManager.checkCanceled()

            resultSet.addAllElements(getKeywordLookupElements.asJavaCollection)
            resultSet.addAllElements(getSpecialReservedIdLookupElements.asJavaCollection)
            resultSet.addAllElements(getPragmaStartEndIdsLookupElements.asJavaCollection)
            resultSet.addAllElements(getCommentIdsLookupElements.asJavaCollection)
            resultSet.addAllElements(getHaddockIdsLookupElements.asJavaCollection)

            ProgressManager.checkCanceled()

            val localLookupElements = currentElement.map(ce => findLocalElements(ce).filterNot(_ == ce).map(createLocalLookupElement)).getOrElse(LazyList())
            ProgressManager.checkCanceled()
            resultSet.addAllElements(localLookupElements.asJavaCollection)
        }
      }
    }
  }

  private val smartProvider: CompletionProvider[CompletionParameters] = new CompletionProvider[CompletionParameters] {

    import intellij.haskell.external.execution.HaskellCompilationResultHelper.LayoutSpaceChar

    def addCompletions(parameters: CompletionParameters, context: ProcessingContext, originalResultSet: CompletionResultSet): Unit = {
      ProgressManager.checkCanceled()

      val project = parameters.getOriginalFile.getProject
      if (StackProjectManager.isInitializing(project)) {
        HaskellEditorUtil.showHaskellSupportIsNotAvailableWhileInitializing(project)
      } else {
        Option(parameters.getOriginalPosition) match {
          case Some(position) if HaskellPsiUtil.findExpression(position).isDefined & position.getText.startsWith("_") =>
            val offset = position.getTextOffset
            val editor = parameters.getEditor
            HaskellAnnotator.findHighlightInfo(project, offset, editor) match {
              case Some(highlightInfo) =>
                val message = highlightInfo.getToolTip
                val typedHoleLines = message.split("\n").
                  map(_.replaceAll(s"$LayoutSpaceChar{2,}", "").
                    replaceAll(s"$LayoutSpaceChar", " "))
                val typedHoleSuggestionsWithHeader = typedHoleLines.dropWhile(l => !l.contains("Relevant bindings include") && !l.contains("Valid substitutions include") && !l.contains("Valid hole fits include"))
                if (typedHoleSuggestionsWithHeader.nonEmpty) {
                  val typedHoleSuggestionLines = typedHoleSuggestionsWithHeader.tail
                  val typedHoleSuggestions = typedHoleSuggestionLines.filterNot(_.trim.startsWith("("))
                  val lookupElements = typedHoleSuggestions.flatMap(createLookupElement).to(LazyList)
                  originalResultSet.addAllElements(lookupElements.asJavaCollection)
                }
              case None => ()
            }
          case Some(position) if HaskellPsiUtil.findTypeSignatureDeclaration(position).isDefined & position.getText.startsWith("_") =>
            val offset = position.getTextOffset
            val editor = parameters.getEditor
            HaskellAnnotator.findHighlightInfo(project, offset, editor) match {
              case Some(highlightInfo) =>
                val messageLines = highlightInfo.getDescription.split("\n")
                val suggestions = messageLines.collect {
                  case TypeWildcard(name) => name
                }
                val lookupElements = suggestions.map(LookupElementBuilder.create).to(LazyList)
                originalResultSet.addAllElements(lookupElements.asJavaCollection)
              case None => ()
            }
          case _ => ()
        }
      }
    }

    private def createLookupElement(typeSignature: String): Option[LookupElementBuilder] = {
      typeSignature.split("::", 2).toSeq match {
        case Seq(n, _) => Some(LookupElementBuilder.create(n.trim).withTypeText(StringEscapeUtils.unescapeHtml(typeSignature)))
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

  private def findLocalElements(element: PsiElement) = {
    HaskellPsiUtil.findExpression(element).to(LazyList).flatMap(e => HaskellPsiUtil.findNamedElements(e)).filter {
      case _: HaskellVarid => true
      case _: HaskellVarsym => true
      case _: HaskellConsym => true
      case _ => false
    }
  }

  private def isImportSpecInProgress(element: PsiElement): Boolean = {
    Option(TreeUtil.findParent(element.getNode, HaskellTypes.HS_IMPORT_SPEC)).isDefined
  }

  private def isFileHeaderPragmaInProgress(element: PsiElement): Boolean = {
    Option(TreeUtil.findParent(element.getNode, HaskellTypes.HS_FILE_HEADER)).isDefined
  }

  private def isPragmaInProgress(element: PsiElement): Boolean = {
    Option(TreeUtil.findSiblingBackward(element.getNode, HS_PRAGMA_START)).isDefined
  }

  private def isImportModuleDeclarationInProgress(element: PsiElement): Boolean = {
    HaskellPsiUtil.findImportDeclaration(element).isDefined ||
      Option(TreeUtil.findSiblingBackward(element.getNode, HS_IMPORT)).isDefined ||
      isImportIdInProgressInsideImportModuleDeclaration(element)
  }

  private def isImportIdInProgressInsideImportModuleDeclaration(element: PsiElement): Boolean = {
    val prevNode = Option(TreeUtil.prevLeaf(element.getNode))
    prevNode.exists(_ => HaskellPsiUtil.findImportDeclaration(element).isDefined)
  }

  private def isNCommentInProgress(element: PsiElement): Boolean = {
    element.getNode.getElementType == HS_NCOMMENT
  }

  private def getGlobalInfo(psiFile: PsiFile): Option[(ComponentTarget, StackComponentGlobalInfo)] = {
    for {
      info <- HaskellComponentsManager.findStackComponentInfo(psiFile)
      globalInfo <- HaskellComponentsManager.findStackComponentGlobalInfo(info)
    } yield (info, globalInfo)
  }

  private def findAvailableIdsForImportModuleSpec(psiFile: PsiFile, element: PsiElement): Iterable[LookupElementBuilder] = {
    import scala.concurrent.ExecutionContext.Implicits.global

    HaskellPsiUtil.findImportDeclaration(element).flatMap(_.getModuleName) match {
      case Some(moduleName) =>
        ScalaFutureUtil.waitForValue(psiFile.getProject,
          HaskellComponentsManager.findModuleIdentifiers(psiFile.getProject, moduleName)
          , s"finding module identifiers for $moduleName").flatten match {
          case Some(ids) => ids.map(x => {
            ProgressManager.checkCanceled()
            createLookupElement(x, addParens = true)
          })
          case None => Iterable()
        }
      case None => Iterable()
    }
  }

  private def createModuleLookupElement(moduleName: String) = {
    LookupElementBuilder.create(moduleName).withTailText(" module", true)
  }

  private lazy val getInsideImportClausesLookupElements = {
    InsideImportKeywords.map(c => LookupElementBuilder.create(c).withTailText(" clause", true))
  }

  private def getLanguageExtensionsLookupElements(project: Project) = {
    HaskellComponentsManager.getSupportedLanguageExtension(project).map(n => LookupElementBuilder.create(n).withIcon(HaskellIcons.HaskellSmallBlueLogo).withTailText(" language extension", true))
  }

  private lazy val getPragmaStartEndIdsLookupElements = {
    PragmaIds.map(p => LookupElementBuilder.create(p).withIcon(HaskellIcons.HaskellSmallBlueLogo).withTailText(" pragma", true))
  }

  private lazy val getFileHeaderPragmaIdsLookupElements = {
    FileHeaderPragmaIds.map(p => LookupElementBuilder.create(p).withIcon(HaskellIcons.HaskellSmallBlueLogo).withTailText(" pragma", true))
  }

  private lazy val getModulePragmaIdsLookupElements = {
    ModulePragmaIds.map(p => LookupElementBuilder.create(p).withIcon(HaskellIcons.HaskellSmallBlueLogo).withTailText(" pragma", true))
  }

  private lazy val getCommentIdsLookupElements = {
    CommentIds.map(p => LookupElementBuilder.create(p).withIcon(HaskellIcons.HaskellSmallBlueLogo).withTailText(" comment", true))
  }

  private lazy val getHaddockIdsLookupElements = {
    HaddockIds.map(p => LookupElementBuilder.create(p).withIcon(HaskellIcons.HaskellSmallBlueLogo).withTailText(" haddock", true))
  }

  private def getAvailableLookupElements(psiFile: PsiFile, currentElement: Option[HaskellNamedElement]): Iterable[LookupElementBuilder] = {
    ProgressManager.checkCanceled()
    FileModuleIdentifiers.findAvailableModuleIdentifiers(psiFile).map(createLookupElement(_)) ++ findTopLeveLookupElements(psiFile, currentElement)
  }

  private lazy val getKeywordLookupElements = {
    Keywords.map(createKeywordLookupElement)
  }

  private def createKeywordLookupElement(keyword: String) = {
    LookupElementBuilder.create(keyword).withIcon(HaskellIcons.HaskellSmallBlueLogo).withTailText(" keyword", true)
  }

  private lazy val getSpecialReservedIdLookupElements = {
    SpecialReservedIds.map(sr => LookupElementBuilder.create(sr).withIcon(HaskellIcons.HaskellSmallBlueLogo).withTailText(" special keyword", true))
  }

  private def findTopLeveLookupElements(psiFile: PsiFile, currentElement: Option[HaskellNamedElement]): Iterable[LookupElementBuilder] = {
    def createTopLevelDeclarationLookupElement(e: HaskellNamedElement, d: HaskellDeclarationElement) = {
      createLocalTopLevelLookupElement(ApplicationUtil.runReadAction(e.getName), ApplicationUtil.runReadAction(d.getPresentation.getPresentableText))
    }

    val expressionLookupElements = HaskellPsiUtil.findTopLevelExpressions(psiFile).flatMap(_.getQNameList.asScala.headOption.map(_.getIdentifierElement)).filterNot(e => currentElement.contains(e)).map(createLocalLookupElement)

    val declarationLookupElements = ApplicationUtil.runReadAction(HaskellPsiUtil.findTopLevelDeclarations(psiFile)).
      flatMap(d => getIdentifiers(d).flatMap {
        e =>
          d match {
            case dd: HaskellDataDeclaration =>
              val dataType = dd.getSimpletype.getText
              HaskellComponentsManager.findTypeInfoForElement(e).toOption.filterNot(_.withFailure).map(_.typeSignature).map(ts => LookupElementBuilder.create(e.getName).withTypeText(ts)).orElse(
                HaskellPsiUtil.findDataFieldDecl(e).orElse(HaskellPsiUtil.findDataConstr(e)) match {
                  case Some(ce) => Some(LookupElementBuilder.create(e.getName).withTypeText(s"${ce.getText} -> $dataType"))
                  case None => Some(createTopLevelDeclarationLookupElement(e, d))
                })
            case d: HaskellDeclarationElement if !d.isInstanceOf[HaskellTypeSignature] => Some(createTopLevelDeclarationLookupElement(e, d))
            case _ => None
          }
      })
    expressionLookupElements ++ declarationLookupElements
  }

  private def getIdentifiers(declarationElement: HaskellDeclarationElement) = {
    ApplicationUtil.runReadAction(declarationElement.getIdentifierElements)
  }

  private def createLookupElement(moduleIdentifier: ModuleIdentifier, addParens: Boolean = false): LookupElementBuilder = {
    addWiths(LookupElementBuilder.create(
      if (moduleIdentifier.operator && addParens)
        s"""(${moduleIdentifier.name})"""
      else
        moduleIdentifier.name
    ), moduleIdentifier)
  }

  def createLocalLookupElement(namedElement: HaskellNamedElement): LookupElementBuilder = {
    ProgressManager.checkCanceled()

    def typeSignature = HaskellComponentsManager.findTypeInfoForElement(namedElement).map(_.typeSignature).map(StringUtil.unescapeXmlEntities).getOrElse("")

    LookupElementBuilder.create(namedElement.getName).withIcon(HaskellIcons.HaskellSmallBlueLogo).withRenderer((_: LookupElement, presentation: LookupElementPresentation) => {
      presentation.setTypeText(typeSignature)
      presentation.setItemText(namedElement.getName)
      presentation.setIcon(HaskellIcons.HaskellSmallBlueLogo)
    })
  }

  private def createLocalTopLevelLookupElement(name: String, declaration: String): LookupElementBuilder = {
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
}
