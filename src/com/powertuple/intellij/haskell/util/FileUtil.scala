/*
 * Copyright 2015 Rik van der Kleij
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

package com.powertuple.intellij.haskell.util

import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.libraries.LibraryUtil
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.{PsiDirectory, PsiFile}
import com.intellij.util.ui.UIUtil
import com.powertuple.intellij.haskell.HaskellNotificationGroup
import com.powertuple.intellij.haskell.external.GhcMod

object FileUtil {

  def saveFile(psiFile: PsiFile) {
    UIUtil.invokeLaterIfNeeded(new Runnable {
      override def run() {
        FileDocumentManager.getInstance.saveDocument(FileDocumentManager.getInstance().getDocument(psiFile.getVirtualFile))
      }
    })
  }

  def saveAllFiles() {
    UIUtil.invokeLaterIfNeeded(new Runnable {
      override def run() {
        FileDocumentManager.getInstance.saveAllDocuments()
      }
    })
  }

  def findModuleFilePath(module: String, project: Project): Option[String] = {
    val moduleFilePath = for {
      (name, path) <- getNameAndPathForModule(module)
      files = HaskellFileIndex.getFilesByName(project, name, GlobalSearchScope.allScope(project))
      file <- files.find(hf => checkPath(hf.getContainingDirectory, path))
      filePath <- Some(file.getVirtualFile.getPath)
    } yield filePath

    moduleFilePath match {
      case Some(p) => Some(p)
      case None =>
        if (GhcMod.listAvailableModules(project).exists(_ == module)) {
          HaskellEditorUtil.createLabelMessage(s"Could not find source code for `$module`. Please add source for package to 'Project Settings/Libraries'", project)
        } else {
          ()
        }
        None
    }
  }

  def getFilePath(psiFile: PsiFile): String = {
    psiFile.getOriginalFile.getVirtualFile.getPath
  }

  def isLibraryFile(psiFile: PsiFile): Boolean = {
    LibraryUtil.findLibraryEntry(psiFile.getVirtualFile, psiFile.getProject) != null
  }

  private def getNameAndPathForModule(module: String) = {
    module.split('.').toList.reverse match {
      case n :: d => Some(n, d)
      case _ => HaskellNotificationGroup.notifyError(s"Could not determine path for $module"); None
    }
  }

  private def checkPath(dir: PsiDirectory, dirNames: List[String]): Boolean = {
    dirNames match {
      case h :: t if dir.getName == h => checkPath(dir.getParentDirectory, t)
      case h :: t => false
      case _ => true
    }
  }
}

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
