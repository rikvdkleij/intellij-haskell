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

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.codeInsight.daemon.impl.DaemonCodeAnalyzerImpl
import com.intellij.codeInsight.intention.impl.BaseIntentionAction
import com.intellij.lang.annotation.{AnnotationHolder, ExternalAnnotator, HighlightSeverity}
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.source.tree.TreeUtil
import com.intellij.psi.util.PsiTreeUtil
import intellij.haskell.editor.HaskellImportOptimizer
import intellij.haskell.external.component.StackReplsComponentsManager._
import intellij.haskell.external.component._
import intellij.haskell.psi._
import intellij.haskell.util._
import intellij.haskell.{HaskellFile, HaskellFileType, HaskellNotificationGroup}

import scala.annotation.tailrec
import scala.collection.Iterable
import scala.collection.JavaConversions._

class HaskellAnnotator extends ExternalAnnotator[PsiFile, LoadResult] {

  private final val NoTypeSignaturePattern = """.* Top-level binding with no type signature: (.+)""".r
  private final val UseLanguageExtensionPattern = """.* Perhaps you intended to use (\w+).*""".r
  private final val UseLanguageExtensionPattern2 = """.* Use (\w+) to allow.*""".r
  private final val UseLanguageExtensionPattern3 = """.* You need (\w+) to.*""".r
  private final val UseLanguageExtensionPattern4 = """.* Try enabling (\w+).*""".r
  private final val DefinedButNotUsedPattern = """.* Defined but not used: [‘`](.+)[’']""".r
  private final val NotInScopePattern = """Not in scope:[^‘`]+[‘`](.+)[’']""".r
  private final val NotInScopePattern2 = """.* not in scope: (.+)""".r

  private final val PerhapsYouMeantNamePattern = """.*[`‘]([^‘’'`]+)['’]""".r
  private final val PerhapsYouMeantMultiplePattern = """.*ot in scope: (.+) Perhaps you meant one of these: (.+)""".r
  private final val PerhapsYouMeantSinglePattern = """.*ot in scope: (.+) Perhaps you meant (.+)""".r
  private final val PerhapsYouMeantImportedFromPattern = """.*[`‘]([^‘’'`]+)['’] \(imported from (.*)\)""".r
  private final val PerhapsYouMeantLocalPattern = """.*[`‘]([^‘’'`]+)['’].*""".r

  private final val HolePattern = """.* Found hole: (.+) Where: .*""".r
  private final val HolePattern2 = """Found hole [`‘]([^‘’'`]+)['’] with type: ([^ ]+) .*""".r

  override def collectInformation(psiFile: PsiFile, editor: Editor, hasErrors: Boolean): PsiFile = {
    (psiFile, Option(psiFile.getOriginalFile.getVirtualFile)) match {
      case (_, None) => null // can be in case if file is in memory only (just created file)
      case (_, Some(f)) if f.getFileType != HaskellFileType.INSTANCE => null
      case (_, Some(f)) if HaskellProjectUtil.isLibraryFile(psiFile) => null
      case (_, Some(f)) => psiFile
    }
  }

  override def doAnnotate(psiFile: PsiFile): LoadResult = {
    HaskellFileUtil.saveAllFiles()
    StackReplsComponentsManager.loadHaskellFile(psiFile, refreshCache = true)
  }

  override def apply(psiFile: PsiFile, loadResult: LoadResult, holder: AnnotationHolder) {
    if (psiFile.isValid) {
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
    }
    restartCodeAnalyser(psiFile)
  }

  private def restartCodeAnalyser(psiFile: PsiFile) {
    HaskellAnnotator.getDaemonCodeAnalyzer(psiFile.getProject).restart(psiFile)
  }

  private[annotator] def createAnnotations(loadResult: LoadResult, psiFile: PsiFile): Iterable[Annotation] = {
    val problems = loadResult.currentFileProblems.filter(_.filePath == psiFile.getOriginalFile.getVirtualFile.getPath)

    if (loadResult.loadFailed && loadResult.currentFileProblems.isEmpty) {
      loadResult.otherFileProblems.foreach {
        case cpf: LoadProblemInOtherFile => HaskellNotificationGroup.notifyBalloonInfo(s"Error in file `${cpf.filePath}`: ${cpf.htmlMessage}")
        case cpf: LoadProblemWithoutLocation => HaskellNotificationGroup.notifyBalloonInfo(s"Error ${cpf.htmlMessage}")
        case _ => ()
      }
    }

    problems.flatMap { problem =>
      val textRange = getProblemTextRange(psiFile, problem)
      textRange.map { tr =>
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
          case HolePattern(typeSignature) =>
            ErrorAnnotation(tr, problem.plainMessage, problem.htmlMessage)
          case HolePattern2(name, typeOfName) =>
            ErrorAnnotation(tr, problem.plainMessage, problem.htmlMessage)
          //
          case NoTypeSignaturePattern(typeSignature) => WarningAnnotationWithIntentionActions(tr, problem.plainMessage, problem.htmlMessage, Iterable(new TypeSignatureIntentionAction(typeSignature)))
          case HaskellImportOptimizer.WarningRedundantImport(moduleName) => WarningAnnotationWithIntentionActions(tr, problem.plainMessage, problem.htmlMessage, Iterable(new OptimizeImportIntentionAction))
          case DefinedButNotUsedPattern(n) => WarningAnnotationWithIntentionActions(tr, problem.plainMessage, problem.htmlMessage, Iterable(new DefinedButNotUsedIntentionAction(n)))
          case UseLanguageExtensionPattern(languageExtension) => createLanguageExtensionIntentionAction(problem, tr, languageExtension)
          case UseLanguageExtensionPattern2(languageExtension) => createLanguageExtensionIntentionAction(problem, tr, languageExtension)
          case UseLanguageExtensionPattern3(languageExtension) => createLanguageExtensionIntentionAction(problem, tr, languageExtension)
          case UseLanguageExtensionPattern4(languageExtension) => createLanguageExtensionIntentionAction(problem, tr, languageExtension)
          case _ => if (problem.isWarning)
            WarningAnnotation(tr, problem.plainMessage, problem.htmlMessage)
          else
            ErrorAnnotation(tr, problem.plainMessage, problem.htmlMessage)
        }
      }
    }
  }

  private def extractPerhapsYouMeantAction(suggestion: String): Option[PerhapsYouMeantIntentionAction] = {
    suggestion match {
      case message@PerhapsYouMeantImportedFromPattern(name, module) => Some(new PerhapsYouMeantIntentionAction(name, message))
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

  private def createNotInScopeIntentionActions(psiFile: PsiFile, name: String) = {
    val project = psiFile.getProject
    val moduleNames = findAvailableModuleNamesForModuleIdentifiers(psiFile.getProject).toStream
    val moduleIdentifiers = moduleNames.flatMap(mn => findImportedModuleIdentifiers(project, mn).filter(_.name == name))
    moduleIdentifiers.map(mi => new NotInScopeIntentionAction(mi.name, mi.moduleName, psiFile))
  }

  private def createLanguageExtensionIntentionAction(problem: LoadProblemInCurrentFile, tr: TextRange, languageExtension: String): ErrorAnnotationWithIntentionActions = {
    ErrorAnnotationWithIntentionActions(tr, problem.plainMessage, problem.htmlMessage, Stream(new LanguageExtensionIntentionAction(languageExtension)))
  }

  // TODO: If problem position is `(`, create text range up to including `)`
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

  def restartDaemonCodeAnalyzerForOpenFiles(project: Project) = {
    ApplicationManager.getApplication.invokeLater {
      new Runnable {
        override def run(): Unit = {
          if (!project.isDisposed) {
            val openFiles = FileEditorManager.getInstance(project).getOpenFiles
            val openProjectFiles = openFiles.filterNot(vf => HaskellProjectUtil.isLibraryFile(vf, project))
            val openProjectPsiFiles = HaskellFileUtil.convertToHaskellFiles(openProjectFiles.toStream, project)
            openProjectPsiFiles.foreach(pf =>
              getDaemonCodeAnalyzer(project).restart(pf)
            )
          }
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

private case class ErrorAnnotationWithIntentionActions(textRange: TextRange, message: String, htmlMessage: String, baseIntentionActions: Stream[HaskellBaseIntentionAction]) extends Annotation

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
    val languagePragmaElement = HaskellElementFactory.createLanguagePragma(project, s"{-# LANGUAGE $languageExtension #-} ${OSUtil.LineSeparator}")
    Option(PsiTreeUtil.findChildOfType(file, classOf[HaskellFileHeader])) match {
      case Some(fh) =>
        val lastPragmaElement = PsiTreeUtil.findChildrenOfType(fh, classOf[HaskellFileHeaderPragma]).lastOption.orNull
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
        } else if (e.getText.startsWith("(") && e.getText.endsWith(")")) {
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

// TODO: Check when adding import module identifier that import of module is not already there for other identifier of module
class NotInScopeIntentionAction(identifier: String, moduleName: String, psiFile: PsiFile) extends HaskellBaseIntentionAction {
  setText(s"Import `$identifier` of module `$moduleName`")

  override def getFamilyName: String = "Perhaps you meant"

  override def invoke(project: Project, editor: Editor, file: PsiFile): Unit = {
    HaskellElementFactory.createImportDeclaration(project, moduleName, identifier).foreach { importDeclarationElement =>
      Option(PsiTreeUtil.findChildOfType(file, classOf[HaskellImportDeclarations])) match {
        case Some(ids) if ids.getImportDeclarationList.nonEmpty =>
          val lastImportDeclarationElement = PsiTreeUtil.findChildrenOfType(ids, classOf[HaskellImportDeclaration]).lastOption.orNull
          ids.addAfter(importDeclarationElement, lastImportDeclarationElement)
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

// TODO: Pass warnings instead of calling load again
class OptimizeImportIntentionAction extends HaskellBaseIntentionAction {
  setText("Optimize imports")

  override def getFamilyName: String = "Optimize imports"

  override def invoke(project: Project, editor: Editor, file: PsiFile): Unit = {
    new HaskellImportOptimizer().processFile(file).run()
  }
}
