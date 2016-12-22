package intellij.haskell.util.index

import java.util
import java.util.Collections

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.{PsiFile, PsiManager}
import com.intellij.util.indexing._
import com.intellij.util.io.EnumeratorStringDescriptor
import intellij.haskell.psi.HaskellPsiUtil
import intellij.haskell.{HaskellFile, HaskellFileType}

object HaskellModuleIndex {
  private val HaskellModuleIndex: ID[String, Void] = ID.create("HaskellModuleIndex")
  private val IndexVersion = 0
  private val KeyDescriptor = new EnumeratorStringDescriptor

  val HaskellModuleFilter = new FileBasedIndex.InputFilter() {
    override def acceptInput(file: VirtualFile): Boolean = {
      //noinspection ObjectEquality
      file.getFileType == HaskellFileType.INSTANCE && file.isInLocalFileSystem
      // to avoid renaming modules that are somewhere in a lib folder
      // and added as a library. Can get nasty otherwise.
    }
  }

  def getFilesByModuleName(project: Project, moduleName: String, searchScope: GlobalSearchScope): List[HaskellFile] = {
    val psiManager = PsiManager.getInstance(project)
    val virtualFiles = getVirtualFilesByModuleName(moduleName, searchScope)
    virtualFiles.toArray.map(virtualFile => {
      val psiFile: PsiFile = psiManager.findFile(virtualFile.asInstanceOf[VirtualFile])
      psiFile match {
        case file: HaskellFile => file
        case _ => null
      }
    }).filter(_ != null).toList
  }

  def getVirtualFilesByModuleName(moduleName: String, searchScope: GlobalSearchScope): util.Collection[VirtualFile] = FileBasedIndex.getInstance.getContainingFiles(HaskellModuleIndex, moduleName, searchScope)
}

class HaskellModuleIndex extends ScalarIndexExtension[String] {

  class MyDataIndexer extends DataIndexer[String, Void, FileContent] {
    override def map(inputData: FileContent): util.Map[String, Void] = {
      val psiFile = inputData.getPsiFile
      HaskellPsiUtil.findModuleDeclaration(psiFile).flatMap(decl => Option(decl.getModid)).map(_.getText) match {
        case Some(n) => Collections.singletonMap(n, null)
        case _ => Collections.emptyMap[String, Void]
      }
    }
  }
  private val INDEXER = new MyDataIndexer

  override def getIndexer = INDEXER

  override def getName = HaskellModuleIndex.HaskellModuleIndex

  override def getKeyDescriptor = HaskellModuleIndex.KeyDescriptor

  override def getInputFilter = HaskellModuleIndex.HaskellModuleFilter

  override def dependsOnFileContent = true

  override def getVersion = HaskellModuleIndex.IndexVersion
}
