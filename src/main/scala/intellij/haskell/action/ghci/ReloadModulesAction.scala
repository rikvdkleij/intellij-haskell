package intellij.haskell.action.ghci

import com.intellij.openapi.actionSystem.{AnAction, AnActionEvent}
import intellij.haskell.action.ActionUtil
import intellij.haskell.runconfig.console.HaskellConsoleViewMap
import intellij.haskell.util.HaskellEditorUtil

class ReloadModulesAction extends AnAction {

  override def update(actionEvent: AnActionEvent): Unit = {
    HaskellEditorUtil.enableAction(onlyForProjectFile = true, actionEvent)
  }

  def actionPerformed(actionEvent: AnActionEvent): Unit = {
    for {
      actionContext <- ActionUtil.findActionContext(actionEvent)
      consoleView <- HaskellConsoleViewMap.getConsole(actionContext.project)
    } yield {
      consoleView.executeCommand(":reload", silent = true)
    }
  }

}
