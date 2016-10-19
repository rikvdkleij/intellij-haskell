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

  def getFilePath(psiFile: PsiFile): String = {
    psiFile.getOriginalFile.getVirtualFile.getPath
  }

  def makeFilePathAbsolute(filePath: String, project: Project): String = {
    if (new File(filePath).isAbsolute)
      filePath
    else
      new File(project.getBasePath, filePath).getAbsolutePath
  }

  def convertToHaskellFiles(virtualFiles: Stream[VirtualFile], project: Project): Stream[HaskellFile] = {
    val psiManager = PsiManager.getInstance(project)
    virtualFiles.flatMap(vf => convertToHaskellFile(vf, psiManager))
  }

  def convertToHaskellFile(virtualFile: VirtualFile, psiManager: PsiManager) = {
    Option(psiManager.findFile(virtualFile)) match {
      case Some(pf: HaskellFile) => Some(pf)
      case _ => None
    }
  }
}
