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

package intellij.haskell.annotator

import javax.swing.event.HyperlinkEvent

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.codeInsight.daemon.impl._
import com.intellij.codeInsight.intention.impl.BaseIntentionAction
import com.intellij.lang.annotation.{AnnotationHolder, ExternalAnnotator, HighlightSeverity}
import com.intellij.notification.Notification
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.{FileEditorManager, OpenFileDescriptor}
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.source.tree.TreeUtil
import com.intellij.psi.util.PsiTreeUtil
import intellij.haskell.editor.HaskellImportOptimizer
import intellij.haskell.external.component._
import intellij.haskell.psi._
import intellij.haskell.util._
import intellij.haskell.{HaskellFile, HaskellFileType, HaskellNotificationGroup}

import scala.annotation.tailrec
import scala.collection.Iterable
import scala.collection.JavaConverters._

class HaskellAnnotator extends ExternalAnnotator[PsiFile, LoadResult] {

  private final val NoTypeSignaturePattern = """.* Top-level binding with no type signature: (.+)""".r
  private final val DefinedButNotUsedPattern = """.* Defined but not used: [‘`](.+)[’']""".r
  private final val NotInScopePattern = """.*Not in scope:[^‘`]+[‘`](.+)[’']""".r
  private final val NotInScopePattern2 = """.* not in scope: (.+)""".r
  private final val UseAloneInstancesImportPattern = """.* To import instances alone, use: (.+)""".r
  private final val RedundantImportPattern = """.* The import of [‘`](.*)[’'] from module [‘`](.*)[’'] is redundant""".r

  private final val PerhapsYouMeantNamePattern = """.*[`‘]([^‘’'`]+)['’]""".r
  private final val PerhapsYouMeantMultiplePattern = """.*ot in scope: (.+) Perhaps you meant one of these: (.+)""".r
  private final val PerhapsYouMeantSinglePattern = """.*ot in scope: (.+) Perhaps you meant (.+)""".r
  private final val PerhapsYouMeantImportedFromPattern = """.*[`‘]([^‘’'`]+)['’] \(imported from (.*)\)""".r
  private final val PerhapsYouMeantLocalPattern = """.*[`‘]([^‘’'`]+)['’].*""".r

  private final val HolePattern = """.* Found hole: (.+) Where: .*""".r
  private final val HolePattern2 = """.* Found hole [`‘]([^‘’'`]+)['’] with type: ([^ ]+) .*""".r

  override def collectInformation(psiFile: PsiFile, editor: Editor, hasErrors: Boolean): PsiFile = {
    ProgressManager.checkCanceled()

    (psiFile, Option(psiFile.getOriginalFile.getVirtualFile)) match {
      case (_, None) => null // can be in case if file is in memory only (just created file)
      case (_, Some(f)) if f.getFileType != HaskellFileType.INSTANCE => null
      case (_, Some(_)) if !psiFile.isValid | HaskellProjectUtil.isLibraryFile(psiFile).getOrElse(true) => null
      case (_, Some(_)) => psiFile
    }
  }

  override def doAnnotate(psiFile: PsiFile): LoadResult = {
    ApplicationManager.getApplication.invokeAndWait(() => {
      HaskellFileUtil.saveFile(psiFile)
    })
    HaskellComponentsManager.loadHaskellFile(psiFile)
  }

  override def apply(psiFile: PsiFile, loadResult: LoadResult, holder: AnnotationHolder): Unit = {
    for (annotation <- createAnnotations(loadResult, psiFile)) {
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
    HaskellAnnotator.getDaemonCodeAnalyzer(psiFile.getProject).restart(psiFile)
  }

  private[annotator] def createAnnotations(loadResult: LoadResult, psiFile: PsiFile): Iterable[Annotation] = {
    val problems = loadResult.currentFileProblems.filter(_.filePath == HaskellFileUtil.getFilePath(psiFile))
    val project = psiFile.getProject
    if (loadResult.loadFailed && loadResult.currentFileProblems.isEmpty) {
      loadResult.otherFileProblems.foreach {
        case cpf: LoadProblemInOtherFile if !cpf.isWarning =>
          HaskellNotificationGroup.logErrorBalloonEvent(project, s"${
            cpf.htmlMessage
          } at <a href='#'>${
            cpf.filePath
          }:${
            cpf.lineNr
          }:${
            cpf.columnNr
          }</a>.",
            (_: Notification, _: HyperlinkEvent) => {
              val file = LocalFileSystem.getInstance().findFileByPath(cpf.filePath)
              new OpenFileDescriptor(project, file, cpf.lineNr - 1, cpf.columnNr - 1).navigate(true)
            })
        case cpf: LoadProblemWithoutLocation if !cpf.isWarning => HaskellNotificationGroup.logErrorBalloonEvent(project, s"Error ${
          cpf.htmlMessage
        }")
        case _ => ()
      }
    }

    problems.flatMap {
      problem =>
        val textRange = getProblemTextRange(psiFile, problem)
        textRange.map {
          tr =>
            val plainMessage = problem.plainMessage
            plainMessage match {
              // Because of setting `-fdefer-typed-holes` the following problems are displayed as error
              case PerhapsYouMeantMultiplePattern(notInScopeMessage, suggestionsList) =>
                val notInScopeName = extractName(notInScopeMessage)
                val annotations = suggestionsList.split(",").flatMap(s => extractPerhapsYouMeantAction(s))
                ErrorAnnotationWithIntentionActions(tr, problem.plainMessage, problem.htmlMessage, annotations.toStream ++ createNotInScopeIntentionActions(psiFile, notInScopeName))
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
              case DefinedButNotUsedPattern(n) => WarningAnnotationWithIntentionActions(tr, problem.plainMessage, problem.htmlMessage, Iterable(new DefinedButNotUsedIntentionAction(n)))
              case _ =>
                findSuggestedLanguageExtension(project, plainMessage) match {
                  case Some(le) => createLanguageExtensionIntentionAction(problem, tr, le)
                  case _ =>
                    if (problem.isWarning)
                      WarningAnnotation(tr, problem.plainMessage, problem.htmlMessage)
                    else
                      ErrorAnnotation(tr, problem.plainMessage, problem.htmlMessage)
                }
            }
        }
    }
  }

  private def findSuggestedLanguageExtension(project: Project, message: String) = {
    val lanuageExtensions = HaskellComponentsManager.findGlobalProjectInfo(project).map(_.languageExtensions)
    lanuageExtensions.flatMap(_.find(message.contains))
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
    val moduleIdentifiers = HaskellComponentsManager.findPreloadedModuleIdentifiers(psiFile.getProject).filter(_.name == name)
    moduleIdentifiers.map(mi => new NotInScopeIntentionAction(mi.name, mi.moduleName, psiFile))
  }

  private def createLanguageExtensionIntentionAction(problem: LoadProblemInCurrentFile, tr: TextRange, languageExtension: String): ErrorAnnotationWithIntentionActions = {
    ErrorAnnotationWithIntentionActions(tr, problem.plainMessage, problem.htmlMessage, Stream(new LanguageExtensionIntentionAction(languageExtension)))
  }

  private def importAloneInstancesAction(problem: LoadProblemInCurrentFile, tr: TextRange, importDecl: String): WarningAnnotationWithIntentionActions = {
    WarningAnnotationWithIntentionActions(tr, problem.plainMessage, problem.htmlMessage, Stream(new ImportAloneInstancesAction(importDecl)))
  }

  private def redundantImportAction(problem: LoadProblemInCurrentFile, tr: TextRange, moduleName: String, redundants: String): WarningAnnotationWithIntentionActions = {
    WarningAnnotationWithIntentionActions(tr, problem.plainMessage, problem.htmlMessage, Stream(new RedundantImportAction(moduleName, redundants)))
  }

  private def getProblemTextRange(psiFile: PsiFile, problem: LoadProblemInCurrentFile): Option[TextRange] = {
    LineColumnPosition.getOffset(psiFile, LineColumnPosition(problem.lineNr, problem.columnNr)).map(offset => {
      findTextRange(psiFile, offset)
    })
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

object HaskellAnnotator {

  def getDaemonCodeAnalyzer(project: Project): DaemonCodeAnalyzerImpl = {
    DaemonCodeAnalyzer.getInstance(project).asInstanceOf[DaemonCodeAnalyzerImpl]
  }

  def restartDaemonCodeAnalyzerForOpenFiles(project: Project): Unit = {
    ApplicationManager.getApplication.invokeLater {
      () => {
        if (!project.isDisposed) {
          val openFiles = FileEditorManager.getInstance(project).getOpenFiles
          val openProjectFiles = openFiles.filterNot(vf => HaskellProjectUtil.isLibraryFile(vf, project).getOrElse(true))
          val openProjectPsiFiles = HaskellFileUtil.convertToHaskellFiles(openProjectFiles.toStream, project)
          openProjectPsiFiles.foreach(pf =>
            getDaemonCodeAnalyzer(project).restart(pf)
          )
        }
      }
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
    val languagePragmaElement = HaskellElementFactory.createLanguagePragma(project, s"{-# LANGUAGE $languageExtension #-} \n")
    Option(PsiTreeUtil.findChildOfType(file, classOf[HaskellFileHeader])) match {
      case Some(fh) =>
        val lastPragmaElement = PsiTreeUtil.findChildrenOfType(fh, classOf[HaskellFileHeaderPragma]).asScala.lastOption.orNull
        fh.addAfter(languagePragmaElement, lastPragmaElement)
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

class DefinedButNotUsedIntentionAction(name: String) extends HaskellBaseIntentionAction {
  setText(s"Remove: `$name`")

  override def getFamilyName: String = "Defined but not used"

  override def invoke(project: Project, editor: Editor, file: PsiFile): Unit = {
    val offset = editor.getCaretModel.getOffset
    Option(file.findElementAt(offset)).foreach(_.delete())
  }
}

class NotInScopeIntentionAction(identifier: String, moduleName: String, psiFile: PsiFile) extends HaskellBaseIntentionAction {
  setText(s"Import `$identifier` of module `$moduleName`")

  override def getFamilyName: String = "Perhaps you meant"

  override def invoke(project: Project, editor: Editor, file: PsiFile): Unit = {
    HaskellElementFactory.createImportDeclaration(project, moduleName, identifier).foreach { importDeclarationElement =>
      Option(PsiTreeUtil.findChildOfType(file, classOf[HaskellImportDeclarations])) match {
        case Some(ids) if !ids.getImportDeclarationList.isEmpty =>
          val lastImportDeclaration = HaskellPsiUtil.findImportDeclarations(psiFile).lastOption.orNull
          ids.addAfter(importDeclarationElement, lastImportDeclaration)
        case _ =>
          HaskellPsiUtil.findModuleDeclaration(psiFile) match {
            case Some(md) =>
              val newLine = md.getParent.addAfter(HaskellElementFactory.createNewLine(project), md.getNextSibling)
              md.getParent.addAfter(importDeclarationElement, newLine)
            case None => file.add(importDeclarationElement)
          }
      }
    }
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
