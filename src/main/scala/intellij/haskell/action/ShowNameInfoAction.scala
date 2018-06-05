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
import com.intellij.openapi.util.text.StringUtil
import intellij.haskell.external.component.NameInfoComponentResult._
import intellij.haskell.external.component._
import intellij.haskell.util.HaskellEditorUtil

class ShowNameInfoAction extends AnAction {

  override def update(actionEvent: AnActionEvent) {
    HaskellEditorUtil.enableAction(onlyForProjectFile = false, actionEvent)
  }

  def actionPerformed(actionEvent: AnActionEvent) {
    if (!StackProjectManager.isBuilding(actionEvent.getProject)) {
      ActionUtil.findActionContext(actionEvent).foreach(actionContext => {
        val editor = actionContext.editor
        val psiFile = actionContext.psiFile
        val offset = editor.getCaretModel.getOffset
        Option(psiFile.findElementAt(offset)).foreach(psiElement => {
          val result = HaskellComponentsManager.findNameInfo(psiElement)
          result match {
            case Some(r) => r match {
              case Left(info) => info.message
              case Right(nameInfos) => HaskellEditorUtil.showList(nameInfos.toSeq.map(createInfoText), editor)
            }
            case None => HaskellEditorUtil.showHint(editor, s"Could not determine info for ${StringUtil.escapeXml(psiElement.getText)}")
          }
        })
      })
    } else {
      HaskellEditorUtil.showHaskellSupportIsNotAvailableWhileBuilding(actionEvent.getProject)
    }
  }

  private def createInfoText(nameInfo: NameInfo): String = {
    nameInfo match {
      case pi: ProjectNameInfo => s"${pi.declaration}   -- ${pi.filePath}"
      case li: LibraryNameInfo => s"${li.shortenedDeclaration}   -- ${li.moduleName}"
      case bi: BuiltInNameInfo => s"${bi.shortenedDeclaration}   -- ${bi.moduleName}  BUILT-IN"
      case ii: InfixInfo => ii.declaration
    }
  }

}
