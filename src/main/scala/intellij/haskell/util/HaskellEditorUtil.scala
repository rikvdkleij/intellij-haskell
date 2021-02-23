/*
 * Copyright 2014-2020 Rik van der Kleij
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
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.MessageType
import com.intellij.openapi.ui.popup.util.BaseListPopupStep
import com.intellij.openapi.ui.popup.{Balloon, JBPopupFactory}
import com.intellij.openapi.wm.WindowManager
import com.intellij.openapi.wm.ex.StatusBarEx
import com.intellij.psi.{PsiElement, PsiFile}
import com.intellij.ui.LightweightHint
import com.intellij.ui.awt.RelativePoint
import com.intellij.util.ui.UIUtil.FontSize
import com.intellij.util.ui.{PositionTracker, UIUtil}
import intellij.haskell.HaskellFile
import javax.swing.Icon
import javax.swing.event.HyperlinkListener

import scala.jdk.CollectionConverters._

object HaskellEditorUtil {

  final val HaskellSupportIsNotAvailableWhileInitializingText = "Haskell support is not available while project is initializing"

  def enableExternalAction(actionEvent: AnActionEvent, enableCondition: Project => Boolean): Unit = {
    Option(actionEvent.getProject) match {
      case Some(project) if HaskellProjectUtil.isHaskellProject(project) =>
        actionEvent.getPresentation.setVisible(true)
        actionEvent.getPresentation.setEnabled(HaskellProjectUtil.isValidHaskellProject(project, notifyNoSdk = false) && enableCondition(project))
      case _ => actionEvent.getPresentation.setEnabledAndVisible(false)
    }
  }

  def enableAction(onlyForSourceFile: Boolean, actionEvent: AnActionEvent): Unit = {
    val presentation = actionEvent.getPresentation

    def enable(): Unit = {
      presentation.setEnabled(true)
      presentation.setVisible(true)
    }

    def disable(): Unit = {
      presentation.setEnabled(false)
      presentation.setVisible(false)
    }

    try {
      val dataContext = actionEvent.getDataContext
      val psiFile = CommonDataKeys.PSI_FILE.getData(dataContext)
      if (HaskellProjectUtil.isHaskellProject(psiFile.getProject)) {
        psiFile match {
          case _: HaskellFile if !onlyForSourceFile => enable()
          case _: HaskellFile if onlyForSourceFile && HaskellProjectUtil.isSourceFile(psiFile) => enable()
          case _ => disable()
        }
      } else {
        disable()
      }
    }
    catch {
      case _: Exception => disable()
    }
  }

  def showHint(editor: Editor, text: String, sticky: Boolean = false): Unit = {
    val label = HintUtil.createInformationLabel(text)
    label.setFont(UIUtil.getLabelFont(FontSize.NORMAL))

    val hint = new LightweightHint(label)
    val hintManager = HintManagerImpl.getInstanceImpl

    label.addMouseMotionListener(new MouseMotionAdapter {
      override def mouseMoved(e: MouseEvent): Unit = {
        hintManager.hideAllHints()
      }
    })

    val position = editor.getCaretModel.getLogicalPosition
    val point = HintManagerImpl.getHintPosition(hint, editor, position, HintManager.ABOVE)
    val hintHint = HintManagerImpl.createHintHint(editor, point, hint, HintManager.ABOVE).setExplicitClose(sticky)

    val hideFlags = if (sticky) {
      HintManager.HIDE_BY_ESCAPE
    } else {
      HintManager.HIDE_BY_ANY_KEY | HintManager.HIDE_BY_TEXT_CHANGE | HintManager.HIDE_BY_SCROLLING
    }

    hintManager.showEditorHint(hint, editor, point, hideFlags, 0, false, hintHint)
  }

  def showInfoMessageBalloon(message: String, editor: Editor, inCenterOfEditor: Boolean): Unit = {
    UIUtil.invokeLaterIfNeeded(() => {
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
    })
  }

  def showList(messages: Seq[String], editor: Editor): Unit = {
    UIUtil.invokeLaterIfNeeded(() => {
      val listPopupStep = new BaseListPopupStep[String]("info", messages.asJava) {
        override def isSpeedSearchEnabled: Boolean = true
      }
      val listPopup = JBPopupFactory.getInstance().createListPopup(listPopupStep)
      listPopup.showInBestPositionFor(editor)
    })
  }

  def showStatusBarMessage(project: Project, message: String): Unit = {
    for {
      wm <- Option(WindowManager.getInstance())
      sb <- Option(wm.getStatusBar(project))
    } yield sb.setInfo(message)
  }

  def showStatusBarBalloonMessage(project: Project, message: String): Unit = {
    UIUtil.invokeLaterIfNeeded(() => {
      def run() = {
        for {
          wm <- Option(WindowManager.getInstance)
          f <- Option(wm.getIdeFrame(project))
          statusBar <- Option(f.getStatusBar.asInstanceOf[StatusBarEx])
        } yield {
          statusBar.isProcessWindowOpen
          statusBar.notifyProgressByBalloon(MessageType.WARNING, message, null.asInstanceOf[Icon], null.asInstanceOf[HyperlinkListener])
        }
      }

      run()
    })
  }

  def findCurrentElement(psiFile: PsiFile): Option[PsiElement] = {
    val offset = Option(FileEditorManagerEx.getInstanceEx(psiFile.getProject).getSelectedTextEditor).map(_.getCaretModel.getCurrentCaret.getOffset)
    offset.map(psiFile.findElementAt)
  }

  def showHaskellSupportIsNotAvailableWhileInitializing(project: Project): Unit = {
    HaskellEditorUtil.showStatusBarMessage(project, HaskellSupportIsNotAvailableWhileInitializingText)
  }
}