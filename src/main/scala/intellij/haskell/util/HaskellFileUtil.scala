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

import java.io.File
import java.util

import com.intellij.openapi.application.{ApplicationManager, ModalityState}
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.editor.{Document, SelectionModel}
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.{PsiFile, PsiManager}
import intellij.haskell.HaskellFile

object HaskellFileUtil {

  def saveAllFiles() {
    ApplicationManager.getApplication.invokeAndWait(new Runnable {
      override def run() {
        FileDocumentManager.getInstance.saveAllDocuments()
      }
    }, ModalityState.NON_MODAL)
  }

  def saveFile(virtualFile: VirtualFile) {
    ApplicationManager.getApplication.invokeAndWait(new Runnable {
      override def run() {
        findDocument(virtualFile).foreach(FileDocumentManager.getInstance.saveDocument)
      }
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

  def convertToHaskellFiles(virtualFiles: util.Collection[VirtualFile], project: Project): util.Collection[HaskellFile] = {
    import scala.collection.JavaConversions._
    val psiManager = PsiManager.getInstance(project)
    virtualFiles.flatMap(vf => convertToHaskellFile(vf, psiManager))
  }

  def convertToHaskellFile(virtualFile: VirtualFile, psiManager: PsiManager) = {
    Option(psiManager.findFile(virtualFile)) match {
      case Some(pf: HaskellFile) => Some(pf)
      case _ => None
    }
  }

  def saveFileWithNewContent(project: Project, virtualFile: VirtualFile, sourceCode: String) = {
    CommandProcessor.getInstance().executeCommand(project, new Runnable {
      override def run(): Unit = {
        ApplicationManager.getApplication.runWriteAction(new Runnable {
          override def run(): Unit = {
            val document = findDocument(virtualFile)
            document.foreach(_.setText(sourceCode))
          }
        })
      }
    }, null, null)
  }

  def saveFileWithPartlyNewContent(project: Project, virtualFile: VirtualFile, sourceCode: String, selectionModel: SelectionModel) = {
    CommandProcessor.getInstance().executeCommand(project, new Runnable {
      override def run(): Unit = {
        ApplicationManager.getApplication.runWriteAction(new Runnable {
          override def run(): Unit = {
            val document = findDocument(virtualFile)
            document.foreach(_.replaceString(selectionModel.getSelectionStart, selectionModel.getSelectionEnd, sourceCode))
          }
        })
      }
    }, null, null)
  }
}
