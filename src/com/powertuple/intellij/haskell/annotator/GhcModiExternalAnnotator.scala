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

import com.intellij.lang.annotation.{AnnotationHolder, ExternalAnnotator}
import com.intellij.notification.NotificationGroup
import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.PsiFile
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.application.{ModalityState, ApplicationManager}
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.execution.process.ProcessOutput
import com.intellij.openapi.util.text.StringUtil
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.codeInsight.daemon.impl.DaemonCodeAnalyzerImpl
import com.intellij.openapi.ui.MessageType
import scala.collection.JavaConversions._
import com.powertuple.intellij.haskell.HaskellFileType
import com.powertuple.intellij.haskell.external.GhciModManager

class GhcModiExternalAnnotator extends ExternalAnnotator[GhcModInitialInfo, GhcModResult] {
  private val Log = Logger.getInstance(classOf[GhcModiExternalAnnotator])

  private val GhcModNotificationGroup = NotificationGroup.balloonGroup("Ghc-mod inspections")

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

  override def doAnnotate(initialInfoGhcMod: GhcModInitialInfo): GhcModResult = {
    ApplicationManager.getApplication.invokeAndWait(new Runnable() {
      override def run() {
        FileDocumentManager.getInstance.saveDocument(FileDocumentManager.getInstance().getDocument(initialInfoGhcMod.psiFile.getVirtualFile))
      }
    }, ModalityState.any())

    val ghcModi = GhciModManager.getGhcMod(initialInfoGhcMod.psiFile.getProject)
    val ghcModOutput = ghcModi.execute("check " + initialInfoGhcMod.filePath)

    if (ghcModOutput.outputLines.isEmpty) {
      new GhcModResult(Seq())
    } else {
      new GhcModResult(ghcModOutput.outputLines.map(parseGhcModOutputLine))
    }
  }

  override def apply(psiFile: PsiFile, ghcModResult: GhcModResult, holder: AnnotationHolder) {
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

  private def startOffSetForProblem(lengthPerLine: Array[Int], problem: GhcModProblem): Int = {
    val lineNr = problem.lineNr
    (if (lineNr <= 1) {
      0
    } else {
      lengthPerLine.take(lineNr - 1).sum
    }) + problem.columnNr - 1
  }

  private[annotator] def createAnnotations(ghcModResult: GhcModResult, text: String): Seq[Annotation] = {
    val lengthPerLine = StringUtil.splitByLines(text, false).map(_.size + 1)
    for (problem <- ghcModResult.problems) yield {
      val startOffSet = startOffSetForProblem(lengthPerLine, problem)
      val textRange = TextRange.create(startOffSet, startOffSet + 1)
      if (problem.description.startsWith("Warning:")) {
        WarningAnnotation(textRange, problem.description)
      } else {
        ErrorAnnotation(textRange, problem.description)
      }
    }
  }

  private[annotator] def parseGhcModOutput(ghcModOutput: ProcessOutput): Seq[GhcModProblem] = {
    ghcModOutput match {
      case gmo if !gmo.getStderrLines.isEmpty => GhcModNotificationGroup.createNotification(s"Ghc-mod error output: ${gmo.getStderr}", MessageType.ERROR); Seq()
      case gmo => gmo.getStdoutLines.map(parseGhcModOutputLine)
    }
  }

  private def parseGhcModOutputLine(ghcModOutput: String): GhcModProblem = {
    val ghcModProblemPattern = """.+:([\d]+):([\d]+):(.+)""".r
    val ghcModProblemPattern(lineNr, columnNr, description) = ghcModOutput
    new GhcModProblem(lineNr.toInt, columnNr.toInt - 1, description)
  }
}

case class GhcModInitialInfo(psiFile: PsiFile, filePath: String)

case class GhcModResult(problems: Seq[GhcModProblem] = Seq())

case class GhcModProblem(lineNr: Int, columnNr: Int, description: String)

abstract class Annotation

case class ErrorAnnotation(textRange: TextRange, message: String) extends Annotation

case class WarningAnnotation(textRange: TextRange, message: String) extends Annotation
