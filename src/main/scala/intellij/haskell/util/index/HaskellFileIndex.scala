package intellij.haskell.util.index

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.psi.search.{FileTypeIndex, GlobalSearchScope, GlobalSearchScopesCore}
import intellij.haskell.HaskellFileType
import intellij.haskell.util.HaskellFileUtil

import scala.jdk.CollectionConverters._

object HaskellFileIndex {

  def findProjectHaskellFiles(project: Project): Iterable[PsiFile] = {
    HaskellFileUtil.convertToHaskellFiles(project, findProjectFiles(project))
  }

  def findProjectProductionHaskellFiles(project: Project): Iterable[PsiFile] = {
    HaskellFileUtil.convertToHaskellFiles(project, findProjectProductionFiles(project))
  }

  private def findProjectFiles(project: Project): Iterable[VirtualFile] = {
    findFiles(project, GlobalSearchScope.projectScope(project))
  }

  private def findProjectProductionFiles(project: Project): Iterable[VirtualFile] = {
    findFiles(project, GlobalSearchScopesCore.projectProductionScope(project))
  }

  private def findFiles(project: Project, searchScope: GlobalSearchScope): Iterable[VirtualFile] = {
    FileTypeIndex.getFiles(HaskellFileType.Instance, searchScope).asScala
  }

}
