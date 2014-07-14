/*
 * Copyright 2014 Rik van der Kleij

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

package com.powertuple.intellij.haskell.actions

import java.awt.Point
import java.awt.event.{MouseEvent, MouseMotionAdapter}

import com.intellij.codeInsight.hint.{HintManager, HintManagerImpl, HintUtil}
import com.intellij.openapi.actionSystem.{AnActionEvent, CommonDataKeys}
import com.intellij.openapi.editor.Editor
import com.intellij.ui.LightweightHint
import com.intellij.util.ui.UIUtil
import com.powertuple.intellij.haskell.psi.HaskellFile

object HaskellActionUtil {

  def enableAndShowIfInHaskellFile(e: AnActionEvent) {
    val presentation = e.getPresentation
    def enable() {
      presentation.setEnabled(true)
      presentation.setVisible(true)
    }
    def disable() {
      presentation.setEnabled(false)
      presentation.setVisible(false)
    }
    try {
      val dataContext = e.getDataContext
      val file = CommonDataKeys.PSI_FILE.getData(dataContext)
      file match {
        case _: HaskellFile => enable()
        case _ => disable()
      }
    }
    catch {
      case e: Exception => disable()
    }
  }

  def showHint(editor: Editor, text: String) {
    val label = HintUtil.createInformationLabel(text)
    label.setFont(UIUtil.getLabelFont)

    val hint = new LightweightHint(label)

    val hintManager = HintManagerImpl.getInstanceImpl

    label.addMouseMotionListener(new MouseMotionAdapter {
      override def mouseMoved(e: MouseEvent) {
        hintManager.hideAllHints()
      }
    })

    val position = editor.getCaretModel.getLogicalPosition
    val point = HintManagerImpl.getHintPosition(hint, editor, position, HintManager.ABOVE)

    hintManager.showEditorHint(hint, editor, point,
      HintManager.HIDE_BY_ANY_KEY | HintManager.HIDE_BY_TEXT_CHANGE | HintManager.HIDE_BY_SCROLLING, 0, false)
  }
}