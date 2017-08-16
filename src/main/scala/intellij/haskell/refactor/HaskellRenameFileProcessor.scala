package intellij.haskell.refactor

import java.util

import com.intellij.psi.PsiElement
import com.intellij.refactoring.rename.RenamePsiElementProcessor
import intellij.haskell.HaskellFile
import intellij.haskell.psi.HaskellPsiUtil
import intellij.haskell.psi.impl.HaskellPsiImplUtil
import intellij.haskell.util.HaskellProjectUtil

class HaskellRenameFileProcessor extends RenamePsiElementProcessor {

  override def canProcessElement(element: PsiElement): Boolean = {
    HaskellProjectUtil.isHaskellProject(element.getProject) && element.isInstanceOf[HaskellFile]
  }

  override def prepareRenaming(psiElement: PsiElement, fileName: String, allRenames: util.Map[PsiElement, String]): Unit = {
    if (psiElement.isValid) {
      HaskellPsiUtil.findModuleDeclaration(psiElement.getContainingFile).foreach(moduleDeclaration => {
        moduleDeclaration.getModuleName.foreach(moduleName => {
          val newModuleName = HaskellRenameFileProcessor.createNewModuleName(moduleName, fileName)
          allRenames.put(moduleDeclaration, newModuleName)
          super.prepareRenaming(psiElement, fileName, allRenames)
        })
      })
    }
  }

}

object HaskellRenameFileProcessor {

  def createNewModuleName(oldModuleName: String, fileName: String): String = {
    val conIds = oldModuleName.split("\\.")
    conIds(conIds.length - 1) = HaskellPsiImplUtil.removeFileExtension(fileName)
    conIds.mkString(".")
  }
}