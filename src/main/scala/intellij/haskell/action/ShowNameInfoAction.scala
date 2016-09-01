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

import com.intellij.openapi.actionSystem.{AnAction, AnActionEvent, CommonDataKeys}
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.util.PsiUtilBase
import intellij.haskell.external.component._
import intellij.haskell.util.HaskellEditorUtil

class ShowNameInfoAction extends AnAction {

  override def update(actionEvent: AnActionEvent) {
    HaskellEditorUtil.enableAction(onlyForProjectFile = false, actionEvent)
  }

  def actionPerformed(actionEvent: AnActionEvent) {
    val context = actionEvent.getDataContext
    for {
      editor <- Option(CommonDataKeys.EDITOR.getData(context))
      psiFile <- Option(PsiUtilBase.getPsiFileInEditor(editor, CommonDataKeys.PROJECT.getData(context)))
      offset = editor.getCaretModel.getOffset
      psiElement <- Option(psiFile.findElementAt(offset))
    } yield
      StackReplsComponentsManager.findNameInfo(psiElement) match {
        case Seq(identifierInfos@_*) if identifierInfos.nonEmpty => HaskellEditorUtil.showInfoMessageBallon(identifierInfos.map(createInfoText).mkString("<br>"), editor)
        case _ => HaskellEditorUtil.showHint(editor, s"Could not determine info for ${StringUtil.escapeXml(psiElement.getText)}")
      }
  }

  private def createInfoText(nameInfo: NameInfo): String = {
    nameInfo match {
      case pi: ProjectNameInfo => s"${pi.escapedDeclaration}   -- ${pi.filePath}"
      case li: LibraryNameInfo => s"${li.escapedDeclaration}   -- ${li.moduleName}"
      case bi: BuiltInNameInfo => s"${bi.escapedDeclaration}   -- ${bi.moduleName}  BUILT-IN"
    }
  }
}
