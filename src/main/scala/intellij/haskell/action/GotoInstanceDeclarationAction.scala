/*
 * Copyright 2014-2019 Rik van der Kleij
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

import com.intellij.codeInsight.navigation.NavigationUtil
import com.intellij.openapi.actionSystem.{AnAction, AnActionEvent}
import com.intellij.openapi.util.text.StringUtil
import intellij.haskell.external.component.HaskellComponentsManager
import intellij.haskell.navigation.HaskellReference
import intellij.haskell.psi.HaskellPsiUtil
import intellij.haskell.util.HaskellEditorUtil

class GotoInstanceDeclarationAction extends AnAction {

  override def update(actionEvent: AnActionEvent): Unit = {
    HaskellEditorUtil.enableAction(onlyForSourceFile = false, actionEvent)
  }

  def actionPerformed(actionEvent: AnActionEvent): Unit = {
    ActionUtil.findActionContext(actionEvent).foreach(actionContext => {
      val editor = actionContext.editor
      val psiFile = actionContext.psiFile
      val project = actionContext.project
      val offset = editor.getCaretModel.getOffset
      Option(psiFile.findElementAt(offset)).flatMap(HaskellPsiUtil.findNamedElement).foreach(namedElement => {
        val instanceElements = HaskellComponentsManager.findNameInfo(namedElement) match {
          case Right(nameInfos) => HaskellReference.resolveInstanceReferences(project, namedElement, nameInfos)
          case Left(info) =>
            HaskellEditorUtil.showHint(editor, info.message)
            Seq()
        }

        if (instanceElements.nonEmpty) {
          val popup = NavigationUtil.getPsiElementPopup(instanceElements.toArray, "Goto instance declaration")
          popup.showInBestPositionFor(editor)
        } else {
          HaskellEditorUtil.showHint(editor, s"No instance declarations found for ${StringUtil.escapeXmlEntities(namedElement.getText)}")
        }
      })
    })
  }

}
