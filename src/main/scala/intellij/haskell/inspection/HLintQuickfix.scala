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
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.{PsiElement, PsiFile}
import intellij.haskell.annotator.HaskellAnnotator
import intellij.haskell.util.HaskellFileUtil

class HLintQuickfix(virtualFile: VirtualFile, startElement: PsiElement, endElement: PsiElement, startOffset: Int, endOffset: Int, replacement: String, hint: String, note: Seq[String]) extends LocalQuickFixOnPsiElement(startElement, endElement) {
  override def getText: String = {
    s"$hint, change to $replacement  ${noteText(note)}"
  }

  override def getFamilyName: String = "Inspection by HLint"

  override def invoke(project: Project, psiFile: PsiFile, startElement: PsiElement, endElement: PsiElement): Unit = {
    CommandProcessor.getInstance().executeCommand(project, () => {
      for {
        document <- HaskellFileUtil.findDocument(virtualFile)
      } yield {
        document.replaceString(startOffset, endOffset, replacement)
        HaskellFileUtil.saveFile(psiFile)
        HaskellAnnotator.restartDaemonCodeAnalyzerForFile(psiFile)
      }
    }, null, null)
  }


  private def noteText(note: Seq[String]) = {
    if (note.isEmpty) {
      ""
    } else {
      s" [Note: ${note.mkString("\n")}]"
    }
  }
}
