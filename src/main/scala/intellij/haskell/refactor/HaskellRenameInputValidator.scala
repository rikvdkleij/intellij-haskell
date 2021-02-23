package intellij.haskell.refactor

import com.intellij.patterns.{ElementPattern, ElementPatternCondition}
import com.intellij.psi.PsiElement
import com.intellij.refactoring.rename.RenameInputValidator
import com.intellij.util.ProcessingContext
import intellij.haskell.psi._
import intellij.haskell.util.HaskellFileUtil
import intellij.haskell.{HaskellFile, HaskellFileType}

class HaskellRenameInputValidator extends RenameInputValidator {

  override def getPattern: ElementPattern[PsiElement] = new ElementPattern[PsiElement]() {
    override def accepts(o: Any): Boolean = true

    override def accepts(o: Any, context: ProcessingContext): Boolean = {
      o.isInstanceOf[HaskellVarid] || o.isInstanceOf[HaskellVarsym] || o.isInstanceOf[HaskellConid] || o.isInstanceOf[HaskellConsym] || o.isInstanceOf[HaskellFile]
    }

    override def getCondition: ElementPatternCondition[PsiElement] = null
  }

  override def isInputValid(newName: String, psiElement: PsiElement, context: ProcessingContext): Boolean = {
    if (newName.contains(' ')) {
      false
    } else {
      val project = psiElement.getProject
      psiElement match {
        case _: HaskellModid => HaskellElementFactory.createModid(project, newName).isDefined
        case _: HaskellVarid => HaskellElementFactory.createVarid(project, newName).isDefined
        case _: HaskellVarsym => HaskellElementFactory.createVarsym(project, newName).isDefined
        case _: HaskellConid => HaskellPsiUtil.findModIdElement(psiElement).isDefined || HaskellElementFactory.createConid(project, newName).isDefined
        case _: HaskellConsym => HaskellElementFactory.createConsym(project, newName).isDefined
        case _: HaskellFile => HaskellElementFactory.createConid(project, HaskellFileUtil.removeFileExtension(newName)).isDefined && newName.endsWith("." + HaskellFileType.INSTANCE.getDefaultExtension)
        case _ => false
      }
    }
  }

}