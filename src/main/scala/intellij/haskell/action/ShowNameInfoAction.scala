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
import intellij.haskell.external.component._
import intellij.haskell.util.HaskellEditorUtil

class ShowNameInfoAction extends AnAction {

  override def update(actionEvent: AnActionEvent) {
    HaskellEditorUtil.enableAction(onlyForProjectFile = false, actionEvent)
  }

  def actionPerformed(actionEvent: AnActionEvent) {
    ActionUtil.findActionContext(actionEvent).foreach(actionContext => {
      val editor = actionContext.editor
      val psiFile = actionContext.psiFile
      val offset = editor.getCaretModel.getOffset
      Option(psiFile.findElementAt(offset)).foreach(psiElement => {
        val nameInfos = StackReplsComponentsManager.findNameInfo(psiElement)
        if (nameInfos.nonEmpty) {
          HaskellEditorUtil.showList(nameInfos.toSeq.map(createInfoText), editor)
        } else {
          HaskellEditorUtil.showHint(editor, s"Could not determine info for ${StringUtil.escapeXml(psiElement.getText)}")
        }
      })
    })
  }

  private def createInfoText(nameInfo: NameInfo): String = {
    nameInfo match {
      case pi: ProjectNameInfo => s"${pi.declaration}   -- ${pi.filePath}"
      case li: LibraryNameInfo => s"${li.shortenedDeclaration}   -- ${li.moduleName}"
      case bi: BuiltInNameInfo => s"${bi.shortenedDeclaration}   -- ${bi.moduleName}  BUILT-IN"
    }
  }
}
