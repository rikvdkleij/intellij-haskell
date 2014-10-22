/*
 * Copyright 2014 Rik van der Kleij
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

package com.powertuple.intellij.haskell.annotator

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.codeInsight.daemon.impl.DaemonCodeAnalyzerImpl
import com.intellij.lang.annotation.{AnnotationHolder, ExternalAnnotator}
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import com.powertuple.intellij.haskell.HaskellFileType
import com.powertuple.intellij.haskell.external.{GhcMod, GhcModCheckResult, GhcModProblem}
import com.powertuple.intellij.haskell.util.{FileUtil, LineColumnPosition}

import scala.annotation.tailrec

class GhcModiExternalAnnotator extends ExternalAnnotator[GhcModInitialInfo, GhcModCheckResult] {

  /**
   * Returning null will cause doAnnotate() not to be called by Intellij API.
   */
  override def collectInformation(psiFile: PsiFile): GhcModInitialInfo = {
    (psiFile, Option(psiFile.getVirtualFile)) match {
      case (_, None) => null // can be case if file is in memory only (just created file)
      case (_, Some(f)) if f.getFileType != HaskellFileType.INSTANCE => null
      case (_, Some(f)) if f.getPath == null => null
      case (_, Some(f)) => FileUtil.saveFile(psiFile); GhcModInitialInfo(psiFile, f.getPath)
    }
  }

  override def doAnnotate(initialInfoGhcMod: GhcModInitialInfo): GhcModCheckResult = {
    // Temporary using `ghc-mod check` because of ghc-mod issue #275
    GhcMod.check(initialInfoGhcMod.psiFile.getProject, initialInfoGhcMod.filePath)
  }

  override def apply(psiFile: PsiFile, ghcModResult: GhcModCheckResult, holder: AnnotationHolder) {
    if (ghcModResult.problems.nonEmpty && psiFile.isValid) {
      for (annotation <- createAnnotations(ghcModResult, psiFile)) {
        annotation match {
          case ErrorAnnotation(textRange, message) => holder.createErrorAnnotation(textRange, message)
          case WarningAnnotation(textRange, message) => holder.createWarningAnnotation(textRange, message)
        }
      }
    }
    markFileDirty(psiFile)
  }

  private def markFileDirty(psiFile: PsiFile) {
    val fileStatusMap = DaemonCodeAnalyzer.getInstance(psiFile.getProject).asInstanceOf[DaemonCodeAnalyzerImpl].getFileStatusMap
    val document = FileDocumentManager.getInstance().getDocument(psiFile.getVirtualFile)
    fileStatusMap.markFileScopeDirty(document, new TextRange(0, document.getTextLength), document.getTextLength)
  }

  // TODO: Try to make problem text range more precise by using problem description
  private[annotator] def createAnnotations(ghcModResult: GhcModCheckResult, psiFile: PsiFile): Seq[Annotation] = {
    for (problem <- ghcModResult.problems.filter(_.filePath == psiFile.getOriginalFile.getVirtualFile.getPath)) yield {
      val textRange = getProblemTextRange(psiFile, problem)
      if (problem.description.startsWith("Warning:")) {
        WarningAnnotation(textRange, problem.description)
      } else {
        ErrorAnnotation(textRange, problem.description)
      }
    }
  }

  private def getProblemTextRange(psiFile: PsiFile, problem: GhcModProblem): TextRange = {
    val ghcModiOffSet = LineColumnPosition.getOffset(psiFile, LineColumnPosition(problem.lineNr, problem.columnNr)).getOrElse(0)
    if (Option(psiFile.findElementAt(ghcModiOffSet)).isEmpty) {
      findTextRangeLastElement(ghcModiOffSet, psiFile).getOrElse(TextRange.create(0, 0))
    } else {
      psiFile.findElementAt(ghcModiOffSet).getTextRange
    }
  }

  @tailrec
  private def findTextRangeLastElement(offSet: Int, psiFile: PsiFile): Option[TextRange] = {
    if (offSet > 0) {
      Option(psiFile.findElementAt(offSet)) match {
        case Some(e) => Some(e.getTextRange)
        case None => findTextRangeLastElement(offSet - 1, psiFile)
      }
    }
    else {
      None
    }
  }

}

case class GhcModInitialInfo(psiFile: PsiFile, filePath: String)

abstract class Annotation

case class ErrorAnnotation(textRange: TextRange, message: String) extends Annotation

case class WarningAnnotation(textRange: TextRange, message: String) extends Annotation
