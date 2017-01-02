package intellij.haskell.action.repl

import java.io.File

import com.intellij.openapi.actionSystem.{AnActionEvent, CommonDataKeys}
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.psi.PsiDocumentManager
import com.intellij.testFramework.LightVirtualFile
import intellij.haskell.{HaskellFile, HaskellIcons}

object LoadHaskellFileInConsoleAction {
  private def getActionFile(e: AnActionEvent): String = {
    val m = RunHaskellConsoleAction.getModule(e)
    if (m == null) return null
    val editor = e.getData(CommonDataKeys.EDITOR)
    if (editor == null || editor.getProject == null) return null
    val psiFile = PsiDocumentManager.getInstance(editor.getProject).getPsiFile(editor.getDocument)
    if (psiFile == null || !psiFile.isInstanceOf[HaskellFile]) return null
    val virtualFile = psiFile.getVirtualFile
    if (virtualFile == null || virtualFile.isInstanceOf[LightVirtualFile]) return null
    virtualFile.getPath
  }
}

final class LoadHaskellFileInConsoleAction() extends HaskellConsoleActionBase {
  getTemplatePresentation.setIcon(HaskellIcons.REPL)

  def actionPerformed(e: AnActionEvent) {
    val editor = e.getData(CommonDataKeys.EDITOR)
    if (editor == null) return
    val project = editor.getProject
    if (project == null) return
    val document = editor.getDocument
    val psiFile = PsiDocumentManager.getInstance(project).getPsiFile(document)
    if (psiFile == null || !psiFile.isInstanceOf[HaskellFile]) return
    val virtualFile = psiFile.getVirtualFile
    if (virtualFile == null) return
    val filePath = virtualFile.getPath
    PsiDocumentManager.getInstance(project).commitAllDocuments()
    FileDocumentManager.getInstance.saveAllDocuments()
    val command = ":load \"" + filePath + "\""
    executeCommand(project, command)
  }

  override def update(e: AnActionEvent) {
    val presentation = e.getPresentation
    val filePath = LoadHaskellFileInConsoleAction.getActionFile(e)
    if (filePath == null) presentation.setVisible(false)
    else {
      val f = new File(filePath)
      presentation.setVisible(true)
      presentation.setText(String.format("Load \"%s\" in Haskell REPL", f.getName))
    }
  }
}
