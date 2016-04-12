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

import com.intellij.codeInsight.intention.impl.BaseIntentionAction
import com.intellij.lang.annotation.{AnnotationHolder, ExternalAnnotator}
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.source.tree.TreeUtil
import com.intellij.psi.util.PsiTreeUtil
import intellij.haskell.code.HaskellImportOptimizer
import intellij.haskell.external._
import intellij.haskell.psi._
import intellij.haskell.util.{FileUtil, HaskellElementCondition, LineColumnPosition, OSUtil}
import intellij.haskell.{HaskellFile, HaskellFileType}

import scala.annotation.tailrec
import scala.collection.Iterable
import scala.collection.JavaConversions._

class GhcModExternalAnnotator extends ExternalAnnotator[GhcModInitialInfo, GhcModCheckResult] {

  private final val NoTypeSignaturePattern = """Warning: Top-level binding with no type signature: (.+)""".r
  private final val UseLanguageExtensionPattern = """.*Perhaps you intended to use (\w+) .*""".r
  private final val PerhapsYouMeantPattern = """.*Perhaps you meant(.*)""".r
  private final val SuggestionPattern = """‘([^‘’]+)’ \(([^\(]+)\)""".r

  override def collectInformation(psiFile: PsiFile, editor: Editor, hasErrors: Boolean): GhcModInitialInfo = {
    (psiFile, Option(psiFile.getVirtualFile)) match {
      case (_, None) => null // can be case if file is in memory only (just created file)
      case (_, Some(f)) if f.getFileType != HaskellFileType.INSTANCE => null
      case (_, Some(f)) if f.getPath == null => null
      case (_, Some(f)) => GhcModInitialInfo(psiFile, f.getPath)
    }
  }

  override def doAnnotate(initialInfoGhcMod: GhcModInitialInfo): GhcModCheckResult = {
    FileUtil.saveFile(initialInfoGhcMod.psiFile)
    GhcModCheck.check(initialInfoGhcMod.psiFile.getProject, initialInfoGhcMod)
  }

  override def apply(psiFile: PsiFile, ghcModResult: GhcModCheckResult, holder: AnnotationHolder) {
    if (psiFile.isValid) {
      for (annotation <- createAnnotations(ghcModResult, psiFile)) {
        annotation match {
          case ErrorAnnotation(textRange, message) => holder.createErrorAnnotation(textRange, message)
          case ErrorAnnotationWithIntentionActions(textRange, message, intentionActions) =>
            val annotation = holder.createErrorAnnotation(textRange, message)
            intentionActions.foreach(annotation.registerFix)
          case WarningAnnotation(textRange, message) => holder.createWarningAnnotation(textRange, message)
          case WarningAnnotationWithIntentionActions(textRange, message, intentionActions) =>
            val annotation = holder.createWarningAnnotation(textRange, message)
            intentionActions.foreach(annotation.registerFix)
        }
      }
    }
  }

  private[annotator] def createAnnotations(ghcModCheckResult: GhcModCheckResult, psiFile: PsiFile): Iterable[Annotation] = {
    val problems = ghcModCheckResult.problems.filter(_.filePath == psiFile.getOriginalFile.getVirtualFile.getPath)
    problems.flatMap { problem =>
      val textRange = getProblemTextRange(psiFile, problem)
      textRange.map { tr =>
        val normalizedMessage = problem.getNormalizedMessage
        if (normalizedMessage.startsWith("Warning:")) {
          normalizedMessage match {
            case NoTypeSignaturePattern(typeSignature) => WarningAnnotationWithIntentionActions(tr, problem.message, Iterable(new TypeSignatureIntentionAction(typeSignature)))
            case HaskellImportOptimizer.WarningRedundantImport() => WarningAnnotationWithIntentionActions(tr, problem.message, Iterable(new OptimizeImportIntentionAction))
            case _ => WarningAnnotation(tr, problem.message)
          }
        } else {
          normalizedMessage match {
            case UseLanguageExtensionPattern(languageExtension) => ErrorAnnotationWithIntentionActions(tr, problem.message, Iterable(new LanguageExtensionIntentionAction(languageExtension)))
            case PerhapsYouMeantPattern(suggestions) =>
              val intentionActions = SuggestionPattern.findAllMatchIn(suggestions).map(s => {
                val suggestion = s.group(1)
                val message = s.group(2)
                new PerhapsYouMeantIntentionAction(suggestion, message)
              }).toIterable
              ErrorAnnotationWithIntentionActions(tr, problem.message, intentionActions)
            case _ => ErrorAnnotation(tr, problem.message)
          }
        }
      }
    }
  }

  private def getProblemTextRange(psiFile: PsiFile, problem: GhcModProblem): Option[TextRange] = {
    val ghcModOffset = LineColumnPosition.getOffset(psiFile, LineColumnPosition(problem.lineNr, problem.columnNr))
    ghcModOffset match {
      case Some(offset) => Some(findTextRange(psiFile, offset))
      case None => None
    }
  }

  private def findTextRange(psiFile: PsiFile, offset: Int): TextRange = {
    Option(psiFile.findElementAt(offset)) match {
      case Some(e: HaskellNamedElement) => e.getTextRange
      case Some(e) => Option(PsiTreeUtil.findFirstParent(e, HaskellElementCondition.QVarConOpElementCondition)).map(_.getTextRange).getOrElse(e.getTextRange)
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

case class GhcModInitialInfo(psiFile: PsiFile, filePath: String)

sealed trait Annotation {
  def textRange: TextRange

  def message: String
}

case class ErrorAnnotation(textRange: TextRange, message: String) extends Annotation

case class ErrorAnnotationWithIntentionActions(textRange: TextRange, message: String, baseIntentionActions: Iterable[HaskellBaseIntentionAction]) extends Annotation

case class WarningAnnotation(textRange: TextRange, message: String) extends Annotation

case class WarningAnnotationWithIntentionActions(textRange: TextRange, message: String, baseIntentionActions: Iterable[HaskellBaseIntentionAction]) extends Annotation

abstract class HaskellBaseIntentionAction extends BaseIntentionAction {
  override def isAvailable(project: Project, editor: Editor, file: PsiFile): Boolean = {
    file.isInstanceOf[HaskellFile]
  }
}

class TypeSignatureIntentionAction(typeSignature: String) extends HaskellBaseIntentionAction {
  setText(s"Add type signature: $typeSignature")

  override def getFamilyName: String = "Add type signature"

  override def invoke(project: Project, editor: Editor, file: PsiFile): Unit = {
    val offset = editor.getCaretModel.getOffset
    Option(file.findElementAt(offset)) match {
      case Some(e) =>
        val topDeclaration = TreeUtil.findParent(e.getNode, HaskellTypes.HS_TOP_DECLARATION).getPsi
        val moduleBody = topDeclaration.getParent
        val topTypeSignature = moduleBody.addBefore(HaskellElementFactory.createTopDeclaration(project, typeSignature), topDeclaration)
        moduleBody.addAfter(HaskellElementFactory.createNewLine(project), topTypeSignature)
      case None => ()
    }
  }
}

class LanguageExtensionIntentionAction(languageExtension: String) extends HaskellBaseIntentionAction {
  setText(s"Add language extension: $languageExtension")

  override def getFamilyName: String = "Add language extension"

  override def invoke(project: Project, editor: Editor, file: PsiFile): Unit = {
    val languagePragma = HaskellElementFactory.createLanguagePragma(project, s"{-# LANGUAGE $languageExtension #-} ${OSUtil.LineSeparator}")
    Option(PsiTreeUtil.findChildOfType(file, classOf[HaskellFileHeader])) match {
      case Some(fh) =>
        val lastPragma = PsiTreeUtil.findChildrenOfType(fh, classOf[HaskellFileHeaderPragma]).lastOption.orNull
        fh.addAfter(languagePragma, lastPragma)
      case None => Option(file.getFirstChild) match {
        case Some(c) =>
          val lp = file.addBefore(languagePragma, c)
          file.addAfter(HaskellElementFactory.createNewLine(project), lp)
        case None => file.add(languagePragma)
      }
    }
  }
}

class PerhapsYouMeantIntentionAction(suggestion: String, message: String) extends HaskellBaseIntentionAction {
  setText(s"Perhaps you meant: $suggestion  ($message)")

  override def getFamilyName: String = "Perhaps you meant"

  override def invoke(project: Project, editor: Editor, file: PsiFile): Unit = {
    val offset = editor.getCaretModel.getOffset
    Option(file.findElementAt(offset)).flatMap(e => Option(PsiTreeUtil.findFirstParent(e, HaskellElementCondition.QVarConOpElementCondition))) match {
      case Some(e) =>
        if (e.getText.startsWith("`") && e.getText.endsWith("`")) {
          e.replace(HaskellElementFactory.createQVarConOp(project, s"`$suggestion`"))
        } else if (e.getText.startsWith("(") && e.getText.endsWith(")")) {
          e.replace(HaskellElementFactory.createQVarConOp(project, s"($suggestion)"))
        } else {
          e.replace(HaskellElementFactory.createQVarConOp(project, suggestion))
        }
      case None => ()
    }
  }
}

class OptimizeImportIntentionAction extends HaskellBaseIntentionAction {
  setText("Optimize imports")

  override def getFamilyName: String = "Optimize imports"

  override def invoke(project: Project, editor: Editor, file: PsiFile): Unit = {
    new HaskellImportOptimizer().processFile(file).run()
  }
}
