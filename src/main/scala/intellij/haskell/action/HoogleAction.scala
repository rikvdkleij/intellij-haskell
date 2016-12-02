/*
 * Copyright 2016 Rik van der Kleij
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
import com.intellij.openapi.util.text.StringUtil
import intellij.haskell.external.component.HoogleComponent
import intellij.haskell.util.HaskellEditorUtil

class HoogleAction extends AnAction {

  override def update(actionEvent: AnActionEvent) {
    HaskellEditorUtil.enableAction(onlyForProjectFile = false, actionEvent)
  }

  def actionPerformed(actionEvent: AnActionEvent) {
    ActionUtil.findActionContext(actionEvent).foreach(actionContext => {
      val editor = actionContext.editor
      val psiFile = actionContext.psiFile
      val offset = editor.getCaretModel.getOffset

      val selectionModel = actionContext.selectionModel
      selectionModel.map(_.getSelectedText).orElse(Option(psiFile.findElementAt(offset)).map(_.getText)).foreach(text => {
        HoogleComponent.runHoogle(psiFile.getProject, text) match {
          case Some(results) => HaskellEditorUtil.showList(results, editor)
          case _ => HaskellEditorUtil.showHint(editor, s"No Hoogle result for ${StringUtil.escapeXml(text)}")
        }
      })
    })
  }
}
