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

import java.io.{DataInput, DataOutput}
import java.util.Collections

import com.intellij.openapi.project.DumbService
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.indexing.FileBasedIndex.InputFilter
import com.intellij.util.indexing._
import com.intellij.util.io.{DataExternalizer, EnumeratorStringDescriptor, IOUtil, KeyDescriptor}
import intellij.haskell.HaskellFileType
import intellij.haskell.psi.HaskellPsiUtil
import intellij.haskell.util.ScalaUtil

import scala.collection.JavaConverters._

object HaskellFilePathIndex {

  private final val HaskellFilePathIndex: ID[String, Option[String]] = ID.create("HaskellFilePathIndex")
  private final val IndexVersion = 1
  private final val Descriptor = new EnumeratorStringDescriptor
  private final val HaskellFileFilter = new FileBasedIndex.InputFilter {

    def acceptInput(file: VirtualFile): Boolean = {
      file.getFileType == HaskellFileType.Instance
    }
  }

  def findModuleName(psiFile: PsiFile, searchScope: GlobalSearchScope): Option[String] = {
    val path = psiFile.getVirtualFile.getPath
    Option(DumbService.getInstance(psiFile.getProject).tryRunReadActionInSmartMode(ScalaUtil.computable(FileBasedIndex.getInstance.getValues(HaskellFilePathIndex, path, searchScope).asScala), null)) match {
      case None => HaskellPsiUtil.findModuleName(psiFile, runInRead = true)
      case Some(v) =>
        // Should be only one entry max
        v.headOption.flatten
    }
  }
}


class HaskellFilePathIndex extends FileBasedIndexExtension[String, Option[String]] {

  private val haskellDataIndexer = new HaskellFilePathIndexer

  override def getName: ID[String, Option[String]] = HaskellFilePathIndex.HaskellFilePathIndex

  override def getKeyDescriptor: KeyDescriptor[String] = HaskellFilePathIndex.Descriptor

  override def dependsOnFileContent(): Boolean = true

  override def getVersion: Int = HaskellFilePathIndex.IndexVersion

  override def getInputFilter: InputFilter = HaskellFilePathIndex.HaskellFileFilter

  override def getIndexer: DataIndexer[String, Option[String], FileContent] = haskellDataIndexer

  private class HaskellFilePathIndexer extends DataIndexer[String, Option[String], FileContent] {

    def map(fileContent: FileContent): java.util.Map[String, Option[String]] = {
      val psiFile = fileContent.getPsiFile
      val path = fileContent.getFile.getPath
      HaskellPsiUtil.findModuleDeclaration(psiFile).flatMap(_.getModuleName) match {
        case Some(mn) => Collections.singletonMap(path, Some(mn))
        case _ => Collections.singletonMap(path, None)
      }
    }
  }

  override def getValueExternalizer: DataExternalizer[Option[String]] = new DataExternalizer[Option[String]] {
    override def save(dataOutput: DataOutput, value: Option[String]): Unit = {
      IOUtil.writeUTF(dataOutput, value.getOrElse("none"))
    }

    override def read(dataInput: DataInput): Option[String] = {
      val s = IOUtil.readUTF(dataInput)
      if (s == "none") None else Some(s)
    }

  }
}
