package intellij.haskell.action.repl

import java.io.File

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.psi.PsiDocumentManager
import com.intellij.testFramework.LightVirtualFile
import intellij.haskell.action.{ActionContext, ActionUtil}
import intellij.haskell.psi.HaskellPsiUtil
import intellij.haskell.{HaskellFile, HaskellIcons}

object LoadHaskellFileAction {
  private def getActionFile(e: AnActionEvent): Option[String] = {
    for {
      _ <- RunHaskellREPLAction.getModule(e)
      ActionContext(psiFile, _, _, _) <- ActionUtil.findActionContext(e)
      if psiFile.isInstanceOf[HaskellFile]
      virtualFile <- Option(psiFile.getVirtualFile)
      if !virtualFile.isInstanceOf[LightVirtualFile]
    } yield virtualFile.getPath
  }
}

final class LoadHaskellFileAction() extends HaskellREPLActionBase {
  getTemplatePresentation.setIcon(HaskellIcons.REPL)

  def actionPerformed(e: AnActionEvent) {
    for {
      ActionContext(psiFile, _, project, _) <- ActionUtil.findActionContext(e)
      if psiFile.isInstanceOf[HaskellFile]
      virtualFile <- Option(psiFile.getVirtualFile)
      moduleName <- HaskellPsiUtil.findModuleName(psiFile)
    } yield {
      val filePath = virtualFile.getPath
      PsiDocumentManager.getInstance(project).commitAllDocuments()
      FileDocumentManager.getInstance.saveAllDocuments()
      val command = ":load \"" + filePath + "\""
      executeCommand(project, command, moduleName)
    }
  }

  override def update(e: AnActionEvent) {
    val presentation = e.getPresentation
    LoadHaskellFileAction.getActionFile(e) match {
      case Some(filePath) =>
        val f = new File(filePath)
        presentation.setVisible(true)
        presentation.setText(String.format("Load \"%s\" in Haskell REPL", f.getName))
      case None => presentation.setVisible(false)
    }
  }
}
