package intellij.haskell.editor

import com.intellij.codeInsight.template.TemplateContextType
import com.intellij.codeInsight.template.impl.DefaultLiveTemplatesProvider
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import intellij.haskell.HaskellFileType
import intellij.haskell.psi.{HaskellExpression, HaskellFileHeader}

class HaskellTemplateContextType extends TemplateContextType("HASKELL_FILE", "Haskell") {
  override def isInContext(file: PsiFile, offset: Int): Boolean =
    file.getFileType == HaskellFileType.Instance
}

class HaskellPragmaTemplateContextType extends TemplateContextType("HASKELL_PRAGMA", "Pragma", classOf[HaskellTemplateContextType]) {
  override def isInContext(file: PsiFile, offset: Int): Boolean = {
    if (file.getFileType != HaskellFileType.Instance) return false
    if (offset < 5) return true
    val element = file.findElementAt(offset - 5)
    element != null &&
      PsiTreeUtil.getParentOfType(element, classOf[HaskellFileHeader]) != null
  }
}

class HaskellGlobalDefinitionTemplateContextType extends TemplateContextType("HASKELL_GLOB_DEF", "Global definition", classOf[HaskellTemplateContextType]) {
  override def isInContext(file: PsiFile, offset: Int): Boolean = {
    if (file.getFileType != HaskellFileType.Instance) return false
    var element = file.findElementAt(offset)
    if (element == null) element = file.findElementAt(offset - 1)
    element != null &&
      PsiTreeUtil.getParentOfType(element, classOf[HaskellExpression]) == null
  }
}

object DefaultHolder {
  val DEFAULT = Array("liveTemplates/Haskell")
}

class HaskellLiveTemplateProvider extends DefaultLiveTemplatesProvider {
  override def getDefaultLiveTemplateFiles: Array[String] = DefaultHolder.DEFAULT

  override def getHiddenLiveTemplateFiles: Array[String] = null
}

