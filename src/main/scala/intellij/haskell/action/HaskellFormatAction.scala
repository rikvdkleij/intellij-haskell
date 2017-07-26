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
import intellij.haskell.util.HaskellEditorUtil

class HaskellFormatAction extends AnAction {

  override def update(actionEvent: AnActionEvent) {
    HaskellEditorUtil.enableAction(onlyForProjectFile = true, actionEvent)
  }

  override def actionPerformed(actionEvent: AnActionEvent): Unit = {
    ActionUtil.findActionContext(actionEvent).foreach(actionContext => {
      val psiFile = actionContext.psiFile
      val selectionModel = actionContext.selectionModel
      selectionModel match {
        case Some(_) =>
          HindentFormatAction.format(psiFile, selectionModel.map(m =>
            HindentFormatAction.translateSelectionModelToSelectionContext(m)))
        case None =>
          HindentFormatAction.format(psiFile)
          StylishHaskellFormatAction.format(psiFile)
      }
    })
  }
}
