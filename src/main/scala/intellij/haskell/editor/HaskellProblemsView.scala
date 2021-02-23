package intellij.haskell.editor

import java.util
import java.util.UUID

import com.intellij.compiler.ProblemsView
import com.intellij.compiler.impl.ProblemsViewPanel
import com.intellij.compiler.progress.CompilerTask
import com.intellij.icons.AllIcons
import com.intellij.ide.errorTreeView.{ErrorTreeElement, ErrorTreeElementKind, GroupingElement}
import com.intellij.openapi.compiler.{CompileScope, CompilerMessage}
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.{Disposer, IconLoader}
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.{ToolWindowAnchor, ToolWindowManager}
import com.intellij.pom.Navigatable
import com.intellij.ui.content.ContentFactory
import com.intellij.util.concurrency.SequentialTaskExecutor
import com.intellij.util.ui.UIUtil
import intellij.haskell.util.HaskellProjectUtil

class HaskellProblemsView(project: Project) extends ProblemsView(project) {

  private final val ProblemsToolWindowId = "Haskell Problems"
  private final val ActiveIcon = AllIcons.Toolwindows.Problems
  private final val PassiveIcon = IconLoader.getDisabledIcon(ActiveIcon)

  private val viewUpdater = SequentialTaskExecutor.createSequentialApplicationPoolExecutor("ProblemsView Pool")

  private lazy val problemsPanel = new ProblemsViewPanel(project)

  private val toolWindowManager = ToolWindowManager.getInstance(project)

  Disposer.register(project, () => {
    Disposer.dispose(problemsPanel)
  })

  if (HaskellProjectUtil.isHaskellProject(project)) {
    UIUtil.invokeLaterIfNeeded(() => {
      if (!project.isDisposed) {
        val toolWindow = toolWindowManager.registerToolWindow(ProblemsToolWindowId, false, ToolWindowAnchor.LEFT, project, true)
        val content = ContentFactory.SERVICE.getInstance.createContent(problemsPanel, "", false)
        content.setHelpId("reference.problems.tool.window")
        toolWindow.getContentManager.addContent(content)
        Disposer.register(project, () => {
          toolWindow.getContentManager.removeAllContents(true)
        })
        updateIcon()
      }
    })
  }

  def clearOldMessages(currentFile: VirtualFile): Unit = {
    viewUpdater.execute(() => {
      cleanupChildrenRecursively(problemsPanel.getErrorViewStructure.getRootElement.asInstanceOf[ErrorTreeElement], currentFile)
      updateIcon()
      problemsPanel.reload()
    })
  }

  def clearOldMessages(scope: CompileScope, currentSessionId: UUID): Unit = {
    // This method can be called. Do not know the reason. See issue #419.
    // For now do nothing because do not know why this method is called.
  }

  override def addMessage(messageCategoryIndex: Int, text: Array[String], groupName: String, navigatable: Navigatable, exportTextPrefix: String, rendererTextPrefix: String, sessionId: UUID): Unit = {
    viewUpdater.execute(() => {
      if (navigatable != null) {
        problemsPanel.addMessage(messageCategoryIndex, text, groupName, navigatable, exportTextPrefix, rendererTextPrefix, sessionId)
      }
      else {
        problemsPanel.addMessage(messageCategoryIndex, text, null, -1, -1, sessionId)
      }
      updateIcon()
    })
  }

  def addMessage(message: CompilerMessage): Unit = {
    val file = message.getVirtualFile
    val navigatable = if (message.getNavigatable == null && file != null && !file.getFileType.isBinary) {
      new OpenFileDescriptor(myProject, file, -1, -1)
    } else {
      message.getNavigatable
    }
    val category = message.getCategory
    val categoryIndex = CompilerTask.translateCategory(category)
    val messageText = splitMessage(message)
    val groupName = if (file != null) file.getPresentableUrl else category.getPresentableText
    addMessage(categoryIndex, messageText, groupName, navigatable, message.getExportTextPrefix, message.getRenderTextPrefix, null)
  }

  def clear(): Unit = {
    val view = problemsPanel.getErrorViewStructure
    view.clear()
    problemsPanel.reload()
  }

  override def setProgress(text: String, fraction: Float): Unit = {
    problemsPanel.setProgress(text, fraction)
  }

  override def setProgress(text: String): Unit = {
    problemsPanel.setProgressText(text)
  }

  override def clearProgress(): Unit = {
    problemsPanel.clearProgressData()
  }

  private def splitMessage(message: CompilerMessage): Array[String] = {
    val messageText = message.getMessage
    if (messageText.contains("\n")) {
      messageText.split("\n")
    } else {
      Array[String](messageText)
    }
  }

  private def cleanupChildrenRecursively(errorTreeElement: ErrorTreeElement, currentFile: VirtualFile): Unit = {
    val errorViewStructure = problemsPanel.getErrorViewStructure
    for (element <- errorViewStructure.getChildElements(errorTreeElement)) {
      element match {
        case groupElement: GroupingElement =>
          if (groupElement.getFile == currentFile) {
            cleanupChildrenRecursively(element, currentFile)
          }
        case _ => errorViewStructure.removeElement(element)
      }
    }
  }

  private def updateIcon(): Unit = {
    UIUtil.invokeLaterIfNeeded(() => {
      if (!myProject.isDisposed) {
        val toolWindow = ToolWindowManager.getInstance(myProject).getToolWindow(ProblemsToolWindowId)
        if (toolWindow != null) {
          val active = problemsPanel.getErrorViewStructure.hasMessages(util.EnumSet.of(ErrorTreeElementKind.ERROR, ErrorTreeElementKind.WARNING, ErrorTreeElementKind.NOTE))
          toolWindow.setIcon(if (active) ActiveIcon else PassiveIcon)
        }
      }
    })
  }
}

object HaskellProblemsView {
  def getInstance(project: Project): HaskellProblemsView = {
    ProblemsView.SERVICE.getInstance(project).asInstanceOf[HaskellProblemsView]
  }
}