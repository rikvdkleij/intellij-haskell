/*
 * Copyright 2014 Rik van der Kleij

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
import com.intellij.openapi.application.{ApplicationManager, ModalityState}
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import com.powertuple.intellij.haskell.HaskellFileType
import com.powertuple.intellij.haskell.external.GhciModManager

class GhcModiExternalAnnotator extends ExternalAnnotator[GhcModInitialInfo, GhcModiResult] {

  /**
   * Returning null will cause doAnnotate() not to be called by Intellij API.
   */
  override def collectInformation(psiFile: PsiFile): GhcModInitialInfo = {
    val vFile = psiFile.getVirtualFile
    vFile match {
      case null => null // can be case if file is in memory only (just created file)
      case f if f.getFileType != HaskellFileType.INSTANCE => null
      case f if f.getCanonicalPath == null => null
      case f => GhcModInitialInfo(psiFile, f.getCanonicalPath)
    }
  }

  override def doAnnotate(initialInfoGhcMod: GhcModInitialInfo): GhcModiResult = {
    ApplicationManager.getApplication.invokeAndWait(new Runnable() {
      override def run() {
        FileDocumentManager.getInstance.saveDocument(FileDocumentManager.getInstance().getDocument(initialInfoGhcMod.psiFile.getVirtualFile))
      }
    }, ModalityState.any())

    val ghcModi = GhciModManager.getGhcMod(initialInfoGhcMod.psiFile.getProject)
    val ghcModiOutput = ghcModi.execute("check " + initialInfoGhcMod.filePath)

    if (ghcModiOutput.outputLines.isEmpty) {
      new GhcModiResult(Seq())
    } else {
      new GhcModiResult(ghcModiOutput.outputLines.map(parseGhcModiOutputLine))
    }
  }

  override def apply(psiFile: PsiFile, ghcModResult: GhcModiResult, holder: AnnotationHolder) {
    if (ghcModResult.problems.nonEmpty && psiFile.isValid) {
      for (annotation <- createAnnotations(ghcModResult, psiFile.getText)) {
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
    fileStatusMap.markFileScopeDirty(document, new TextRange(0, document.getTextLength), psiFile.getTextLength)
  }

  private def startOffSetForProblem(lengthPerLine: Iterator[Int], problem: GhcModiProblem): Int = {
    lengthPerLine.take(problem.lineNr - 1).map(_ + 1).sum + problem.columnNr
  }

  private[annotator] def createAnnotations(ghcModResult: GhcModiResult, text: String): Seq[Annotation] = {
    val lengthPerLine = text.lines.map(_.size)
    for (problem <- ghcModResult.problems) yield {
      val startOffSet = startOffSetForProblem(lengthPerLine, problem)
      val textRange = TextRange.create(startOffSet - 1, startOffSet)
      if (problem.description.startsWith("Warning:")) {
        WarningAnnotation(textRange, problem.description)
      } else {
        ErrorAnnotation(textRange, problem.description)
      }
    }
  }

  private[annotator] def parseGhcModiOutputLine(ghcModOutput: String): GhcModiProblem = {
    val ghcModProblemPattern = """.+:([\d]+):([\d]+):(.+)""".r
    val ghcModProblemPattern(lineNr, columnNr, description) = ghcModOutput
    new GhcModiProblem(lineNr.toInt, columnNr.toInt, description)
  }
}

case class GhcModInitialInfo(psiFile: PsiFile, filePath: String)

case class GhcModiResult(problems: Seq[GhcModiProblem] = Seq())

case class GhcModiProblem(lineNr: Int, columnNr: Int, description: String)

abstract class Annotation

case class ErrorAnnotation(textRange: TextRange, message: String) extends Annotation

case class WarningAnnotation(textRange: TextRange, message: String) extends Annotation
