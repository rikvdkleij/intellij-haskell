package intellij.haskell.action.repl

import java.io.File

import com.intellij.openapi.actionSystem.{AnActionEvent, CommonDataKeys}
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.psi.PsiDocumentManager
import com.intellij.testFramework.LightVirtualFile
import intellij.haskell.{HaskellFile, HaskellIcons}

object LoadHaskellFileAction {
  private def getActionFile(e: AnActionEvent): Option[String] = {
    for {
      _ <- RunHaskellREPLAction.getModule(e)
      editor <- Option(e.getData(CommonDataKeys.EDITOR))
      project <- Option(editor.getProject)
      psiFile <- Option(PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument))
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
      editor <- Option(e.getData(CommonDataKeys.EDITOR))
      project <- Option(editor.getProject)
      document <- Option(editor.getDocument)
      psiFile <- Option(PsiDocumentManager.getInstance(project).getPsiFile(document))
      if psiFile.isInstanceOf[HaskellFile]
      virtualFile <- Option(psiFile.getVirtualFile)
    } yield {
      val filePath = virtualFile.getPath
      PsiDocumentManager.getInstance(project).commitAllDocuments()
      FileDocumentManager.getInstance.saveAllDocuments()
      val command = ":load \"" + filePath + "\""
      executeCommand(project, command)
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
