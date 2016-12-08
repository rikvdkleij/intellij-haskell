package intellij.haskell.refactor

import java.util

import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.refactoring.rename.RenamePsiElementProcessor
import intellij.haskell.HaskellFile
import intellij.haskell.psi.HaskellModid
import intellij.haskell.util.HaskellProjectUtil
import org.apache.commons.lang.StringUtils

class HaskellRenameFileProcessor extends RenamePsiElementProcessor {
  override def canProcessElement(element: PsiElement): Boolean =
    HaskellProjectUtil.isHaskellStackProject(element.getProject) && element.isInstanceOf[HaskellFile]

  override def prepareRenaming(element: PsiElement, newName: String, allRenames: util.Map[PsiElement, String]): Unit = {
    val haskellModuledecl = PsiTreeUtil.findChildOfType(element, classOf[HaskellModid])
    if (haskellModuledecl != null) {
      val conids = haskellModuledecl.getText.split("\\.")
      conids(conids.length - 1) = createCorrectModuleName(newName)
      allRenames.put(haskellModuledecl, conids.mkString("."))
    }
    super.prepareRenaming(element, newName, allRenames)
  }

  def createCorrectModuleName(newName: String): String = {
    if (StringUtils.endsWith(newName, ".hs")) {
      StringUtils.removeEnd(newName, ".hs")
    } else {
      newName
    }
  }
}
