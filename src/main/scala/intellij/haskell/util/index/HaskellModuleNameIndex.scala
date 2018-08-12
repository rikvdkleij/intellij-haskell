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

package intellij.haskell.util.index

import java.util.Collections

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.{IndexNotReadyException, Project}
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.indexing._
import com.intellij.util.io.{EnumeratorStringDescriptor, KeyDescriptor}
import intellij.haskell.HaskellFileType
import intellij.haskell.external.component.{IndexNotReady, NoInfo}
import intellij.haskell.psi.HaskellPsiUtil
import intellij.haskell.util.{ApplicationUtil, HaskellFileUtil}

import scala.collection.JavaConverters._

/**
  * Notice that Haskell modules in libraries can be found which are not exposed
  */
object HaskellModuleNameIndex {
  private val HaskellModuleNameIndex: ID[String, Unit] = ID.create("HaskellModuleNameIndex")
  private val IndexVersion = 1
  private val KeyDescriptor = new EnumeratorStringDescriptor

  private val HaskellFileFilter = new FileBasedIndex.InputFilter() {

    override def acceptInput(file: VirtualFile): Boolean = {
      file.getFileType == HaskellFileType.Instance
    }
  }

  def findHaskellFileByModuleName(project: Project, moduleName: String, searchScope: GlobalSearchScope): Either[NoInfo, Option[PsiFile]] = {
    findFilesByModuleName(project, moduleName, searchScope) match {
      case Right(vfs) => vfs.headOption match {
        case Some(vf) => HaskellFileUtil.convertToHaskellFileInReadAction(project, vf)
        case None => Right(None)
      }
      case Left(noInfo) => Left(noInfo)
    }
  }

  def findHaskellFilesByModuleNameInAllScope(project: Project, moduleName: String): Iterable[PsiFile] = {
    HaskellFileUtil.convertToHaskellFiles(project, findFilesByModuleName(project, moduleName, GlobalSearchScope.allScope(project)).getOrElse(Iterable()))
  }

  private def findFilesByModuleName(project: Project, moduleName: String, searchScope: GlobalSearchScope): Either[NoInfo, Iterable[VirtualFile]] = {
    if (ApplicationManager.getApplication.isDispatchThread) {
      Right(FileBasedIndex.getInstance.getContainingFiles(HaskellModuleNameIndex, moduleName, searchScope).asScala)
    } else {
      val result = {
        ApplicationUtil.scheduleInReadActionWithWriteActionPriority(
          project, {
            try {
              Right(FileBasedIndex.getInstance.getContainingFiles(HaskellModuleNameIndex, moduleName, searchScope).asScala)
            } catch {
              case _: IndexNotReadyException => Left(IndexNotReady)
            }
          },
          s"finding file for module $moduleName by index"
        )
      }
      for {
        r <- result
        vfs <- r
      } yield vfs
    }
  }
}

class HaskellModuleNameIndex extends ScalaScalarIndexExtension[String] {

  private val haskellModuleNameIndexer = new HaskellModuleNameIndexer

  override def getIndexer: DataIndexer[String, Unit, FileContent] = haskellModuleNameIndexer

  override def getName: ID[String, Unit] = HaskellModuleNameIndex.HaskellModuleNameIndex

  override def getKeyDescriptor: KeyDescriptor[String] = HaskellModuleNameIndex.KeyDescriptor

  override def getInputFilter: FileBasedIndex.InputFilter = HaskellModuleNameIndex.HaskellFileFilter

  override def dependsOnFileContent: Boolean = true

  override def getVersion: Int = HaskellModuleNameIndex.IndexVersion

  class HaskellModuleNameIndexer extends DataIndexer[String, Unit, FileContent] {

    override def map(inputData: FileContent): java.util.Map[String, Unit] = {
      val psiFile = inputData.getPsiFile
      HaskellPsiUtil.findModuleNameInPsiTree(psiFile) match {
        case Some(n) => Collections.singletonMap(n, ())
        case _ => Collections.emptyMap()
      }
    }
  }

}
