/*
 * Copyright 2014-2018 Rik van der Kleij
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
import java.nio.file.attribute.PosixFilePermission
import java.nio.file.{Files, Paths}

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.module.Module
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.{LocalFileSystem, VirtualFile, VirtualFileManager}
import com.intellij.psi.{PsiDocumentManager, PsiFile, PsiManager}
import intellij.haskell.HaskellFile
import intellij.haskell.action.SelectionContext

object HaskellFileUtil {

  def saveAllFiles(project: Project, psiFile: PsiFile): Unit = {
    val documentManager = PsiDocumentManager.getInstance(psiFile.getProject)
    findDocument(psiFile).foreach(documentManager.doPostponedOperationsAndUnblockDocument)
    documentManager.performWhenAllCommitted(
      () => {
        FileDocumentManager.getInstance.saveAllDocuments()
      }
    )
  }

  def saveFile(psiFile: PsiFile, checkCancelled: Boolean): Unit = {
    findDocument(psiFile).foreach(d => {
      PsiDocumentManager.getInstance(psiFile.getProject).doPostponedOperationsAndUnblockDocument(d)
      if (checkCancelled) {
        ProgressManager.checkCanceled()
      }
      FileDocumentManager.getInstance.saveDocument(d)
    })
  }

  def findVirtualFile(psiFile: PsiFile): Option[VirtualFile] = {
    Option(psiFile.getOriginalFile.getVirtualFile)
  }

  def findDocument(virtualFile: VirtualFile): Option[Document] = {
    val fileDocumentManager = FileDocumentManager.getInstance()
    Option(fileDocumentManager.getDocument(virtualFile))
  }

  def findDocument(psiFile: PsiFile): Option[Document] = {
    findVirtualFile(psiFile).flatMap(findDocument)
  }

  def getAbsolutePath(psiFile: PsiFile): Option[String] = {
    Option(psiFile.getOriginalFile.getVirtualFile) match {
      case Some(vf) => Some(getAbsolutePath(vf))
      case None => None
    }
  }

  def getAbsolutePath(virtualFile: VirtualFile): String = {
    Paths.get(virtualFile.getPath).toAbsolutePath.normalize().toString
  }

  def makeFilePathAbsolute(filePath: String, project: Project): String = {
    makeFilePathAbsolute(filePath, project.getBasePath)
  }

  def makeFilePathAbsolute(filePath: String, module: Module): String = {
    makeFilePathAbsolute(filePath, HaskellProjectUtil.getModuleDir(module).getAbsolutePath)
  }

  def makeFilePathAbsolute(filePath: String, parentFilePath: String): String = {
    val path = Paths.get(filePath)
    if (path.isAbsolute)
      path.normalize().toString
    else
      Paths.get(parentFilePath, filePath).normalize().toString
  }

  def convertToHaskellFiles(project: Project, virtualFiles: Iterable[VirtualFile]): Iterable[HaskellFile] = {
    if (project.isDisposed) {
      Iterable()
    } else {
      val psiManager = PsiManager.getInstance(project)
      virtualFiles.flatMap(vf => convertToHaskellFile(vf, psiManager))
    }
  }

  def convertToHaskellFile(virtualFile: VirtualFile, psiManager: PsiManager): Option[HaskellFile] = {
    Option(psiManager.findFile(virtualFile)) match {
      case Some(pf: HaskellFile) => Some(pf)
      case _ => None
    }
  }

  def convertToHaskellFile(project: Project, virtualFile: VirtualFile): Option[HaskellFile] = {
    val psiManager = PsiManager.getInstance(project)
    Option(psiManager.findFile(virtualFile)) match {
      case Some(pf: HaskellFile) => Some(pf)
      case _ => None
    }
  }

  def saveFileWithNewContent(psiFile: PsiFile, sourceCode: String): Unit = {
    CommandProcessor.getInstance().executeCommand(psiFile.getProject, () => {
      ApplicationManager.getApplication.runWriteAction(new Runnable {
        override def run(): Unit = {
          val document = findDocument(psiFile)
          document.foreach(_.setText(sourceCode))
        }
      })
    }, null, null)
  }

  def saveFileWithPartlyNewContent(psiFile: PsiFile, sourceCode: String, selectionContext: SelectionContext): Unit = {
    CommandProcessor.getInstance().executeCommand(psiFile.getProject, () => {
      ApplicationManager.getApplication.runWriteAction(new Runnable {
        override def run(): Unit = {
          val document = findDocument(psiFile)
          document.foreach(_.replaceString(selectionContext.start, selectionContext.end, sourceCode))
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

  def getFileNameWithoutExtension(psiFile: PsiFile): String = {
    val name = psiFile.getName
    removeFileExtension(name)
  }

  def removeFileExtension(fileName: String): String = {
    val index = fileName.lastIndexOf('.')
    if (index < 0) fileName else fileName.substring(0, index)
  }

  def findDirectory(dirPath: String, project: Project): Option[VirtualFile] = {
    Option(LocalFileSystem.getInstance().findFileByPath(HaskellFileUtil.makeFilePathAbsolute(dirPath, project)))
  }

  def getUrlByPath(absolutePath: String): String = {
    VirtualFileManager.constructUrl(LocalFileSystem.getInstance.getProtocol, absolutePath)
  }

  def createDirectoryIfNotExists(directory: File, onlyWriteableByOwner: Boolean): Unit = {
    if (!directory.exists()) {
      val result = FileUtil.createDirectory(directory)
      if (!result) {
        throw new RuntimeException(s"Could not create directory `${directory.getAbsolutePath}`")
      }
      if (onlyWriteableByOwner) {
        directory.setWritable(true, true)
        removeGroupWritePermission(directory)
      }
    }
  }

  // On Linux setting `directory.setWritable(true, true)` does not guarantee that group has NO write permissions
  def removeGroupWritePermission(path: File): Unit = {
    if (!SystemInfo.isWindows) {
      val directoryPath = Paths.get(path.getAbsolutePath)
      val permissions = Files.getPosixFilePermissions(directoryPath)
      if (permissions.contains(PosixFilePermission.GROUP_WRITE)) {
        permissions.remove(PosixFilePermission.GROUP_WRITE)
        Files.setPosixFilePermissions(directoryPath, permissions)
      }
    }
  }
}
