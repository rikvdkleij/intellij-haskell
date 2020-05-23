package intellij.haskell.alex

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.FileViewProvider

/**
  * @author ice1000
  */
class AlexFile(viewProvider: FileViewProvider) extends PsiFileBase(viewProvider, AlexLanguage.Instance) {

  def getFileType: FileType = {
    new AlexFileType(AlexLanguage.Instance)
  }

  override def toString: String = {
    " Alex file"
  }
}
