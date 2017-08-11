package intellij.haskell.action

import com.intellij.openapi.actionSystem.{AnAction, AnActionEvent}
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.{PsiElement, TokenType}
import intellij.haskell.external.component.HaskellComponentsManager
import intellij.haskell.psi.{HaskellPsiUtil, HaskellQualifiedNameElement, HaskellTypes}
import intellij.haskell.util.{HaskellEditorUtil, StringUtil}

import scala.annotation.tailrec

class ShowTypeStickyAction extends AnAction {

  override def update(actionEvent: AnActionEvent) {
    HaskellEditorUtil.enableAction(onlyForProjectFile = true, actionEvent)
  }

  def actionPerformed(actionEvent: AnActionEvent) {
    ActionUtil.findActionContext(actionEvent).foreach(actionContext => {
      val editor = actionContext.editor
      val psiFile = actionContext.psiFile

      actionContext.selectionModel match {
        case Some(sm) => HaskellComponentsManager.findTypeInfoForSelection(psiFile, sm) match {
          case Some(ti) => HaskellEditorUtil.showHint(editor, StringUtil.escapeString(ti.typeSignature), sticky = true)
          case None => HaskellEditorUtil.showHint(editor, "Could not determine type for selection")
        }
        case _ =>
          for {
            psiElement <- untilNonWhitespaceBackwards(Option(psiFile.findElementAt(editor.getCaretModel.getOffset)))
            namedElement <- HaskellPsiUtil.findNamedElement(psiElement).orElse {
              untilNameElementBackwards(Some(PsiTreeUtil.getDeepestLast(psiElement)))
            }
          } yield {
            ShowTypeAction.showTypeHint(actionContext.project, editor, namedElement, psiFile, sticky = true)
          }
      }
    })
  }

  @tailrec
  private def untilNonWhitespaceBackwards(element: Option[PsiElement]): Option[PsiElement] = {
    element match {
      case Some(e) if e.getNode.getElementType == HaskellTypes.HS_NEWLINE || e.getNode.getElementType == TokenType.WHITE_SPACE =>
        untilNonWhitespaceBackwards(Option(e.getPrevSibling))
      case e => e
    }
  }

  @tailrec
  private def untilNameElementBackwards(element: Option[PsiElement]): Option[HaskellQualifiedNameElement] = {
    element match {
      case Some(e) =>
        HaskellPsiUtil.findQualifiedNameParent(e) match {
          case None => untilNameElementBackwards(Option(e.getPrevSibling))
          case qualifiedName => qualifiedName
        }

      case None => None
    }
  }

}
