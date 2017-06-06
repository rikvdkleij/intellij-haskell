package intellij.haskell.action

import com.intellij.openapi.actionSystem.{AnAction, AnActionEvent}
import com.intellij.openapi.progress.{ProgressIndicator, ProgressManager, Task}
import intellij.haskell.external.commandLine.StackCommandLine
import intellij.haskell.external.component.StackProjectManager.componentName
import intellij.haskell.external.component.{HoogleComponent, StackProjectManager}
import intellij.haskell.util.HaskellEditorUtil

class GenerateHoogleDbAction extends AnAction {

  override def update(actionEvent: AnActionEvent) {
    HaskellEditorUtil.enableExternalAction(actionEvent, StackProjectManager.isHoogleAvailable)
  }

  def actionPerformed(actionEvent: AnActionEvent) {
    Option(actionEvent.getProject).foreach { project =>
      ProgressManager.getInstance().run(new Task.Backgroundable(project, s"[$componentName] Generating Haskell documentation and Hoogle database", true) {
        def run(progressIndicator: ProgressIndicator) {
          StackCommandLine.executeBuild(project, Seq("haddock", "--only-dependencies", "--test"), "Haskell documentation")
          StackCommandLine.executeBuild(project, Seq(HoogleComponent.HoogleName, "--", "generate", "--local"), "Hoogle database")
        }
      })
    }
  }
}