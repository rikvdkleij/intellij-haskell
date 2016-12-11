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

import com.intellij.openapi.application.{ApplicationManager, ModalityState}
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.editor.{Document, SelectionModel}
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.{PsiFile, PsiManager}
import intellij.haskell.HaskellFile

import scala.annotation.tailrec

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

  /**
    * Returns an array of directory names as the relative path to `file` from the source root.
    * For example, given file "project/src/foo/bar/baz.hs" the result would be `{"foo", "bar"}`.
    */
  def getPathFromSourceRoot(project: Project, file: VirtualFile): Option[List[String]] = {
    @tailrec
    def loop(file: VirtualFile, rootPath: String, initial: List[String] = List()): List[String] = {
      if (rootPath == file.getCanonicalPath) initial
      else loop(file.getParent, rootPath, file.getName :: initial)
    }

    for {
      root <- getSourceRoot(project, file)
      rootPath <- Option(root.getCanonicalPath)
    } yield loop(file, rootPath, List())
  }

  def getSourceRoot(project: Project, file: VirtualFile): Option[VirtualFile] = {
    @tailrec
    def loop(maybeFile: Option[VirtualFile], rootPath: String): Option[VirtualFile] = {
      maybeFile match {
        case None => None
        case Some(f) => if (rootPath == f.getCanonicalPath) Some(f) else loop(Option(f.getParent), rootPath)
      }
    }

    val result = Option(file).flatMap { file =>
      ProjectRootManager.getInstance(project).getContentSourceRoots.view.flatMap { root =>
        for {
          root <- Option(root)
          rootPath <- Option(root.getCanonicalPath)
        } yield loop(Some(file), rootPath)
      }.headOption
    }
    result.flatten
  }
}
