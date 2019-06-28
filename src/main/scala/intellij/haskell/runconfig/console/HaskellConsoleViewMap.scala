package intellij.haskell.runconfig.console

import java.util.concurrent.ConcurrentHashMap

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile

import scala.collection.concurrent
import scala.jdk.CollectionConverters._

object HaskellConsoleViewMap {
  private val consoleViews = new ConcurrentHashMap[Editor, HaskellConsoleView]().asScala

  def addConsole(console: HaskellConsoleView): Unit = {
    consoleViews.put(console.getConsoleEditor, console)
  }

  def delConsole(console: HaskellConsoleView): Unit = {
    consoleViews.remove(console.getConsoleEditor)
  }

  def getConsole(editor: Editor): Option[HaskellConsoleView] = {
    consoleViews.get(editor)
  }

  def getConsole(editor: Project): Option[HaskellConsoleView] = {
    consoleViews.values.find(console => console.project == editor && console.isShowing)
  }

  // File is project file and not file which represents console
  val projectFileByConfigName: concurrent.Map[String, PsiFile] = new ConcurrentHashMap[String, PsiFile]().asScala
}
