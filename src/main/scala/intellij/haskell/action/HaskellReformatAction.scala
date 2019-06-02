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

import com.intellij.codeInsight.actions.ReformatCodeAction
import com.intellij.openapi.actionSystem.{AnActionEvent, Presentation}
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import intellij.haskell.external.component.StackProjectManager
import intellij.haskell.util.{HaskellEditorUtil, HaskellFileUtil}

class HaskellReformatAction extends ReformatCodeAction {

  private val presentation: Presentation = getTemplatePresentation
  presentation.setText("Reformat Code")

  override def update(actionEvent: AnActionEvent) {
    ActionUtil.findActionContext(actionEvent).foreach(actionContext => {
      val psiFile = actionContext.psiFile
      if (HaskellFileUtil.isHaskellFile(psiFile)) {
        HaskellEditorUtil.enableExternalAction(actionEvent, (project: Project) => StackProjectManager.isStylishHaskellAvailable(project) && StackProjectManager.isHindentAvailable(project))
      } else {
        super.update(actionEvent)
      }
    })
  }

  override def actionPerformed(actionEvent: AnActionEvent): Unit = {
    ActionUtil.findActionContext(actionEvent).foreach(actionContext => {
      val psiFile = actionContext.psiFile
      if (HaskellFileUtil.isHaskellFile(psiFile)) {
        val selectionModel = actionContext.selectionModel
        selectionModel match {
          case Some(_) =>
            HindentReformatAction.format(psiFile, selectionModel.map(m =>
              HindentReformatAction.translateSelectionModelToSelectionContext(m)))
          case None => HaskellReformatAction.reformatFile(psiFile)
        }
      } else {
        super.actionPerformed(actionEvent)
      }
    })
  }
}

object HaskellReformatAction {

  def reformatFile(psiFile: PsiFile): Boolean = {
    HindentReformatAction.format(psiFile)
    StylishHaskellReformatAction.format(psiFile)
    true
  }
}