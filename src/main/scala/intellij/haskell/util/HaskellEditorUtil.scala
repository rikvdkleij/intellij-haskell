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

package intellij.haskell.util

import java.awt.Point
import java.awt.event.{MouseEvent, MouseMotionAdapter}

import com.intellij.codeInsight.hint.{HintManager, HintManagerImpl, HintUtil}
import com.intellij.openapi.actionSystem.{AnActionEvent, CommonDataKeys}
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.MessageType
import com.intellij.openapi.ui.popup.util.BaseListPopupStep
import com.intellij.openapi.ui.popup.{Balloon, JBPopupFactory}
import com.intellij.openapi.wm.WindowManager
import com.intellij.ui.LightweightHint
import com.intellij.ui.awt.RelativePoint
import com.intellij.util.ui.{PositionTracker, UIUtil}
import intellij.haskell.HaskellFile

import scala.collection.JavaConversions._

object HaskellEditorUtil {

  def enableAction(onlyForProjectFile: Boolean, actionEvent: AnActionEvent): Unit = {
    val presentation = actionEvent.getPresentation
    def enable() {
      presentation.setEnabled(true)
      presentation.setVisible(true)
    }
    def disable() {
      presentation.setEnabled(false)
      presentation.setVisible(false)
    }
    try {
      val dataContext = actionEvent.getDataContext
      val psiFile = CommonDataKeys.PSI_FILE.getData(dataContext)
      psiFile match {
        case _: HaskellFile if !onlyForProjectFile => enable()
        case f: HaskellFile if onlyForProjectFile && HaskellProjectUtil.isProjectFile(psiFile) => enable()
        case _ => disable()
      }
    }
    catch {
      case e: Exception => disable()
    }
  }

  def showHint(editor: Editor, text: String): Unit = {
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

  def showInfoMessageBallon(message: String, editor: Editor, inCenterOfEditor: Boolean): Unit = {
    UIUtil.invokeLaterIfNeeded(new Runnable {
      override def run() {
        val balloon = JBPopupFactory.getInstance().createHtmlTextBalloonBuilder(message, MessageType.INFO, null).setCloseButtonEnabled(true).setHideOnAction(true).createBalloon()
        if (inCenterOfEditor) {
          balloon.showInCenterOf(editor.getComponent)
        } else {
          balloon.show(new PositionTracker[Balloon](editor.getContentComponent) {
            def recalculateLocation(balloon: Balloon): RelativePoint = {
              val popupFactory = JBPopupFactory.getInstance
              val target: RelativePoint = popupFactory.guessBestPopupLocation(editor)
              val screenPoint: Point = target.getScreenPoint
              var y: Int = screenPoint.y
              if (target.getPoint.getY > editor.getLineHeight + balloon.getPreferredSize.getHeight) {
                y -= editor.getLineHeight
              }
              val relativePoint = new RelativePoint(new Point(screenPoint.x, y))
              relativePoint
            }
          }, Balloon.Position.above)
        }
      }
    })
  }

  def showList(messages: Seq[String], editor: Editor): Unit = {
    UIUtil.invokeLaterIfNeeded(new Runnable {
      override def run() {
        val listPopupStep = new BaseListPopupStep[String]("info", messages)
        val listPopup = JBPopupFactory.getInstance().createListPopup(listPopupStep)
        listPopup.showInBestPositionFor(editor)
      }
    })
  }

  def showStatusBarInfoMessage(message: String, project: Project): Unit = {
    WindowManager.getInstance().getStatusBar(project).setInfo(message)
  }
}