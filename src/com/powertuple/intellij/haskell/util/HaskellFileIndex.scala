package com.powertuple.intellij.haskell.util

import java.io.{DataInput, DataOutput}

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.CommonProcessors
import com.intellij.util.indexing.FileBasedIndex.InputFilter
import com.intellij.util.indexing._
import com.intellij.util.io.{DataExternalizer, EnumeratorStringDescriptor, KeyDescriptor}
import com.powertuple.intellij.haskell.{HaskellFile, HaskellFileType}
import gnu.trove.THashSet

import scala.collection.JavaConversions._

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
      Map(inputData.getFile.getNameWithoutExtension ->())
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

  def getAllNames(project: Project, searchScope: GlobalSearchScope): Seq[String] = {
    val allKeys: java.util.Set[String] = new THashSet[String]
    FileBasedIndex.getInstance.processAllKeys(HaskellFileIndex, new CommonProcessors.CollectProcessor[String](allKeys), searchScope, null)
    allKeys.toSeq
  }

  def getFilesByName(project: Project, name: String, searchScope: GlobalSearchScope): Seq[HaskellFile] = {
    getByName(project, name, searchScope)
  }

  private def getByName(project: Project, name: String, searchScope: GlobalSearchScope): Seq[HaskellFile] = {
    val psiManager = PsiManager.getInstance(project)
    val virtualFiles = getVirtualFilesByName(project, name, searchScope)

    virtualFiles.flatMap(convertToHaskellFile(_, psiManager))
  }

  private def convertToHaskellFile(virtualFile: VirtualFile, psiManager: PsiManager): Option[HaskellFile] = {
    val psiFile = psiManager.findFile(virtualFile)
    psiFile match {
      case f: HaskellFile => Some(f)
      case _ => None
    }
  }

  private def getVirtualFilesByName(project: Project, name: String, searchScope: GlobalSearchScope): Seq[VirtualFile] = {
    FileBasedIndex.getInstance.getContainingFiles(HaskellFileIndex, name, searchScope).toSeq
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