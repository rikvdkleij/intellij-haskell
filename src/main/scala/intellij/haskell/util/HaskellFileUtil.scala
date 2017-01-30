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

import java.io.{File, FileOutputStream, InputStream}

import com.intellij.openapi.application.{ApplicationManager, ModalityState}
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.editor.{Document, SelectionModel}
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.{PsiFile, PsiManager}
import intellij.haskell.HaskellFile

object HaskellFileUtil {

  def saveAllFiles(): Unit = {
    ApplicationManager.getApplication.invokeAndWait(() => {
      FileDocumentManager.getInstance.saveAllDocuments()
    }, ModalityState.NON_MODAL)
  }

  def saveFile(virtualFile: VirtualFile): Unit = {
    ApplicationManager.getApplication.invokeAndWait(() => {
      findDocument(virtualFile).foreach(FileDocumentManager.getInstance.saveDocument)
    }, ModalityState.NON_MODAL)
  }

  def findVirtualFile(psiFile: PsiFile): VirtualFile = {
    psiFile.getOriginalFile.getVirtualFile
  }

  def findDocument(virtualFile: VirtualFile): Option[Document] = {
    val fileDocumentManager = FileDocumentManager.getInstance()
    Option(fileDocumentManager.getDocument(virtualFile))
  }

  def getFilePath(psiFile: PsiFile): String = {
    psiFile.getOriginalFile.getVirtualFile.getPath
  }

  def makeFilePathAbsolute(filePath: String, project: Project): String = {
    if (new File(filePath).isAbsolute)
      filePath
    else
      new File(project.getBasePath, filePath).getAbsolutePath
  }

  def convertToHaskellFiles(virtualFiles: Iterable[VirtualFile], project: Project): Iterable[HaskellFile] = {
    val psiManager = PsiManager.getInstance(project)
    virtualFiles.flatMap(vf => convertToHaskellFile(vf, psiManager))
  }

  def convertToHaskellFile(virtualFile: VirtualFile, psiManager: PsiManager): Option[HaskellFile] = {
    Option(psiManager.findFile(virtualFile)) match {
      case Some(pf: HaskellFile) => Some(pf)
      case _ => None
    }
  }

  def saveFileWithNewContent(project: Project, virtualFile: VirtualFile, sourceCode: String): Unit = {
    CommandProcessor.getInstance().executeCommand(project, () => {
      ApplicationManager.getApplication.runWriteAction(new Runnable {
        override def run(): Unit = {
          val document = findDocument(virtualFile)
          document.foreach(_.setText(sourceCode))
        }
      })
    }, null, null)
  }

  def saveFileWithPartlyNewContent(project: Project, virtualFile: VirtualFile, sourceCode: String, selectionModel: SelectionModel): Unit = {
    CommandProcessor.getInstance().executeCommand(project, () => {
      ApplicationManager.getApplication.runWriteAction(new Runnable {
        override def run(): Unit = {
          val document = findDocument(virtualFile)
          document.foreach(_.replaceString(selectionModel.getSelectionStart, selectionModel.getSelectionEnd, sourceCode))
        }
      })
    }, null, null)
  }

  def copyStreamToFile(stream: InputStream, file: File): File = {
    try {
      val outputStream = new FileOutputStream(file)
      try {
        FileUtil.copy(stream, outputStream)
      } finally {
        outputStream.close()
      }
    } finally {
      stream.close()
    }
    file
  }
}
