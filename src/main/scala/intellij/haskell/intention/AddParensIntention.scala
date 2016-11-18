package intellij.haskell.intention

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.{PsiElementFactory, PsiFileFactory, PsiElement}
import intellij.haskell.psi.HaskellDeclarationElement
import intellij.haskell.{HaskellLanguage, HaskellNotificationGroup}
import intellij.haskell.psi.HaskellTypes._
import scala.collection.JavaConversions._


class AddParensIntention extends PsiElementBaseIntentionAction {
  override def invoke(project: Project, editor: Editor, psiElement: PsiElement): Unit = {
    val parens = PsiTreeUtil.findChildrenOfType(PsiFileFactory.getInstance(project).createFileFromText("DUMMY.hs", HaskellLanguage.Instance, "add = (1 + 2)"), classOf[LeafPsiElement])
    //val leftParen = PsiElementFactory.SERVICE.getInstance(project).createDummyHolder("(", HS_LEFT_PAREN, null)
    //val rightParen = PsiElementFactory.SERVICE.getInstance(project).createDummyHolder(")", HS_RIGHT_PAREN, null)
    //PsiTreeUtil.findChildrenOfType(psiElement, classOf[HaskellDeclarationElement]).filter(_.getNode.getElementType == HS_LEFT_PAREN)
    val left = parens.filter(_.getNode.getElementType == HS_LEFT_PAREN).head
    val right = parens.filter(_.getNode.getElementType == HS_RIGHT_PAREN).head
    HaskellNotificationGroup.logWarning(s"============ $left")
    HaskellNotificationGroup.logWarning(s"============ $right")
    HaskellNotificationGroup.logWarning(s"============ psi element text ${psiElement.getText}")
    val psiFile = psiElement.getContainingFile
    val start = psiFile.findElementAt(editor.getSelectionModel.getSelectionStart)
    val end = psiFile.findElementAt(editor.getSelectionModel.getSelectionEnd - 1)
    HaskellNotificationGroup.logWarning(s"============ start element text ${psiElement.getText}")
    HaskellNotificationGroup.logWarning(s"============ end element text ${psiElement.getText}")
    start.getParent.addBefore(left, start)
    end.getParent.addAfter(right, start)
  }

  override def isAvailable(project: Project, editor: Editor, psiElement: PsiElement): Boolean = {
    val psiFile = psiElement.getContainingFile
    val start = psiFile.findElementAt(editor.getSelectionModel.getSelectionStart)
    val end = psiFile.findElementAt(editor.getSelectionModel.getSelectionEnd - 1)
    HaskellNotificationGroup.logWarning(s"=============== ${start.getNode.getElementType == HS_LEFT_PAREN} ${psiElement.isWritable} ${end.getNode.getElementType == HS_RIGHT_PAREN}")
    psiElement.isWritable && start.getNode.getElementType != HS_LEFT_PAREN && end.getNode.getElementType != HS_RIGHT_PAREN
  }

  override def getFamilyName: String = getText

  override def getText: String = "Add parens around expression"
}
