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

package intellij.haskell.util.index

import java.io.{DataInput, DataOutput}
import java.util

import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.{FileTypeIndex, GlobalSearchScope, GlobalSearchScopesCore}
import com.intellij.util.indexing.FileBasedIndex.InputFilter
import com.intellij.util.indexing._
import com.intellij.util.io.{DataExternalizer, EnumeratorStringDescriptor, KeyDescriptor}
import intellij.haskell.util.HaskellFileUtil
import intellij.haskell.{HaskellFile, HaskellFileType}

import scala.collection.JavaConverters._

class HaskellFileIndex extends ScalaScalarIndexExtension[String] {

  override def getName: ID[String, Unit] = HaskellFileIndex.HaskellFileIndex

  override def getKeyDescriptor: KeyDescriptor[String] = HaskellFileIndex.Descriptor

  override def dependsOnFileContent(): Boolean = false

  override def getVersion: Int = HaskellFileIndex.IndexVersion

  override def getInputFilter: InputFilter = {
    HaskellFileIndex.HaskellModuleFilter
  }

  override def getIndexer: DataIndexer[String, Unit, FileContent] = haskellDataIndexer

  private val haskellDataIndexer = new HaskellDataIndexer

  private class HaskellDataIndexer extends DataIndexer[String, Unit, FileContent] {

    def map(inputData: FileContent): java.util.Map[String, Unit] = {
      val map = new util.HashMap[String, Unit]
      map.put(inputData.getFile.getNameWithoutExtension, ())
      map
    }
  }

}

object HaskellFileIndex {

  private final val HaskellFileIndex: ID[String, Unit] = ID.create("HaskellFileIndex")
  private final val IndexVersion = 1
  private final val Descriptor = new EnumeratorStringDescriptor
  private final val HaskellModuleFilter = new FileBasedIndex.InputFilter {

    def acceptInput(file: VirtualFile): Boolean = {
      file.getFileType == HaskellFileType.INSTANCE
    }
  }

  def findFilesByName(project: Project, name: String, searchScope: GlobalSearchScope): Iterable[VirtualFile] = {
    getFilesByName(project, name, searchScope)
  }

  def findFiles(project: Project, searchScope: GlobalSearchScope): Iterable[VirtualFile] = {
    getFilesForType(HaskellFileType.INSTANCE, project, searchScope)
  }

  def findProjectPsiFiles(project: Project): Iterable[HaskellFile] = {
    HaskellFileUtil.convertToHaskellFiles(findProjectFiles(project), project)
  }

  def findProjectProductionPsiFiles(project: Project): Iterable[HaskellFile] = {
    HaskellFileUtil.convertToHaskellFiles(findProjectProductionFiles(project), project)
  }

  def findProjectTestPsiFiles(project: Project): Iterable[HaskellFile] = {
    HaskellFileUtil.convertToHaskellFiles(findProjectTestFiles(project), project)
  }

  def findProjectFiles(project: Project): Iterable[VirtualFile] = {
    findFiles(project, GlobalSearchScope.projectScope(project))
  }

  private def findProjectProductionFiles(project: Project): Iterable[VirtualFile] = {
    findFiles(project, GlobalSearchScopesCore.projectProductionScope(project))
  }

  private def findProjectTestFiles(project: Project): Iterable[VirtualFile] = {
    findFiles(project, GlobalSearchScopesCore.projectTestScope(project))
  }

  private def getFilesForType(fileType: FileType, project: Project, searchScope: GlobalSearchScope): Iterable[VirtualFile] = {
    FileBasedIndex.getInstance.getContainingFiles(FileTypeIndex.NAME, fileType, searchScope).asScala
  }

  private def getFilesByName(project: Project, name: String, searchScope: GlobalSearchScope): Iterable[VirtualFile] = {
    FileBasedIndex.getInstance.getContainingFiles(HaskellFileIndex, name, searchScope).asScala
  }
}

/**
  * A specialization of FileBasedIndexExtension allowing to create a mapping [DataObject -> List of files containing this object]
  *
  */
object ScalaScalarIndexExtension {
  final val VoidDataExternalizer: DataExternalizer[Unit] = new ScalaScalarIndexExtension.UnitDataExternalizer

  private class UnitDataExternalizer extends DataExternalizer[Unit] {
    def save(out: DataOutput, value: Unit) {
    }

    def read(in: DataInput): Unit = {
      ()
    }
  }

}

abstract class ScalaScalarIndexExtension[K] extends FileBasedIndexExtension[K, Unit] {
  final def getValueExternalizer: DataExternalizer[Unit] = {
    ScalaScalarIndexExtension.VoidDataExternalizer
  }
}
