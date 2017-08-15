package intellij.haskell.runconfig.console

import java.util.concurrent.ConcurrentHashMap

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile

import scala.collection.JavaConverters._

object HaskellConsoleViewMap {
  val consoleViews = new ConcurrentHashMap[HaskellConsoleView, Option[PsiFile]]().asScala

  def addConsole(console: HaskellConsoleView) {
    consoleViews.put(console, None)
  }

  def delConsole(console: HaskellConsoleView) {
    consoleViews.remove(console)
  }

  def getConsole(editor: Editor): Option[(HaskellConsoleView, Option[PsiFile])] = {
    consoleViews.find(_._1.getConsoleEditor == editor)
  }

  def getConsole(project: Project): Option[(HaskellConsoleView, Option[PsiFile])] = {
    consoleViews.find(_._1.getProject == project)
  }


  val consoleFileViews = new ConcurrentHashMap[String, PsiFile]().asScala
}
