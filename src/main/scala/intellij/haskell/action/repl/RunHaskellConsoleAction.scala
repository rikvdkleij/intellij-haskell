package intellij.haskell.action.repl

import com.intellij.openapi.actionSystem._
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import intellij.haskell.HaskellIcons
import intellij.haskell.repl.HaskellConsoleRunner

object RunHaskellConsoleAction {
  private[repl] def getModule(project: Project): Module = {
    if (project == null) return null
    val modules = ModuleManager.getInstance(project).getModules
    if (modules.nonEmpty) return modules(0)
    null
  }

  private[repl] def getModule(e: AnActionEvent): Module = {
    val module = e.getData(LangDataKeys.MODULE)
    if (module == null) {
      val project = e.getData(CommonDataKeys.PROJECT)
      getModule(project)
    }
    else module
  }
}

final class RunHaskellConsoleAction() extends AnAction with DumbAware {
  getTemplatePresentation.setIcon(HaskellIcons.REPL)

  override def update(e: AnActionEvent) {
    val m = RunHaskellConsoleAction.getModule(e)
    val presentation = e.getPresentation
    if (m == null) {
      presentation.setEnabled(false)
      return
    }
    presentation.setEnabled(true)
    super.update(e)
  }

  def actionPerformed(event: AnActionEvent) {
    val module = RunHaskellConsoleAction.getModule(event)
    HaskellConsoleRunner.run(module)
  }
}
