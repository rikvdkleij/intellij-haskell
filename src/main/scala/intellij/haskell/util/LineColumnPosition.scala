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

package intellij.haskell.util

import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.psi.PsiFile

case class LineColumnPosition(lineNr: Int, columnNr: Int) extends Ordered[LineColumnPosition] {

  def compare(that: LineColumnPosition): Int = {
    val lineNrCompare = this.lineNr compare that.lineNr
    if (lineNrCompare == 0) {
      this.columnNr compare that.columnNr
    } else {
      lineNrCompare
    }
  }
}

object LineColumnPosition {

  def fromOffset(psiFile: PsiFile, offset: Int): Option[LineColumnPosition] = {
    val fdm = FileDocumentManager.getInstance
    for {
      file <- Option(psiFile.getOriginalFile.getVirtualFile)
      doc <- Option(fdm.getDocument(file))
      li <- if (offset < doc.getTextLength) Some(doc.getLineNumber(offset)) else None
    } yield LineColumnPosition(li + 1, offset - doc.getLineStartOffset(li) + 1)
  }

  def getOffset(psiFile: PsiFile, lineColPos: LineColumnPosition): Option[Int] = {
    val fdm = FileDocumentManager.getInstance
    for {
      file <- Option(psiFile.getOriginalFile.getVirtualFile)
      doc <- Option(fdm.getDocument(file))
      lineIndex <- getLineIndex(lineColPos.lineNr, doc)
      startOffsetLine = doc.getLineStartOffset(lineIndex)
    } yield startOffsetLine + lineColPos.columnNr - 1
  }

  private def getLineIndex(lineNr: Int, doc: Document) = {
    if (lineNr > doc.getLineCount) {
      None
    } else {
      Some(lineNr - 1)
    }
  }
}
