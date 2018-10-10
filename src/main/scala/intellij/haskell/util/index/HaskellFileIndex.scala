package intellij.haskell.util.index

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.psi.search.{FileTypeIndex, GlobalSearchScope, GlobalSearchScopesCore}
import intellij.haskell.HaskellFileType
import intellij.haskell.util.{HaskellFileUtil, HaskellProjectUtil}

import scala.collection.JavaConverters._

object HaskellFileIndex {

  def findProjectProductionHaskellFiles(project: Project): Iterable[PsiFile] = {
    HaskellFileUtil.convertToHaskellFiles(project, findProjectProductionFiles(project))
  }

  def findProjectProductionFiles(project: Project): Iterable[VirtualFile] = {
    findFiles(project, GlobalSearchScopesCore.projectProductionScope(project)).filter(vf => HaskellProjectUtil.isSourceFile(project, vf))
  }

  private def findFiles(project: Project, searchScope: GlobalSearchScope): Iterable[VirtualFile] = {
    FileTypeIndex.getFiles(HaskellFileType.Instance, searchScope).asScala
  }

}
