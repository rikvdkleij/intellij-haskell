package intellij.haskell.util.index

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.{FileTypeIndex, GlobalSearchScope, GlobalSearchScopesCore}
import intellij.haskell.util.HaskellFileUtil
import intellij.haskell.{HaskellFile, HaskellFileType}

import scala.collection.JavaConverters._

object HaskellFileIndex {

  def findProjectProductionHaskellFiles(project: Project): Iterable[HaskellFile] = {
    HaskellFileUtil.convertToHaskellFiles(project, findProjectProductionFiles(project))
  }

  def findProjectProductionFiles(project: Project): Iterable[VirtualFile] = {
    findFiles(project, GlobalSearchScopesCore.projectProductionScope(project))
  }

  private def findFiles(project: Project, searchScope: GlobalSearchScope): Iterable[VirtualFile] = {
    FileTypeIndex.getFiles(HaskellFileType.Instance, searchScope).asScala
  }

}
