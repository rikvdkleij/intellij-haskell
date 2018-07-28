/*
 * Copyright 2014-2018 Rik van der Kleij
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package intellij.haskell.action

import com.intellij.openapi.actionSystem.{AnAction, AnActionEvent}
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.{PsiElement, PsiFile}
import intellij.haskell.HaskellNotificationGroup
import intellij.haskell.editor.HaskellCompletionContributor
import intellij.haskell.external.component.{HaskellComponentsManager, StackProjectManager}
import intellij.haskell.psi._
import intellij.haskell.util.{HaskellEditorUtil, StringUtil}

class ShowTypeAction extends AnAction {

  override def update(actionEvent: AnActionEvent) {
    HaskellEditorUtil.enableAction(onlyForProjectFile = true, actionEvent)
  }

  def actionPerformed(actionEvent: AnActionEvent) {
    if (!StackProjectManager.isBuilding(actionEvent.getProject)) {
      ActionUtil.findActionContext(actionEvent).foreach(actionContext => {
        val editor = actionContext.editor
        val psiFile = actionContext.psiFile

        actionContext.selectionModel match {
          case Some(sm) => HaskellComponentsManager.findTypeInfoForSelection(psiFile, sm) match {
            case Right(info) => HaskellEditorUtil.showHint(editor, StringUtil.escapeString(info.typeSignature))
            case _ => HaskellEditorUtil.showHint(editor, "Could not determine type for selection")
          }
          case _ => ()
            Option(psiFile.findElementAt(editor.getCaretModel.getOffset)).foreach { psiElement =>
              ShowTypeAction.showTypeAsHint(actionContext.project, editor, psiElement, psiFile)
            }
        }
      })
    }
    else {
      HaskellEditorUtil.showHaskellSupportIsNotAvailableWhileBuilding(actionEvent.getProject)
    }
  }
}

object ShowTypeAction {

  def showTypeAsHint(project: Project, editor: Editor, psiElement: PsiElement, psiFile: PsiFile, sticky: Boolean = false): Unit = {
    showTypeInfo(project, editor, psiElement, psiFile, sticky = sticky)
  }

  private def showTypeInfo(project: Project, editor: Editor, psiElement: PsiElement, psiFile: PsiFile, sticky: Boolean = false): Unit = {
    showTypeSignatureAsHint(project, editor, sticky, getTypeInfo(psiFile, psiElement))
  }

  private def getTypeInfo(psiFile: PsiFile, psiElement: PsiElement): String = {
    HaskellComponentsManager.findTypeInfoForElement(psiElement) match {
      case Right(info) =>
        val typeSignatureFromScope =
          if (info.withFailure) {
            findTypeSignatureFromScope(psiFile, psiElement)
          } else {
            None
          }
        typeSignatureFromScope.getOrElse(info.typeSignature)

      case Left(noInfo) =>
        findTypeSignatureFromScope(psiFile, psiElement) match {
          case Some(typeSignature) => typeSignature
          case None =>
            val message = s"Could not determine type for `${psiElement.getText}`. ${noInfo.message}"
            HaskellNotificationGroup.logInfoEvent(psiFile.getProject, message)
            message
        }
    }
  }

  private def showTypeSignatureAsHint(project: Project, editor: Editor, sticky: Boolean, typeSignature: String) = {
    HaskellEditorUtil.showHint(editor, StringUtil.escapeString(typeSignature), sticky)
  }

  private def findTypeSignatureFromScope(psiFile: PsiFile, psiElement: PsiElement) = {
    if (HaskellPsiUtil.findExpressionParent(psiElement).isDefined) {
      HaskellPsiUtil.findQualifiedNameParent(psiElement).flatMap(qualifiedNameElement => {
        val name = qualifiedNameElement.getName
        val moduleName = HaskellPsiUtil.findModuleName(psiFile)
        HaskellCompletionContributor.getAvailableModuleIdentifiers(psiFile, moduleName).find(_.name == name).map(_.declaration).
          orElse(HaskellPsiUtil.findHaskellDeclarationElements(psiFile).find(_.getIdentifierElements.exists(_.getName == name)).map(_.getText.replaceAll("""\s+""", " ")))
      })
    } else {
      None
    }
  }
}
