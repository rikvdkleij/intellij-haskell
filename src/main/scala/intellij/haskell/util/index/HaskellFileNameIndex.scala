/*
 * Copyright 2014-2017 Rik van der Kleij
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

import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.project.{DumbService, Project}
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.{FileTypeIndex, GlobalSearchScope, GlobalSearchScopesCore}
import com.intellij.util.indexing.FileBasedIndex.InputFilter
import com.intellij.util.indexing._
import com.intellij.util.io.{EnumeratorStringDescriptor, KeyDescriptor}
import intellij.haskell.util.{HaskellFileUtil, ScalaUtil}
import intellij.haskell.{HaskellFile, HaskellFileType}

import scala.collection.JavaConverters._

object HaskellFileNameIndex {

  private final val HaskellFileNameIndex: ID[String, Unit] = ID.create("HaskellFileNameIndex")
  private final val IndexVersion = 1
  private final val Descriptor = new EnumeratorStringDescriptor
  private final val HaskellFileFilter = new FileBasedIndex.InputFilter {

    def acceptInput(file: VirtualFile): Boolean = {
      file.getFileType == HaskellFileType.Instance
    }
  }

  def findFilesByName(project: Project, name: String, searchScope: GlobalSearchScope): Iterable[VirtualFile] = {
    getFilesByName(project, name, searchScope)
  }

  def findProjectProductionHaskellFiles(project: Project): Iterable[HaskellFile] = {
    HaskellFileUtil.convertToHaskellFiles(project, findProjectProductionFiles(project))
  }

  def findProjectTestHaskellFiles(project: Project): Iterable[HaskellFile] = {
    HaskellFileUtil.convertToHaskellFiles(project, findProjectTestFiles(project))
  }

  private def findFiles(project: Project, searchScope: GlobalSearchScope): Iterable[VirtualFile] = {
    getFilesForType(HaskellFileType.Instance, project, searchScope)
  }

  def findProjectProductionFiles(project: Project): Iterable[VirtualFile] = {
    findFiles(project, GlobalSearchScopesCore.projectProductionScope(project))
  }

  private def findProjectTestFiles(project: Project): Iterable[VirtualFile] = {
    findFiles(project, GlobalSearchScopesCore.projectTestScope(project))
  }

  private def getFilesForType(fileType: FileType, project: Project, searchScope: GlobalSearchScope): Iterable[VirtualFile] = {
    FileTypeIndex.getFiles(fileType, searchScope).asScala
  }

  // Waits if index is not ready
  private def getFilesByName(project: Project, name: String, searchScope: GlobalSearchScope): Iterable[VirtualFile] = {
    DumbService.getInstance(project).runReadActionInSmartMode(ScalaUtil.computable(FileBasedIndex.getInstance.getContainingFiles(HaskellFileNameIndex, name, searchScope).asScala))
  }
}


class HaskellFileNameIndex extends ScalaScalarIndexExtension[String] {

  private val haskellDataIndexer = new HaskellFileNameIndexer

  override def getName: ID[String, Unit] = HaskellFileNameIndex.HaskellFileNameIndex

  override def getKeyDescriptor: KeyDescriptor[String] = HaskellFileNameIndex.Descriptor

  override def dependsOnFileContent(): Boolean = false

  override def getVersion: Int = HaskellFileNameIndex.IndexVersion

  override def getInputFilter: InputFilter = HaskellFileNameIndex.HaskellFileFilter

  override def getIndexer: DataIndexer[String, Unit, FileContent] = haskellDataIndexer

  private class HaskellFileNameIndexer extends DataIndexer[String, Unit, FileContent] {

    def map(inputData: FileContent): java.util.Map[String, Unit] = {
      Collections.singletonMap(inputData.getFile.getNameWithoutExtension, ())
    }
  }

}
