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

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.indexing._
import com.intellij.util.io.{EnumeratorStringDescriptor, KeyDescriptor}
import intellij.haskell.psi.HaskellPsiUtil
import intellij.haskell.util.HaskellFileUtil
import intellij.haskell.{HaskellFile, HaskellFileType}

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

  def findHaskellFileByModuleName(project: Project, moduleName: String, searchScope: GlobalSearchScope): Option[HaskellFile] = {
    val projectFile = if (searchScope.isSearchInLibraries) {
      findFilesByModuleName(moduleName, GlobalSearchScope.projectScope(project)).headOption
    } else {
      None
    }

    val virtualFile = projectFile.orElse(findFilesByModuleName(moduleName, searchScope).headOption)
    virtualFile.flatMap(vf => HaskellFileUtil.convertToHaskellFile(project, vf))
  }

  def findHaskellFilesByModuleNameInAllScope(project: Project, moduleName: String): Iterable[HaskellFile] = {
    HaskellFileUtil.convertToHaskellFiles(project, findFilesByModuleName(moduleName, GlobalSearchScope.allScope(project)))
  }

  private def findFilesByModuleName(moduleName: String, searchScope: GlobalSearchScope): Iterable[VirtualFile] = {
    FileBasedIndex.getInstance.getContainingFiles(HaskellModuleNameIndex, moduleName, searchScope).asScala
  }
}

class HaskellModuleNameIndex extends ScalaScalarIndexExtension[String] {

  private val haskellModuleNameIndexer = new HaskellModuleNameIndexer

  override def getIndexer: DataIndexer[String, Unit, FileContent] = haskellModuleNameIndexer

  override def getName: ID[String, Unit] = HaskellModuleNameIndex.HaskellModuleNameIndex

  override def getKeyDescriptor: KeyDescriptor[String] = HaskellModuleNameIndex.KeyDescriptor

  override def getInputFilter = HaskellModuleNameIndex.HaskellFileFilter

  override def dependsOnFileContent: Boolean = true

  override def getVersion: Int = HaskellModuleNameIndex.IndexVersion

  class HaskellModuleNameIndexer extends DataIndexer[String, Unit, FileContent] {

    override def map(inputData: FileContent): java.util.Map[String, Unit] = {
      val psiFile = inputData.getPsiFile
      HaskellPsiUtil.findModuleDeclaration(psiFile).map(_.getModid.getName) match {
        case Some(n) => Collections.singletonMap(n, ())
        case _ => Collections.emptyMap()
      }
    }
  }

}
