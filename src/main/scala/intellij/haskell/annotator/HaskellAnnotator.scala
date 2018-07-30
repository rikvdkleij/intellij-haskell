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
import com.intellij.openapi.progress.ProgressManager
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
    if (HaskellConsoleView.isConsoleFile(psiFile) || HaskellProjectUtil.isLibraryFile(psiFile)) {
      null
    } else if (StackProjectManager.isBuilding(psiFile.getProject)) {
      val project = psiFile.getProject
      // Last file is leading
      HaskellNotificationGroup.logInfoEvent(project, s"File ${psiFile.getName} could not be loaded because project was still building")
      HaskellAnnotator.NotLoadedFile.put(project, psiFile)
      null
    } else {
      (psiFile, HaskellFileUtil.findVirtualFile(psiFile)) match {
        case (_, None) => null // can be in case if file is in memory only (just created file)
        case (_, Some(f)) if f.getFileType != HaskellFileType.Instance => null
        case (_, Some(_)) if !psiFile.isValid => null
        case (_, Some(_)) =>
          val currentElement = Option(psiFile.findElementAt(editor.getCaretModel.getOffset)).
            find(e => HaskellPsiUtil.findExpressionParent(e).isDefined).
            flatMap(e => Option(PsiTreeUtil.prevVisibleLeaf(e))).filter(_.isValid)
          (psiFile, currentElement)
      }
    }
  }

  override def doAnnotate(psiFileElement: (PsiFile, Option[PsiElement])): CompilationResult = {
    ProgressManager.checkCanceled()
    val psiFile = psiFileElement._1
    val fileChanged = HaskellFileUtil.findVirtualFile(psiFile).exists(FileDocumentManager.getInstance().isFileModified)

    ProgressManager.checkCanceled()

    if (fileChanged) {
      ApplicationManager.getApplication.invokeAndWait(() => {
        if (!psiFile.getProject.isDisposed) {
          ProgressManager.checkCanceled()
          HaskellFileUtil.saveFile(psiFile, checkCancelled = true)
        }
      })
    }

    ProgressManager.checkCanceled()
    HaskellComponentsManager.loadHaskellFile(psiFile, fileChanged, psiFileElement._2).orNull
  }

  override def apply(psiFile: PsiFile, loadResult: CompilationResult, holder: AnnotationHolder): Unit = {
    val haskellProblemsView = ProblemsView.SERVICE.getInstance(psiFile.getProject).asInstanceOf[HaskellProblemsView]
    val project = psiFile.getProject

    HaskellFileUtil.findVirtualFile(psiFile).foreach { currentFile =>
      ApplicationManager.getApplication.invokeLater { () =>
        if (!project.isDisposed) {
          haskellProblemsView.clearProgress()
          haskellProblemsView.clearOldMessages(currentFile)

          for (problem <- loadResult.currentFileProblems) {
            val message = createCompilerMessage(currentFile, project, problem)
            haskellProblemsView.addMessage(message)
          }

          for (problem <- loadResult.otherFileProblems) {
            HaskellProjectUtil.findVirtualFile(problem.filePath, project).foreach { file =>
              val message = createCompilerMessage(file, project, problem)
              haskellProblemsView.addMessage(message)
            }
          }
        }
      }
    }

    HaskellCompilationResultHelper.createNotificationsForErrorsNotInCurrentFile(project, loadResult)

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
    val category = if (problem.isWarning) CompilerMessageCategory.WARNING else CompilerMessageCategory.ERROR
    new CompilerMessageImpl(project, category, problem.message, file, problem.lineNr, problem.columnNr, null)
  }
}

object HaskellAnnotator {

  private final val NoTypeSignaturePattern = """.* Top-level binding with no type signature: (.+)""".r
  private final val DefinedButNotUsedPattern = """.* Defined but not used: [‘`](.+)[’']""".r
  private final val NotInScopePattern = """.*ot in scope:[^‘`']*[‘`']([^’']+)[’'].*""".r
  private final val NotInScopePattern2 = """.*ot in scope: ([^ ]+).*""".r
  private final val UseAloneInstancesImportPattern = """.* To import instances alone, use: (.+)""".r
  private final val RedundantImportPattern = """.* The import of [‘`](.*)[’'] from module [‘`](.*)[’'] is redundant""".r

  private final val PerhapsYouMeantNamePattern = """.*[`‘]([^‘’'`]+)['’]""".r
  private final val PerhapsYouMeantMultiplePattern = """.*ot in scope: (.+) Perhaps you meant one of these: (.+)""".r
  private final val PerhapsYouMeantSingleMultiplePattern = """.*ot in scope: (.+) Perhaps you meant one of these: (.+) Perhaps you want to add .*""".r
  private final val PerhapsYouMeantSinglePattern = """.*ot in scope: (.+) Perhaps you meant (.+)""".r
  private final val PerhapsYouMeantImportedFromPattern = """.*[`‘]([^‘’'`]+)['’] \(imported from (.*)\)""".r
  private final val PerhapsYouMeantLocalPattern = """.*[`‘]([^‘’'`]+)['’].*""".r

  private final val HolePattern = """.* Found hole: (.+) Where: .*""".r
  private final val HolePattern2 = """.* Found hole [`‘]([^‘’'`]+)['’] with type: ([^ ]+) .*""".r

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

  private def createAnnotations(project: Project, psiFile: PsiFile, problems: Iterable[CompilationProblem]): Iterable[Annotation] = {

    def createErrorAnnotationWithMultiplePerhapsIntentions(problem: CompilationProblem, tr: TextRange, notInScopeMessage: String, suggestionsList: String) = {
      val notInScopeName = extractName(notInScopeMessage)
      val annotations = suggestionsList.split(",").flatMap(s => extractPerhapsYouMeantAction(s))
      ErrorAnnotationWithIntentionActions(tr, problem.plainMessage, problem.htmlMessage, annotations.toStream ++ createNotInScopeIntentionActions(psiFile, notInScopeName))
    }

    problems.flatMap {
      problem =>
        val textRange = getProblemTextRange(psiFile, problem)
        textRange.map {
          tr =>
            val plainMessage = problem.plainMessage
            plainMessage match {
              // Because of setting `-fdefer-type-errors` the following problems are displayed as error
              case PerhapsYouMeantSingleMultiplePattern(notInScopeMessage, suggestionsList) =>
                createErrorAnnotationWithMultiplePerhapsIntentions(problem, tr, notInScopeMessage, suggestionsList)
              case PerhapsYouMeantMultiplePattern(notInScopeMessage, suggestionsList) =>
                createErrorAnnotationWithMultiplePerhapsIntentions(problem, tr, notInScopeMessage, suggestionsList)
              case PerhapsYouMeantSinglePattern(notInScopeMessage, suggestion) =>
                val notInScopeName = extractName(notInScopeMessage)
                val annotation = extractPerhapsYouMeantAction(suggestion)
                ErrorAnnotationWithIntentionActions(tr, problem.plainMessage, problem.htmlMessage, annotation.toStream ++ createNotInScopeIntentionActions(psiFile, notInScopeName))
              case NotInScopePattern(name) =>
                ErrorAnnotationWithIntentionActions(tr, problem.plainMessage, problem.htmlMessage, createNotInScopeIntentionActions(psiFile, name))
              case NotInScopePattern2(name) =>
                ErrorAnnotationWithIntentionActions(tr, problem.plainMessage, problem.htmlMessage, createNotInScopeIntentionActions(psiFile, name.split("::").headOption.getOrElse(name).trim))
              case UseAloneInstancesImportPattern(importDecl) => importAloneInstancesAction(problem, tr, importDecl)
              case RedundantImportPattern(redundants, moduleName) => redundantImportAction(problem, tr, moduleName, redundants)
              case HolePattern(_) =>
                ErrorAnnotation(tr, problem.plainMessage, problem.htmlMessage)
              case HolePattern2(_, _) =>
                ErrorAnnotation(tr, problem.plainMessage, problem.htmlMessage)
              //
              case NoTypeSignaturePattern(typeSignature) => WarningAnnotationWithIntentionActions(tr, problem.plainMessage, problem.htmlMessage, Iterable(new TypeSignatureIntentionAction(typeSignature)))
              case HaskellImportOptimizer.WarningRedundantImport(moduleName) => WarningAnnotationWithIntentionActions(tr, problem.plainMessage, problem.htmlMessage, Iterable(new OptimizeImportIntentionAction(moduleName, tr.getStartOffset)))
              case DefinedButNotUsedPattern(n) => WarningAnnotationWithIntentionActions(tr, problem.plainMessage, problem.htmlMessage, Iterable(new DefinedButNotUsedRemoveIntentionAction(n), new DefinedButNotUsedUnderscoreIntentionAction(n)))
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
    val moduleIdentifiers = HaskellComponentsManager.findPreloadedModuleIdentifiers(psiFile.getProject).filter(_.name == nameWithoutParens)
    moduleIdentifiers.map(mi => new NotInScopeIntentionAction(mi.name, mi.moduleName, psiFile))
  }

  private def createLanguageExtensionIntentionsAction(problem: CompilationProblem, tr: TextRange, languageExtensions: Iterable[String]): ErrorAnnotationWithIntentionActions = {
    ErrorAnnotationWithIntentionActions(tr, problem.plainMessage, problem.htmlMessage, languageExtensions.map(le => new LanguageExtensionIntentionAction(le)))
  }

  private def importAloneInstancesAction(problem: CompilationProblem, tr: TextRange, importDecl: String): WarningAnnotationWithIntentionActions = {
    WarningAnnotationWithIntentionActions(tr, problem.plainMessage, problem.htmlMessage, Stream(new ImportAloneInstancesAction(importDecl)))
  }

  private def redundantImportAction(problem: CompilationProblem, tr: TextRange, moduleName: String, redundants: String): WarningAnnotationWithIntentionActions = {
    WarningAnnotationWithIntentionActions(tr, problem.plainMessage, problem.htmlMessage, Stream(new RedundantImportAction(moduleName, redundants)))
  }

  private def getProblemTextRange(psiFile: PsiFile, problem: CompilationProblem): Option[TextRange] = {
    HaskellFileUtil.findVirtualFile(psiFile).flatMap(vf =>  LineColumnPosition.getOffset(vf, LineColumnPosition(problem.lineNr, problem.columnNr)).map(offset => {
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
          typeSignatureElement <- HaskellElementFactory.createTopDeclaration(project, typeSignature)
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
    val languagePragmaElement = HaskellElementFactory.createLanguagePragma(project, s"{-# LANGUAGE $languageExtension #-}")
    Option(PsiTreeUtil.findChildOfType(file, classOf[HaskellFileHeader])) match {
      case Some(fh) =>
        val lastPragmaElement = PsiTreeUtil.findChildrenOfType(fh, classOf[HaskellFileHeaderPragma]).asScala.lastOption.orNull
        val newline = fh.addAfter(HaskellElementFactory.createNewLine(project), lastPragmaElement)
        fh.addAfter(languagePragmaElement, newline)
      case None => Option(file.getFirstChild) match {
        case Some(c) =>
          val addedPragmaElement = file.addBefore(languagePragmaElement, c)
          file.addAfter(HaskellElementFactory.createNewLine(project), addedPragmaElement)
        case None => file.add(languagePragmaElement)
      }
    }
  }
}

class PerhapsYouMeantIntentionAction(suggestion: String, message: String) extends HaskellBaseIntentionAction {
  setText(s"Perhaps you meant: `$suggestion`  ($message)")

  override def getFamilyName: String = "Perhaps you meant"

  override def invoke(project: Project, editor: Editor, file: PsiFile): Unit = {
    val offset = editor.getCaretModel.getOffset
    Option(file.findElementAt(offset)).flatMap(e => Option(PsiTreeUtil.findFirstParent(e, HaskellElementCondition.QualifiedNameElementCondition))) match {
      case Some(e) =>
        if (e.getText.startsWith("`") && e.getText.endsWith("`")) {
          e.replace(HaskellElementFactory.createQualifiedNameElement(project, s"`$suggestion`"))
        } else if (DeclarationLineUtil.isOperator(e.getText)) {
          e.replace(HaskellElementFactory.createQualifiedNameElement(project, s"($suggestion)"))
        } else {
          e.replace(HaskellElementFactory.createQualifiedNameElement(project, suggestion))
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
                    HaskellElementFactory.createImportId(project, identifier).foreach(mi => importIdsSpec.addAfter(mi, commaElement))
                  })
                case None => createImportDeclaration(importDeclarationElement, ids)
              }
            case None =>
              createImportDeclaration(importDeclarationElement, ids)
          }
        case _ =>
          HaskellPsiUtil.findModuleDeclaration(psiFile) match {
            case Some(md) =>
              val newLine = md.getParent.addAfter(HaskellElementFactory.createNewLine(project), md.getNextSibling)
              md.getParent.addAfter(importDeclarationElement, newLine)
            case None => file.add(importDeclarationElement)
          }
      }
    )
  }

  private def createImportDeclaration(importDeclarationElement: HaskellImportDeclaration, ids: HaskellImportDeclarations) = {
    val lastImportDeclaration = HaskellPsiUtil.findImportDeclarations(psiFile).lastOption.orNull
    ids.addAfter(importDeclarationElement, lastImportDeclaration)
  }
}

class OptimizeImportIntentionAction(moduleName: String, offset: Int) extends HaskellBaseIntentionAction {
  setText(s"Remove redundant import for `$moduleName`")

  override def getFamilyName: String = "Optimize imports"

  override def invoke(project: Project, editor: Editor, psiFile: PsiFile): Unit = {
    HaskellImportOptimizer.removeRedundantImport(psiFile, offset)
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
          importDeclarations <- HaskellPsiUtil.findImportDeclarationsParent(e)
          importDeclaration <- HaskellPsiUtil.findImportDeclarationParent(e)
          importDeclElement <- HaskellElementFactory.createImportDeclaration(project, importDecl)
        } yield importDeclarations.getNode.replaceChild(importDeclaration.getNode, importDeclElement.getNode)
      case None => ()
    }
  }
}

class RedundantImportAction(moduleName: String, redundants: String) extends HaskellBaseIntentionAction {
  setText(s"Remove redundant import `$moduleName` ($redundants)")

  override def getFamilyName: String = "Redundant import"

  def removeRedundants(spec: String, redundants: String): String = {
    val specArr = spec.stripPrefix("(").stripSuffix(")").split(",").map(_.trim)
    val redundantsArr = redundants.split(",").map(_.trim)
    specArr.filterNot(e => redundantsArr.contains(e.replace("(..)", ""))).mkString(", ")
  }

  override def invoke(project: Project, editor: Editor, file: PsiFile): Unit = {
    val offset = editor.getCaretModel.getOffset
    Option(file.findElementAt(offset)) match {
      case Some(e) =>
        for {
          importDeclarations <- HaskellPsiUtil.findImportDeclarationsParent(e)
          importDeclaration <- HaskellPsiUtil.findImportDeclarationParent(e)
          importSpec <- Option(PsiTreeUtil.findChildOfType(importDeclaration, classOf[HaskellImportSpec]))
          importDeclElement <- HaskellElementFactory.createImportDeclaration(project, moduleName, removeRedundants(importSpec.getText, redundants))
        } yield importDeclarations.getNode.replaceChild(importDeclaration.getNode, importDeclElement.getNode)
      case None => ()
    }
  }
}
