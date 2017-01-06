package intellij.haskell.repl

import com.intellij.execution.console.LanguageConsoleImpl
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.testFramework.LightVirtualFile
import intellij.haskell.HaskellFileType

final class HaskellConsole private[repl](val project: Project, val title: String)
  extends LanguageConsoleImpl(project, title, HaskellFileType.INSTANCE.getLanguage) {}

object HaskellConsole {
  def isHaskellConsoleFile(psiFile: PsiFile): Boolean = {
    psiFile.getVirtualFile != null &&
      psiFile.getVirtualFile.isInstanceOf[LightVirtualFile] &&
      psiFile.getName.startsWith(HaskellConsoleRunner.REPLTitle)
  }
}
