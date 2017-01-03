package intellij.haskell.action.repl

import com.intellij.openapi.actionSystem._
import com.intellij.openapi.module.{Module, ModuleManager}
import com.intellij.openapi.project.{DumbAware, Project}
import intellij.haskell.HaskellIcons
import intellij.haskell.repl.HaskellConsoleRunner

object RunHaskellREPLAction {
  private[repl] def getModule(project: Project): Option[Module] = {
    Option(project).flatMap(project => {
      ModuleManager.getInstance(project).getModules.headOption
    })
  }

  private[repl] def getModule(e: AnActionEvent): Option[Module] = {
    Option(e.getData(LangDataKeys.MODULE)) match {
      case m@Some(_) => m
      case None => val project = e.getData(CommonDataKeys.PROJECT)
        getModule(project)
    }
  }
}

final class RunHaskellREPLAction() extends AnAction with DumbAware {
  getTemplatePresentation.setIcon(HaskellIcons.REPL)

  override def update(e: AnActionEvent) {
    val presentation = e.getPresentation

    RunHaskellREPLAction.getModule(e) match {
      case Some(_) =>
        presentation.setEnabled(true)
        super.update(e)
      case None => presentation.setEnabled(false)
    }
  }

  def actionPerformed(event: AnActionEvent) {
    RunHaskellREPLAction.getModule(event).foreach(module => {
      HaskellConsoleRunner.run(module)
    })
  }
}
