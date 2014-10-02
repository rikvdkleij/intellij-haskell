/*
 * Copyright 2014 Rik van der Kleij
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

package com.powertuple.intellij.haskell.view

import com.intellij.openapi.actionSystem.{AnAction, AnActionEvent, CommonDataKeys}
import com.intellij.psi.util.PsiUtilBase
import com.powertuple.intellij.haskell.external._
import com.powertuple.intellij.haskell.util.HaskellEditorUtil

class ShowInfoAction extends AnAction {

  override def update(actionEvent: AnActionEvent) {
    HaskellEditorUtil.enableAndShowIfInHaskellFile(actionEvent)
  }

  def actionPerformed(actionEvent: AnActionEvent) {
    val context = actionEvent.getDataContext
    for {
      editor <- Option(CommonDataKeys.EDITOR.getData(context))
      psiFile <- Option(PsiUtilBase.getPsiFileInEditor(editor, CommonDataKeys.PROJECT.getData(context)))
      offset = editor.getCaretModel.getOffset
      project = psiFile.getProject
      expression <- Option(psiFile.findElementAt(offset)).map(_.getText.trim).flatMap(t => if (t.isEmpty) None else Some(t))
    } yield
      GhcModiManager.findInfoFor(psiFile, expression) match {
        case Seq(expressionInfos@_*) if expressionInfos.nonEmpty => HaskellEditorUtil.createInfoBallon(expressionInfos.map(createInfoText).mkString("<br>"), editor)
        case _ => HaskellEditorUtil.showHint(editor, s"Could not determine info for $expression")
      }
  }

  private def createInfoText(expressionInfo: ExpressionInfo): String = {
    expressionInfo match {
      case pei: ProjectExpressionInfo => s"${pei.typeSignature}  -- ${pei.filePath}"
      case lei: LibraryExpressionInfo => s"${lei.typeSignature}   -- ${lei.module}"
      case bei: BuiltInExpressionInfo => s"${bei.typeSignature}   -- ${bei.module}  BUILT-IN"
    }
  }
}
