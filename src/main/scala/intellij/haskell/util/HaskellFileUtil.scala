/*
 * Copyright 2014-2019 Rik van der Kleij
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
import com.intellij.openapi.fileEditor.{FileDocumentManager, FileEditorManager}
import com.intellij.openapi.module.Module
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.{LocalFileSystem, VirtualFile, VirtualFileManager}
import com.intellij.psi.impl.PsiManagerEx
import com.intellij.psi.{PsiDocumentManager, PsiFile, PsiManager}
import intellij.haskell.HaskellFileType
import intellij.haskell.action.SelectionContext
import intellij.haskell.external.component.{NoInfo, NoInfoAvailable}

object HaskellFileUtil {

  def saveAllFiles(project: Project): Unit = {
    val openFiles = FileEditorManager.getInstance(project).getOpenFiles.filter(HaskellFileUtil.isHaskellFile)
    val documentManager = PsiDocumentManager.getInstance(project)
    openFiles.flatMap(f => findDocument(f)).foreach(documentManager.doPostponedOperationsAndUnblockDocument)
    documentManager.performWhenAllCommitted(
      () => {
        FileDocumentManager.getInstance.saveAllDocuments()
      }
    )
  }

  def saveFileInDispatchThread(psiFile: PsiFile): Unit = {
    findDocument(psiFile).foreach(d => {
      PsiDocumentManager.getInstance(psiFile.getProject).doPostponedOperationsAndUnblockDocument(d)
      ApplicationManager.getApplication.invokeAndWait(() => {
        FileDocumentManager.getInstance.saveDocument(d)
      })
    })
  }

  def saveFileInDispatchThread(project: Project, virtualFile: VirtualFile): Unit = {
    findDocument(virtualFile).foreach(d => {
      PsiDocumentManager.getInstance(project).doPostponedOperationsAndUnblockDocument(d)
      ApplicationManager.getApplication.invokeAndWait(() => {
        FileDocumentManager.getInstance.saveDocument(d)
      })
    })
  }

  def saveFile(psiFile: PsiFile): Unit = {
    findDocument(psiFile).foreach(d => {
      PsiDocumentManager.getInstance(psiFile.getProject).doPostponedOperationsAndUnblockDocument(d)
      FileDocumentManager.getInstance.saveDocument(d)
    })
  }

  def isDocumentUnsaved(document: Document): Boolean = {
    FileDocumentManager.getInstance().isDocumentUnsaved(document)
  }

  def findFileInRead(project: Project, filePath: String): (Option[VirtualFile], Either[NoInfo, PsiFile]) = {
    val virtualFile = Option(LocalFileSystem.getInstance().findFileByPath(HaskellFileUtil.makeFilePathAbsolute(filePath, project)))
    val psiFile = virtualFile.map(f => HaskellFileUtil.convertToHaskellFileInReadAction(project, f)) match {
      case Some(r) => r
      case None => Left(NoInfoAvailable(filePath, "-"))
    }
    (virtualFile, psiFile)
  }

  def findFile(project: Project, filePath: String): (Option[VirtualFile], Option[PsiFile]) = {
    val virtualFile = Option(LocalFileSystem.getInstance().findFileByPath(HaskellFileUtil.makeFilePathAbsolute(filePath, project)))
    val psiFile = virtualFile.map(f => HaskellFileUtil.convertToHaskellFileDispatchThread(project, f)) match {
      case Some(r) => r
      case None => None
    }
    (virtualFile, psiFile)
  }

  def findVirtualFile(project: Project, filePath: String): Option[VirtualFile] = {
    Option(LocalFileSystem.getInstance().findFileByPath(HaskellFileUtil.makeFilePathAbsolute(filePath, project)))
  }

  def findVirtualFile(psiFile: PsiFile): Option[VirtualFile] = {
    Option(psiFile.getOriginalFile.getVirtualFile)
  }

  def findDocument(virtualFile: VirtualFile): Option[Document] = {
    val fileDocumentManager = FileDocumentManager.getInstance()
    Option(fileDocumentManager.getCachedDocument(virtualFile))
  }

  def findDocument(psiFile: PsiFile): Option[Document] = {
    for {
      vf <- findVirtualFile(psiFile)
      fileDocumentManager = FileDocumentManager.getInstance()
      d <- Option(fileDocumentManager.getCachedDocument(vf))
    } yield d
  }

  def getAbsolutePath(psiFile: PsiFile): Option[String] = {
    Option(psiFile.getOriginalFile.getVirtualFile) match {
      case Some(vf) => Some(getAbsolutePath(vf))
      case None => None
    }
  }

  def getAbsolutePath(virtualFile: VirtualFile): String = {
    new File(virtualFile.getPath).getAbsolutePath
  }

  def makeFilePathAbsolute(filePath: String, project: Project): String = {
    makeFilePathAbsolute(filePath, project.getBasePath)
  }

  def makeFilePathAbsolute(filePath: String, module: Module): String = {
    makeFilePathAbsolute(filePath, HaskellProjectUtil.getModuleDir(module).getAbsolutePath)
  }

  def makeFilePathAbsolute(filePath: String, parentFilePath: String): String = {
    val path = new File(filePath)
    if (path.isAbsolute)
      path.getCanonicalPath
    else
      new File(parentFilePath, filePath).getCanonicalPath
  }

  def convertToHaskellFiles(project: Project, virtualFiles: Iterable[VirtualFile]): Iterable[PsiFile] = {
    if (project.isDisposed) {
      Iterable()
    } else {
      val psiManager = PsiManager.getInstance(project)
      virtualFiles.flatMap(vf => findCachedPsiFile(psiManager, vf) match {
        case Some(pf) => Some(pf)
        case _ => None
      })
    }
  }

  private def findCachedPsiFile(psiManager: PsiManager, virtualFile: VirtualFile): Option[PsiFile] = {
    val manager = psiManager.asInstanceOf[PsiManagerEx]
    val fileManager = manager.getFileManager
    ProgressManager.checkCanceled()
    Option(fileManager.getCachedPsiFile(virtualFile))
  }

  private def findPsiFile(psiManager: PsiManager, virtualFile: VirtualFile): Option[PsiFile] = {
    Option(psiManager.findFile(virtualFile))
  }

  def convertToHaskellFileDispatchThread(project: Project, virtualFile: VirtualFile): Option[PsiFile] = {
    val psiManager = PsiManager.getInstance(project)

    findCachedPsiFile(psiManager, virtualFile) match {
      case pf@Some(_) => pf
      case None => findPsiFile(psiManager, virtualFile)
    }
  }

  def convertToHaskellFileInReadAction(project: Project, virtualFile: VirtualFile): Either[NoInfo, PsiFile] = {
    val psiManager = PsiManager.getInstance(project)

    val actionMessage = s"Converting ${virtualFile.getName} to psi file"
    ApplicationUtil.runInReadActionWithWriteActionPriority(project, findCachedPsiFile(psiManager, virtualFile), readActionDescription = actionMessage) match {
      case Right(Some(pf)) => Right(pf)
      case _ => ApplicationUtil.runInReadActionWithWriteActionPriority(project, findPsiFile(psiManager, virtualFile), readActionDescription = actionMessage) match {
        case Right(Some(pf)) => Right(pf)
        case Right(None) => Left(NoInfoAvailable(virtualFile.getName, "-"))
        case Left(noInfo) => Left(noInfo)
      }
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

  def isHaskellFile(psiFile: PsiFile): Boolean = {
    isHaskellFileName(psiFile.getName)
  }

  def isHaskellFile(virtualFile: VirtualFile): Boolean = {
    isHaskellFileName(virtualFile.getName)
  }

  private final val HaskellFileSuffix = "." + HaskellFileType.HaskellFileExtension

  private def isHaskellFileName(name: String) = {
    name.endsWith(HaskellFileSuffix)
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
      if (!result && !directory.exists()) {
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
