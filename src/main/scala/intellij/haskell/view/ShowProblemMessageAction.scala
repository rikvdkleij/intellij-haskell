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

package intellij.haskell.view

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.codeInsight.daemon.impl.DaemonCodeAnalyzerImpl
import com.intellij.openapi.actionSystem.{AnAction, AnActionEvent, CommonDataKeys}
import com.intellij.xml.util.{XmlUtil, HtmlUtil}
import intellij.haskell.util.HaskellEditorUtil

class ShowProblemMessageAction extends AnAction {

  override def update(actionEvent: AnActionEvent) {
    HaskellEditorUtil.enableAndShowIfInHaskellFile(actionEvent)
  }

  override def actionPerformed(actionEvent: AnActionEvent): Unit = {
    val context = actionEvent.getDataContext
    for {
      project <- Option(CommonDataKeys.PROJECT.getData(context))
      editor <- Option(CommonDataKeys.EDITOR.getData(context))
      offset = editor.getCaretModel.getOffset
      codeAnalyzer <- Option(DaemonCodeAnalyzer.getInstance(project).asInstanceOf[DaemonCodeAnalyzerImpl])
      info <- Option(codeAnalyzer.findHighlightByOffset(editor.getDocument, offset, false))
      message = info.getDescription
    } yield {
      HaskellEditorUtil.showHint(editor, XmlUtil.escape(message).replace(" ", "&nbsp;"))
    }
  }
}
