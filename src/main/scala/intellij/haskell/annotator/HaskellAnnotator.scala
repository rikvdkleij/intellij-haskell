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

package intellij.haskell.annotator

import java.util.concurrent.ConcurrentHashMap

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.codeInsight.daemon.impl._
import com.intellij.codeInsight.intention.impl.BaseIntentionAction
import com.intellij.compiler.{CompilerMessageImpl, ProblemsView}
import com.intellij.lang.annotation.{AnnotationHolder, ExternalAnnotator, HighlightSeverity}
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.compiler.CompilerMessageCategory
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.impl.source.tree.TreeUtil
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.{PsiElement, PsiFile}
import intellij.haskell.editor.{HaskellImportOptimizer, HaskellProblemsView}
import intellij.haskell.external.component.{StackProjectManager, _}
import intellij.haskell.external.execution._
import intellij.haskell.psi._
import intellij.haskell.runconfig.console.HaskellConsoleView
import intellij.haskell.util._
import intellij.haskell.{HaskellFile, HaskellFileType, HaskellNotificationGroup}

import scala.annotation.tailrec
import scala.collection.Iterable
import scala.collection.JavaConverters._

class HaskellAnnotator extends ExternalAnnotator[(PsiFile, Option[PsiElement]), CompilationResult] {

  override def collectInformation(psiFile: PsiFile, editor: Editor, hasErrors: Boolean): (PsiFile, Option[PsiElement]) = {
    if (HaskellConsoleView.isConsoleFile(psiFile) || !HaskellProjectUtil.isSourceFile(psiFile)) {
      null
    } else if (StackProjectManager.isInitializing(psiFile.getProject)) {
      val project = psiFile.getProject
      // Last file is leading
      HaskellNotificationGroup.logInfoEvent(project, s"File ${psiFile.getName} could not be loaded because project is still initializing")
      HaskellAnnotator.NotLoadedFile.put(project, psiFile)
      null
    } else {
      (psiFile, HaskellFileUtil.findVirtualFile(psiFile)) match {
        case (_, None) => null // can be in case if file is in memory only (just created file)
        case (_, Some(f)) if f.getFileType != HaskellFileType.Instance => null
        case (_, Some(_)) if !psiFile.isValid => null
        case (_, Some(_)) =>
          val currentElement = Option(psiFile.findElementAt(editor.getCaretModel.getOffset)).
            find(e => HaskellPsiUtil.findExpression(e).isDefined).
            flatMap(e => Option(PsiTreeUtil.prevVisibleLeaf(e))).filter(_.isValid)
          (psiFile, currentElement)
      }
    }
  }

  override def doAnnotate(psiFileElement: (PsiFile, Option[PsiElement])): CompilationResult = {
    val psiFile = psiFileElement._1
    HaskellFileUtil.findVirtualFile(psiFile) match {
      case Some(virtualFile) =>
        val fileChanged = FileDocumentManager.getInstance().isFileModified(virtualFile)
        if (fileChanged) {
          HaskellFileUtil.saveFileInDispatchThread(psiFile.getProject, virtualFile)
        }
        HaskellComponentsManager.loadHaskellFile(psiFile, fileChanged, psiFileElement._2).orNull
      case None => CompilationResult(Iterable(), Iterable(), failed = false)
    }
  }

  override def apply(psiFile: PsiFile, loadResult: CompilationResult, holder: AnnotationHolder): Unit = {
    val haskellProblemsView = ProblemsView.SERVICE.getInstance(psiFile.getProject).asInstanceOf[HaskellProblemsView]
    val project = psiFile.getProject

    val currentFile = HaskellFileUtil.findVirtualFile(psiFile)
    val currentFileMessages = currentFile.map { cf =>
      loadResult.currentFileProblems.map(p => createCompilerMessage(cf, project, p))
    }

    val otherFileMessages = loadResult.otherFileProblems.flatMap { problem =>
      HaskellFileUtil.findVirtualFile(project, problem.filePath).map { file =>
        createCompilerMessage(file, project, problem)
      }
    }

    ApplicationManager.getApplication.invokeLater { () =>
      if (!project.isDisposed) {
        currentFile.foreach(cf => {
          haskellProblemsView.clearProgress()
          haskellProblemsView.clearOldMessages(cf)
        })

        currentFileMessages.foreach(_.foreach(haskellProblemsView.addMessage))
        otherFileMessages.foreach(haskellProblemsView.addMessage)
      }
    }

    for (annotation <- HaskellAnnotator.createAnnotations(project, psiFile, loadResult.currentFileProblems)) {
      annotation match {
        case ErrorAnnotation(textRange, message, htmlMessage) => holder.createAnnotation(HighlightSeverity.ERROR, textRange, message, htmlMessage)
        case ErrorAnnotationWithIntentionActions(textRange, message, htmlMessage, intentionActions) =>
          val annotation = holder.createAnnotation(HighlightSeverity.ERROR, textRange, message, htmlMessage)
          intentionActions.foreach(annotation.registerFix)
        case WarningAnnotation(textRange, message, htmlMessage) => holder.createAnnotation(HighlightSeverity.WARNING, textRange, message, htmlMessage)
        case WarningAnnotationWithIntentionActions(textRange, message, htmlMessage, intentionActions) =>
          val annotation = holder.createAnnotation(HighlightSeverity.WARNING, textRange, message, htmlMessage)
          intentionActions.foreach(annotation.registerFix)
      }
    }
  }

  private def createCompilerMessage(file: VirtualFile, project: Project, problem: CompilationProblem) = {
    val category = if (problem.isWarning && !(problem.message.contains("-Wdeferred-type-error") || problem.message.contains("not in scope") || problem.message.contains("Not in scope"))) CompilerMessageCategory.WARNING
    else CompilerMessageCategory.ERROR
    new CompilerMessageImpl(project, category, problem.message, file, problem.lineNr, problem.columnNr, null)
  }
}

object HaskellAnnotator {

  private final val NoTypeSignaturePattern = """.* Top-level binding with no type signature: (.+)""".r
  private final val DefinedButNotUsedPattern = """.* Defined but not used: [‘`](.+)[’']""".r
  private final val NotInScopePattern = """.*ot in scope:[^‘`']*[‘`']([^’]+)[’'].*""".r
  private final val NotInScopePattern2 = """.*ot in scope: ([^ ]+).*""".r
  private final val UseAloneInstancesImportPattern = """.* The import of [‘`](.*)[’'] is redundant except perhaps to import instances from [‘`].*[’'] To import instances alone, use: (.+)""".r

  private final val PerhapsYouMeantNamePattern = """.*[`‘]([^‘’`]+)['’]""".r
  private final val PerhapsYouMeantMultiplePattern = """.*ot in scope: [`‘]?([^‘’`]+)['’]? Perhaps you meant one of these: (.+)""".r
  private final val PerhapsYouMeantSingleMultiplePattern = """.*ot in scope: [`‘]?([^‘’`]+)['’]? Perhaps you meant one of these: (((?!Perhaps you).)+) Perhaps you want to add [`‘]?([^‘’`]+)['’]? to the import list in the import of [`‘]?([^‘’`]+)['’]?.*""".r
  //  (((?!Perhaps you).)+) does not work here
  private final val PerhapsYouMeantSingleMultiplePattern3 =
    """.*ot in scope: [`‘]?([^‘’`]+)['’]? Perhaps you meant (.+\)) Perhaps you want to add [`‘]?([^‘’`]+)['’]? to the import list in the import of [`‘]?([^‘’`]+)['’]?.*""".r
  private final val PerhapsYouMeantSingleMultiplePattern2 = """.*ot in scope: [`‘]?([^‘’`]+)['’]? Perhaps you want to add [`‘]?([^‘’`]+)['’]? to the import list in the import of [`‘]?([^‘’`]+)['’]?.*""".r
  private final val PerhapsYouMeantSinglePattern = """.*ot in scope: (?:type constructor or class )?[`‘]?([^‘’`]+)['’]? Perhaps you meant (.+)""".r
  private final val PerhapsYouMeantImportedFromPattern = """.*[`‘]([^‘’`]+)['’] \(imported from (.*)\).*""".r
  private final val PerhapsYouMeantLocalPattern = """.*[`‘]([^‘’`]+)['’].*""".r

  private final val DeprecatedPattern = """.*In the use of.*[‘`](.*)[’'].*Deprecated: "Use ([^ ]+).*"""".r

  private final val HolePattern = """warning: \[\-Wtyped\-holes\] (.+)""".r

  // File which could not be loaded because project was not yet build
  final val NotLoadedFile = new ConcurrentHashMap[Project, PsiFile].asScala

  def getDaemonCodeAnalyzer(project: Project): DaemonCodeAnalyzerImpl = {
    DaemonCodeAnalyzer.getInstance(project).asInstanceOf[DaemonCodeAnalyzerImpl]
  }

  def restartDaemonCodeAnalyzerForFile(psiFile: PsiFile): Unit = {
    ApplicationManager.getApplication.invokeLater {
      () => {
        if (!psiFile.getProject.isDisposed) {
          HaskellNotificationGroup.logInfoEvent(psiFile.getProject, s"Restart daemon code analyzer for file: ${psiFile.getName}")
          getDaemonCodeAnalyzer(psiFile.getProject).restart(psiFile)
        }
      }
    }
  }

  def findHighlightInfo(project: Project, offset: Int, editor: Editor): Option[HighlightInfo] = {
    Option(getDaemonCodeAnalyzer(project).findHighlightByOffset(editor.getDocument, offset, false))
  }

  private def createAnnotations(project: Project, psiFile: PsiFile, problems: Iterable[CompilationProblem]): Iterable[Annotation] = {

    def createErrorAnnotationWithMultiplePerhapsIntentions(problem: CompilationProblem, tr: TextRange, notInScopeMessage: String, suggestionsList: String, add: Option[(String, String)]) = {
      val notInScopeName = extractName(notInScopeMessage)
      val annotations = suggestionsList.split(",").flatMap(s => extractPerhapsYouMeantAction(s))
      ErrorAnnotationWithIntentionActions(tr, problem.plainMessage, problem.htmlMessage, annotations.toStream ++ createNotInScopeIntentionActions(psiFile, notInScopeName) ++ add.map(a => new NotInScopeIntentionAction(a._2, a._1, psiFile)))
    }

    problems.flatMap {
      problem =>
        val textRange = getProblemTextRange(psiFile, problem)
        textRange.map {
          tr =>
            val plainMessage = problem.plainMessage.replaceAll(" •", "")
            plainMessage match {
              // Because of setting `-fdefer-type-errors` the following problems are displayed as error
              case PerhapsYouMeantSingleMultiplePattern(notInScopeMessage, suggestionsList, _, addName, addModule) =>
                createErrorAnnotationWithMultiplePerhapsIntentions(problem, tr, notInScopeMessage, suggestionsList, Some((addModule, addName)))
              case PerhapsYouMeantSingleMultiplePattern3(notInScopeMessage, suggestionsList, addName, addModule) =>
                createErrorAnnotationWithMultiplePerhapsIntentions(problem, tr, notInScopeMessage, suggestionsList, Some((addModule, addName)))
              case PerhapsYouMeantSingleMultiplePattern2(notInScopeMessage, addName, addModule) =>
                createErrorAnnotationWithMultiplePerhapsIntentions(problem, tr, notInScopeMessage, "", Some(addModule, addName))
              case PerhapsYouMeantMultiplePattern(notInScopeMessage, suggestionsList) =>
                createErrorAnnotationWithMultiplePerhapsIntentions(problem, tr, notInScopeMessage, suggestionsList, None)
              case PerhapsYouMeantSinglePattern(notInScopeMessage, suggestion) =>
                val notInScopeName = extractName(notInScopeMessage)
                val annotation = extractPerhapsYouMeantAction(suggestion)
                ErrorAnnotationWithIntentionActions(tr, problem.plainMessage, problem.htmlMessage, annotation.toStream ++ createNotInScopeIntentionActions(psiFile, notInScopeName))
              case NotInScopePattern(name) =>
                ErrorAnnotationWithIntentionActions(tr, problem.plainMessage, problem.htmlMessage, createNotInScopeIntentionActions(psiFile, name))
              case NotInScopePattern2(name) =>
                ErrorAnnotationWithIntentionActions(tr, problem.plainMessage, problem.htmlMessage, createNotInScopeIntentionActions(psiFile, name.split("::").headOption.getOrElse(name).trim))
              case UseAloneInstancesImportPattern(importDecl, useImport) => importAloneInstancesAction(problem, tr, importDecl, useImport)
              case HolePattern(_) =>
                ErrorAnnotation(tr, problem.plainMessage, problem.htmlMessage)
              //
              case NoTypeSignaturePattern(typeSignature) => WarningAnnotationWithIntentionActions(tr, problem.plainMessage, problem.htmlMessage, Iterable(new TypeSignatureIntentionAction(typeSignature)))
              case HaskellImportOptimizer.WarningRedundantImport(moduleName) => WarningAnnotationWithIntentionActions(tr, problem.plainMessage, problem.htmlMessage, Iterable(new OptimizeImportIntentionAction(moduleName, None)))
              case HaskellImportOptimizer.WarningRedundant2Import(idNames, moduleName) => WarningAnnotationWithIntentionActions(tr, problem.plainMessage, problem.htmlMessage, Iterable(new OptimizeImportIntentionAction(moduleName, Some(idNames))))
              case DefinedButNotUsedPattern(n) => WarningAnnotationWithIntentionActions(tr, problem.plainMessage, problem.htmlMessage, Iterable(new DefinedButNotUsedRemoveIntentionAction(n), new DefinedButNotUsedUnderscoreIntentionAction(n)))
              case DeprecatedPattern(name, suggestion) => WarningAnnotationWithIntentionActions(tr, problem.plainMessage, problem.htmlMessage, Iterable(new DeprecatedUseAction(name, StringUtil.removeOuterQuotes(suggestion))))
              case _ =>
                findSuggestedLanguageExtension(project, plainMessage) match {
                  case les if les.nonEmpty => createLanguageExtensionIntentionsAction(problem, tr, les)
                  case _ =>
                    if (problem.isWarning && !plainMessage.startsWith("warning: [-Wdeferred-type-errors]") && !plainMessage.startsWith("warning: [-Wdeferred-type-holes]"))
                      WarningAnnotation(tr, problem.plainMessage, problem.htmlMessage)
                    else
                      ErrorAnnotation(tr, problem.plainMessage, problem.htmlMessage)
                }
            }
        }
    }
  }

  private def findSuggestedLanguageExtension(project: Project, message: String) = {
    val lanuageExtensions = HaskellComponentsManager.getSupportedLanguageExtension(project)
    lanuageExtensions.filter(message.contains)
  }

  private def extractPerhapsYouMeantAction(suggestion: String): Option[PerhapsYouMeantIntentionAction] = {
    suggestion match {
      case message@PerhapsYouMeantImportedFromPattern(name, _) => Some(new PerhapsYouMeantIntentionAction(name, message))
      case message@PerhapsYouMeantLocalPattern(name) => Some(new PerhapsYouMeantIntentionAction(name, message))
      case _ => None
    }
  }

  private def extractName(notInScopeMessage: String): String = {
    notInScopeMessage match {
      case PerhapsYouMeantNamePattern(name) => name
      case _ => notInScopeMessage.split("::").headOption.getOrElse(notInScopeMessage).trim.replaceAll("‘’'`•", "")
    }
  }

  private def createNotInScopeIntentionActions(psiFile: PsiFile, name: String): Iterable[NotInScopeIntentionAction] = {
    import scala.concurrent.ExecutionContext.Implicits.global

    val nameWithoutParens = StringUtil.removeOuterParens(name)
    val moduleIdentifiers = HaskellComponentsManager.findModuleIdentifiersInCache(psiFile.getProject).filter(_.name == nameWithoutParens)
    moduleIdentifiers.map(mi => new NotInScopeIntentionAction(mi.name, mi.moduleName, psiFile))
  }

  private def createLanguageExtensionIntentionsAction(problem: CompilationProblem, tr: TextRange, languageExtensions: Iterable[String]): ErrorAnnotationWithIntentionActions = {
    ErrorAnnotationWithIntentionActions(tr, problem.plainMessage, problem.htmlMessage, languageExtensions.map(le => new LanguageExtensionIntentionAction(le)))
  }

  private def importAloneInstancesAction(problem: CompilationProblem, tr: TextRange, importDecl: String, useImport: String): WarningAnnotationWithIntentionActions = {
    WarningAnnotationWithIntentionActions(tr, problem.plainMessage, problem.htmlMessage, Stream(new ImportAloneInstancesAction(importDecl), new OptimizeImportIntentionAction(importDecl, None)))
  }

  private def getProblemTextRange(psiFile: PsiFile, problem: CompilationProblem): Option[TextRange] = {
    HaskellFileUtil.findVirtualFile(psiFile).flatMap(vf => LineColumnPosition.getOffset(vf, LineColumnPosition(problem.lineNr, problem.columnNr)).map(offset => {
      findTextRange(psiFile, offset)
    }))
  }

  private def findTextRange(psiFile: PsiFile, offset: Int): TextRange = {
    Option(psiFile.findElementAt(offset)) match {
      case Some(e: HaskellNamedElement) => e.getTextRange
      case Some(e) => Option(PsiTreeUtil.findFirstParent(e, HaskellElementCondition.QualifiedNameElementCondition)).map(_.getTextRange).getOrElse(e.getTextRange)
      case None => findTextRangeLastElement(offset, psiFile).getOrElse(TextRange.create(0, 0))
    }
  }

  @tailrec
  private def findTextRangeLastElement(offset: Int, psiFile: PsiFile): Option[TextRange] = {
    if (offset > 0) {
      Option(psiFile.findElementAt(offset)) match {
        case Some(e) => Some(e.getTextRange)
        case None => findTextRangeLastElement(offset - 1, psiFile)
      }
    }
    else {
      None
    }
  }
}

private sealed trait Annotation {
  def textRange: TextRange

  def message: String
}

private case class ErrorAnnotation(textRange: TextRange, message: String, htmlMessage: String) extends Annotation

private case class ErrorAnnotationWithIntentionActions(textRange: TextRange, message: String, htmlMessage: String, baseIntentionActions: Iterable[HaskellBaseIntentionAction]) extends Annotation

private case class WarningAnnotation(textRange: TextRange, message: String, htmlMessage: String) extends Annotation

private case class WarningAnnotationWithIntentionActions(textRange: TextRange, message: String, htmlMessage: String, baseIntentionActions: Iterable[HaskellBaseIntentionAction]) extends Annotation

sealed abstract class HaskellBaseIntentionAction extends BaseIntentionAction {
  override def isAvailable(project: Project, editor: Editor, file: PsiFile): Boolean = {
    file.isInstanceOf[HaskellFile]
  }
}

class TypeSignatureIntentionAction(typeSignature: String) extends HaskellBaseIntentionAction {
  setText(s"Add type signature `$typeSignature`")

  override def getFamilyName: String = "Add type signature"

  override def invoke(project: Project, editor: Editor, file: PsiFile): Unit = {
    val offset = editor.getCaretModel.getOffset
    Option(file.findElementAt(offset)) match {
      case Some(e) =>
        for {
          topDeclaration <- Option(TreeUtil.findParent(e.getNode, HaskellTypes.HS_TOP_DECLARATION))
          psi <- Option(topDeclaration.getPsi)
          moduleBody <- Option(psi.getParent)
          typeSignatureElement <- HaskellElementFactory.createTopDeclarationLine(project, typeSignature)
          typeSignature = moduleBody.addBefore(typeSignatureElement, psi)
        } yield moduleBody.addAfter(HaskellElementFactory.createNewLine(project), typeSignature)
      case None => ()
    }
  }
}

class LanguageExtensionIntentionAction(languageExtension: String) extends HaskellBaseIntentionAction {
  setText(s"Add language extension `$languageExtension`")

  override def getFamilyName: String = "Add language extension"

  override def invoke(project: Project, editor: Editor, file: PsiFile): Unit = {
    val languagePragmaElement = HaskellElementFactory.createLanguagePragma(project, s"{-# LANGUAGE $languageExtension #-}\n")
    Option(PsiTreeUtil.findChildOfType(file, classOf[HaskellFileHeader])) match {
      case Some(fh) =>
        val lastPragmaElement = PsiTreeUtil.findChildrenOfType(fh, classOf[HaskellPragma]).asScala.lastOption.orNull
        if (lastPragmaElement == null) {
          val p = fh.add(languagePragmaElement)
          fh.addAfter(HaskellElementFactory.createNewLine(project), p)
        } else {
          val nl = fh.addAfter(HaskellElementFactory.createNewLine(project), lastPragmaElement)
          fh.addAfter(languagePragmaElement, nl)
        }
      case None => () // File header should always be there
    }
  }
}

class PerhapsYouMeantIntentionAction(suggestion: String, message: String) extends HaskellBaseIntentionAction {
  setText(s"Perhaps you meant: `$suggestion`  ($message)")

  override def getFamilyName: String = "Perhaps you meant"

  override def invoke(project: Project, editor: Editor, file: PsiFile): Unit = {
    IntentionHelper.replace(project, editor, file, suggestion)
  }
}

class DeprecatedUseAction(name: String, suggestion: String) extends HaskellBaseIntentionAction {
  setText(s"`$name` is deprecated. Use `$suggestion`")

  override def getFamilyName: String = "Deprecated"

  override def invoke(project: Project, editor: Editor, file: PsiFile): Unit = {
    IntentionHelper.replace(project, editor, file, suggestion)
  }
}

private object IntentionHelper {
  def replace(project: Project, editor: Editor, file: PsiFile, newName: String): Unit = {
    val offset = editor.getCaretModel.getOffset
    Option(file.findElementAt(offset)).flatMap(HaskellPsiUtil.findQualifiedName) match {
      case Some(e) =>
        if (e.getText.startsWith("`") && e.getText.endsWith("`")) {
          e.replace(HaskellElementFactory.createQualifiedNameElement(project, s"`$newName`"))
        } else if (StringUtil.isWithinParens(e.getText)) {
          e.replace(HaskellElementFactory.createQualifiedNameElement(project, s"($newName)"))
        } else {
          e.replace(HaskellElementFactory.createQualifiedNameElement(project, newName))
        }
      case None => ()
    }
  }
}

class DefinedButNotUsedRemoveIntentionAction(name: String) extends HaskellBaseIntentionAction {
  setText(s"Remove: `$name`")

  override def getFamilyName: String = "Defined but not used"

  override def invoke(project: Project, editor: Editor, file: PsiFile): Unit = {
    val offset = editor.getCaretModel.getOffset
    Option(file.findElementAt(offset)).foreach(_.delete())
  }
}

class DefinedButNotUsedUnderscoreIntentionAction(name: String) extends HaskellBaseIntentionAction {
  setText(s"Replace by `_`")

  override def getFamilyName: String = "Defined but not used"

  override def invoke(project: Project, editor: Editor, file: PsiFile): Unit = {
    val offset = editor.getCaretModel.getOffset
    for {
      e <- Option(file.findElementAt(offset))
      u <- HaskellElementFactory.createUnderscore(project)
    } yield e.replace(u)
  }
}

class NotInScopeIntentionAction(identifier: String, moduleName: String, psiFile: PsiFile) extends HaskellBaseIntentionAction {
  setText(s"Import `$identifier` of module `$moduleName`")

  override def getFamilyName: String = "Perhaps you meant"

  override def invoke(project: Project, editor: Editor, file: PsiFile): Unit = {
    HaskellElementFactory.createImportDeclaration(project, moduleName, identifier).foreach(importDeclarationElement =>
      Option(PsiTreeUtil.findChildOfType(file, classOf[HaskellImportDeclarations])) match {
        case Some(ids) if !ids.getImportDeclarationList.isEmpty =>
          ids.getImportDeclarationList.asScala.find(d => d.getModuleName.contains(moduleName)) match {
            case Some(id) =>
              val parent = Option(id.getImportSpec).flatMap(s => Option(s.getImportIdsSpec))
              parent match {
                case Some(importIdsSpec) =>
                  importIdsSpec.getImportIdList.asScala.lastOption.foreach(importId => {
                    val commaElement = importIdsSpec.addAfter(HaskellElementFactory.createComma(project), importId)
                    HaskellElementFactory.createImportId(project, identifier).foreach { ii =>
                      val iiElement = importIdsSpec.addAfter(ii, commaElement)
                      importIdsSpec.addBefore(HaskellElementFactory.createWhiteSpace(project), iiElement)
                    }
                  })
                case None => createImportDeclaration(importDeclarationElement, ids, project)
              }
            case None =>
              createImportDeclaration(importDeclarationElement, ids, project)
          }
        case _ =>
          HaskellPsiUtil.findModuleDeclaration(psiFile) match {
            case Some(md) =>
              val newLine = md.getParent.addAfter(HaskellElementFactory.createNewLine(project), md.getNextSibling)
              val bla = md.getParent.addAfter(importDeclarationElement, newLine)
              md.getParent.addAfter(HaskellElementFactory.createNewLine(project), bla)
            case None =>
              file.add(importDeclarationElement)
          }
      }
    )
  }

  private def createImportDeclaration(importDeclarationElement: HaskellImportDeclaration, ids: HaskellImportDeclarations, project: Project) = {
    HaskellPsiUtil.findImportDeclarations(psiFile).lastOption match {
      case Some(id) =>
        val nla = ids.addAfter(HaskellElementFactory.createNewLine(project), id)
        ids.addAfter(importDeclarationElement, nla)
      case None =>
        val importElement = ids.addAfter(importDeclarationElement, null)
        ids.addAfter(HaskellElementFactory.createNewLine(project), importElement)
    }
  }
}

class OptimizeImportIntentionAction(moduleName: String, mids: Option[String]) extends HaskellBaseIntentionAction {
  setText(s"Remove redundant import for `$moduleName`" + mids.getOrElse(""))

  override def getFamilyName: String = "Optimize imports"

  override def invoke(project: Project, editor: Editor, psiFile: PsiFile): Unit = {
    mids match {
      case None => HaskellImportOptimizer.removeRedundantImport(psiFile, moduleName)
      case Some(ids) => HaskellImportOptimizer.removeRedundantImportIds(psiFile, moduleName, ids.split(',').map(_.trim))
    }
  }
}

class ImportAloneInstancesAction(importDecl: String) extends HaskellBaseIntentionAction {
  setText(s"Import alone instance `$importDecl`")

  override def getFamilyName: String = "Import alone instance"

  override def invoke(project: Project, editor: Editor, file: PsiFile): Unit = {
    val offset = editor.getCaretModel.getOffset
    Option(file.findElementAt(offset)) match {
      case Some(e) =>
        for {
          importDeclarations <- HaskellPsiUtil.findImportDeclarations(e)
          importDeclaration <- HaskellPsiUtil.findImportDeclaration(e)
          importDeclElement <- HaskellElementFactory.createImportDeclaration(project, importDecl)
        } yield importDeclarations.getNode.replaceChild(importDeclaration.getNode, importDeclElement.getNode)
      case None => ()
    }
  }

}
