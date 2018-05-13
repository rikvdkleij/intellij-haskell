/*
 * Copyright 2014-2017 Rik van der Kleij
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
            case Some(Right(info)) => HaskellEditorUtil.showHint(editor, StringUtil.escapeString(info.typeSignature))
            case _ => HaskellEditorUtil.showHint(editor, "Could not determine type for selection")
          }
          case _ => ()
            Option(psiFile.findElementAt(editor.getCaretModel.getOffset)).foreach { psiElement =>
            ShowTypeAction.showTypeHint(actionContext.project, editor, psiElement, psiFile)
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

  def showTypeHint(project: Project, editor: Editor, psiElement: PsiElement, psiFile: PsiFile, sticky: Boolean = false): Unit = {
    // FIXME For now disabled to improve to responsiveness
//    ApplicationManager.getApplication.executeOnPooledThread(new Runnable {
//      override def run(): Unit = {
//        TypeInfoUtil.preloadTypesAround(psiElement)
//      }
//    })

    HaskellComponentsManager.findTypeInfoForElement(psiElement) match {
      case Some(Right(info)) =>
        HaskellEditorUtil.showStatusBarInfoMessage(project, info.typeSignature)
        HaskellEditorUtil.showHint(editor, StringUtil.escapeString(info.typeSignature), sticky)
      case _ if HaskellPsiUtil.findExpressionParent(psiElement).isDefined =>
          // TODO Looks like redundant code
          // TODO Also not works
        val moduleNames = HaskellPsiUtil.findImportDeclarations(psiFile).flatMap(_.getModuleName)
        val declaration = HaskellPsiUtil.findQualifiedNameParent(psiElement).flatMap(qualifiedNameElement => {
          val name = qualifiedNameElement.getName
          HaskellCompletionContributor.getAvailableImportedModuleIdentifiers(psiFile).find(mi => moduleNames.exists(_ == mi.moduleName) && mi.name == name).map(_.declaration).
            orElse(findModuleName(psiFile).flatMap(mn => HaskellComponentsManager.findLocalModuleIdentifiers(psiFile, mn).find(_.name == name).map(_.declaration))).
            orElse(HaskellPsiUtil.findHaskellDeclarationElements(psiFile).find(_.getIdentifierElements.exists(_.getName == name)).map(_.getText.replaceAll("""\s+""", " ")))
        })

        declaration match {
          case Some(d) =>
            HaskellEditorUtil.showStatusBarInfoMessage(project, d)
            HaskellEditorUtil.showHint(editor, StringUtil.escapeString(d), sticky)
          case None => showNoTypeInfoHint(editor, psiElement)
        }
      case _ => showNoTypeInfoHint(editor, psiElement)
    }
  }

  private def findModuleName(psiFile: PsiFile): Option[String] = {
    HaskellPsiUtil.findModuleName(psiFile, runInRead = true)
  }

  private def showNoTypeInfoHint(editor: Editor, psiElement: PsiElement): Unit = {
    HaskellEditorUtil.showHint(editor, s"Could not determine type for ${StringUtil.escapeString(psiElement.getText)}")
  }
}
