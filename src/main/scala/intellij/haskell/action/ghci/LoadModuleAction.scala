package intellij.haskell.action.ghci

import com.intellij.openapi.actionSystem.{AnAction, AnActionEvent}
import intellij.haskell.action.ActionUtil
import intellij.haskell.runconfig.console.HaskellConsoleViewMap
import intellij.haskell.util.HaskellFileUtil

class LoadModuleAction extends AnAction {

  override def update(actionEvent: AnActionEvent): Unit = {
    actionEvent.getPresentation.setEnabled(HaskellConsoleViewMap.getConsole(actionEvent.getProject).isDefined)
  }

  def actionPerformed(actionEvent: AnActionEvent): Unit = {
    for {
      actionContext <- ActionUtil.findActionContext(actionEvent)
      consoleView <- HaskellConsoleViewMap.getConsole(actionContext.project)
    } yield {
      consoleView.executeCommand(s":load ${HaskellFileUtil.getAbsolutePath(actionContext.psiFile)}", addToHistory = false)
    }
  }

}
