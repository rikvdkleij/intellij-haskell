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

package intellij.haskell.inspection

import com.intellij.codeInspection.LocalQuickFixOnPsiElement
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.{PsiElement, PsiFile}
import intellij.haskell.annotator.HaskellAnnotator
import intellij.haskell.inspection.HLintQuickfix.{invokeReplace, noteText}
import intellij.haskell.util.HaskellFileUtil

abstract class HLintQuickfix(startElement: PsiElement, endElement: PsiElement) extends LocalQuickFixOnPsiElement(startElement, endElement) {

  override def getFamilyName: String = "Inspection by HLint"
}

class HLintReplaceQuickfix(document: Document, virtualFile: VirtualFile, startElement: PsiElement, endElement: PsiElement, startOffset: Int, endOffset: Int, replacement: String, hint: String, note: Seq[String], deletes: Seq[HLintDeleteQuickfix]) extends HLintQuickfix(startElement, endElement) {

  override def getText: String =
    s"$hint, change to `$replacement` | ${noteText(note)}"

  override def invoke(project: Project, psiFile: PsiFile, startElement: PsiElement, endElement: PsiElement): Unit = {
    invokeReplace(project, document, virtualFile, psiFile, startOffset, endOffset, replacement)
    deletes.foreach(_.invoke(project, psiFile, startElement, endElement))
  }
}

class HLintDeleteQuickfix(document: Document, virtualFile: VirtualFile, startElement: PsiElement, endElement: PsiElement, startOffset: Int, endOffset: Int, hint: String, originalText: String) extends HLintQuickfix(startElement, endElement) {

  override def getText: String = s"$hint, delete `$originalText`"

  override def invoke(project: Project, psiFile: PsiFile, startElement: PsiElement, endElement: PsiElement): Unit =
    CommandProcessor.getInstance().executeCommand(project, () => {
      document.deleteString(startOffset, endOffset)
      HaskellFileUtil.saveFile(psiFile)
      HaskellAnnotator.restartDaemonCodeAnalyzerForFile(psiFile)
    }, null, null)
}

class HLintModifyCommentQuickfix(document: Document, virtualFile: VirtualFile, startElement: PsiElement, endElement: PsiElement, startOffset: Int, endOffset: Int, newComment: String, hint: String, note: Seq[String]) extends HLintQuickfix(startElement, endElement) {

  override def getText: String =
    s"$hint, change to `$newComment` | ${noteText(note)} "

  override def invoke(project: Project, psiFile: PsiFile, startElement: PsiElement, endElement: PsiElement): Unit =
    invokeReplace(project, document, virtualFile, psiFile, startOffset, endOffset, newComment)
}

class HLintInsertCommentQuickfix(document: Document, virtualFile: VirtualFile, startElement: PsiElement, endElement: PsiElement, startOffset: Int, endOffset: Int, insertComment: String, hint: String, note: Seq[String]) extends HLintQuickfix(startElement, endElement) {

  override def getText: String =
    s"$hint, insert `$insertComment` | ${noteText(note)} "

  override def invoke(project: Project, psiFile: PsiFile, startElement: PsiElement, endElement: PsiElement): Unit =
    CommandProcessor.getInstance().executeCommand(project, () => {
      document.insertString(startOffset, insertComment)
      HaskellFileUtil.saveFile(psiFile)
      HaskellAnnotator.restartDaemonCodeAnalyzerForFile(psiFile)
    }, null, null)
}

object HLintQuickfix {

  private[inspection] def noteText(note: Seq[String]): String = {
    if (note.isEmpty) {
      ""
    } else {
      s" [Note: ${note.mkString("\n")}]"
    }
  }

  private[inspection] def invokeReplace(project: Project, document: Document, virtualFile: VirtualFile, psiFile: PsiFile, startOffset: Int, endOffset: Int, replacement: String): Unit = {
    CommandProcessor.getInstance().executeCommand(project, () => {
      document.replaceString(startOffset, endOffset, replacement
        .replace("\\n", "\n")
      )
      HaskellFileUtil.saveFile(psiFile)
      HaskellAnnotator.restartDaemonCodeAnalyzerForFile(psiFile)
    }, null, null)
  }
}