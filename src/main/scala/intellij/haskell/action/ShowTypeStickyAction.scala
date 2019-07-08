package intellij.haskell.action

import com.intellij.openapi.actionSystem.{AnAction, AnActionEvent}
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import intellij.haskell.external.component.HaskellComponentsManager
import intellij.haskell.psi.{HaskellPsiUtil, HaskellQualifiedNameElement}
import intellij.haskell.util.{HaskellEditorUtil, StringUtil}

import scala.annotation.tailrec

class ShowTypeStickyAction extends AnAction {

  override def update(actionEvent: AnActionEvent): Unit = {
    HaskellEditorUtil.enableAction(onlyForSourceFile = true, actionEvent)
  }

  def actionPerformed(actionEvent: AnActionEvent): Unit = {
    ActionUtil.findActionContext(actionEvent).foreach(actionContext => {
      val editor = actionContext.editor
      val psiFile = actionContext.psiFile

      actionContext.selectionModel match {
        case Some(sm) => HaskellComponentsManager.findTypeInfoForSelection(psiFile, sm) match {
          case Right(info) => HaskellEditorUtil.showHint(editor, StringUtil.escapeString(info.typeSignature), sticky = true)
          case Left(info) => HaskellEditorUtil.showHint(editor, info.message)
          case _ => HaskellEditorUtil.showHint(editor, "Could not determine type for selection")
        }
        case _ =>
          for {
            psiElement <- HaskellPsiUtil.untilNonWhitespaceBackwards(Option(psiFile.findElementAt(editor.getCaretModel.getOffset)))
            namedElement <- HaskellPsiUtil.findNamedElement(psiElement).orElse {
              untilNameElementBackwards(Some(PsiTreeUtil.getDeepestLast(psiElement)))
            }
          } yield {
            ShowTypeAction.showTypeAsHint(actionContext.project, editor, namedElement, psiFile, sticky = true)
          }
      }
    })
  }

  @tailrec
  private def untilNameElementBackwards(element: Option[PsiElement]): Option[HaskellQualifiedNameElement] = {
    element match {
      case Some(e) =>
        HaskellPsiUtil.findQualifiedName(e) match {
          case None => untilNameElementBackwards(Option(e.getPrevSibling))
          case qualifiedName => qualifiedName
        }

      case None => None
    }
  }

}
