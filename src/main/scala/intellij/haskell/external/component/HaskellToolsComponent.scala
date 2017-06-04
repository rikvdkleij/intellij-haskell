package intellij.haskell.external.component

import java.io.File

import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.editor.{Editor, SelectionModel}
import com.intellij.openapi.progress.{ProgressIndicator, ProgressManager, Task}
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.psi.{PsiElement, PsiFile}
import intellij.haskell.external.commandLine.StackCommandLine
import intellij.haskell.util.{HaskellFileUtil, LineColumnPosition, StackYamlUtil}

import scala.collection.JavaConverters._

object HaskellToolsComponent {
  final val HaskellToolsCLIName = Seq("haskell-tools-cli-0.7.0.0", "haskell-tools-ast-0.7.0.0", "haskell-tools-refactor-0.7.0.0", "haskell-tools-backend-ghc-0.7.0.0",
    "haskell-tools-prettyprint-0.7.0.0", "haskell-tools-rewrite-0.7.0.0")

  final val HaskellToolName = "ht-refact"

  def isRefactoringSupported(project: Project): Boolean = {
    StackYamlUtil.getResolverFromStackYamlFile(project).exists(resolver => {
      if (resolver.startsWith("lts-")) {
        resolver.replace("lts-", "") >= "8.0"
      } else if (resolver.startsWith("nightly-")) {
        resolver.replace("nightly-", "") >= "2017-02-13"
      } else {
        false
      }
    })
  }

  def generateExports(project: Project, psiFile: PsiFile, moduleName: String): Unit = {
    runRefactor(project, psiFile, moduleName, "GenerateExports")
  }

  def extractBinding(project: Project, psiFile: PsiFile, moduleName: String, selectionModel: SelectionModel, newName: String): Unit = {
    getSrcRangeBySelectionModel(selectionModel).foreach(srcRange => {
      val mode = "\"ExtractBinding" + s" $srcRange" + "\""
      runRefactor(project, psiFile, moduleName, mode)
    })
  }

  def inlineBinding(project: Project, psiFile: PsiFile, moduleName: String, editor: Editor, selectionModel: Option[SelectionModel]): Unit = {
    findSrcRange(psiFile, selectionModel, editor).foreach(srcRange => {
      val mode = "\"InlineBinding" + s" $srcRange" + "\""
      runRefactor(project, psiFile, moduleName, mode)
    })
  }

  def generateTypeSignature(project: Project, psiFile: PsiFile, moduleName: String, editor: Editor, selectionModel: Option[SelectionModel]): Unit = {
    findSrcRange(psiFile, selectionModel, editor).foreach(srcRange => {
      val mode = "\"GenerateSignature" + s" $srcRange" + "\""
      runRefactor(project, psiFile, moduleName, mode)
    })
  }

  private def getAutoGenDirectory(project: Project): Seq[String] = {
    StackCommandLine.runCommand(Seq("path", "--dist-dir"), project).flatMap(_.getStdoutLines.asScala.headOption).map(_ + File.separator + "build" + File.separator + "autogen").toSeq
  }

  private def getRefactorCommand(project: Project, moduleName: String, mode: String): Seq[String] = {
    Seq("exec", "--", HaskellToolName, "-one-shot", s"-module-name=$moduleName", s"-refactoring=$mode", project.getBasePath) ++ getAutoGenDirectory(project)
  }

  private def findSrcRange(psiFile: PsiFile, selectionModel: Option[SelectionModel], editor: Editor) = {
    selectionModel match {
      case Some(sm) => getSrcRangeBySelectionModel(sm)
      case _ => Option(psiFile.findElementAt(editor.getCaretModel.getOffset)).flatMap(e => getSrcRangeByElement(psiFile, e))
    }
  }

  private def getSrcRangeByElement(psiFile: PsiFile, psiElement: PsiElement): Option[String] = {
    val startPoint = LineColumnPosition.fromOffset(psiFile, psiElement.getTextOffset)
    val endPoint = LineColumnPosition.fromOffset(psiFile, psiElement.getTextOffset + psiElement.getTextLength - 1)

    (startPoint, endPoint) match {
      case (Some(sp), Some(ep)) => Some(s"${sp.lineNr}:${sp.columnNr}-${ep.lineNr}:${ep.columnNr}")
      case (_, _) => None
    }
  }

  private def getSrcRangeBySelectionModel(selectionModel: SelectionModel) = {
    val startPoint = Option(selectionModel.getSelectionStartPosition)
    val endPoint = Option(selectionModel.getSelectionEndPosition)

    (startPoint, endPoint) match {
      case (Some(sp), Some(ep)) => Some(s"${sp.line + 1}:${sp.column + 1}-${ep.line + 1}:${ep.column}")
      case (_, _) => None
    }
  }

  private def runRefactor(project: Project, psiFile: PsiFile, moduleName: String, mode: String) = {
    import scala.concurrent.duration._

    ProgressManager.getInstance().run(new Task.Modal(project, "Refactoring...", false) {
      override def run(indicator: ProgressIndicator): Unit = {
        StackCommandLine.runCommand(getRefactorCommand(project, moduleName, mode), project, 60.seconds.toMillis, captureOutputToLog = true, logErrorAsInfo = true)
      }
    })

    CommandProcessor.getInstance().executeCommand(project, () => {
      HaskellFileUtil.findVirtualFile(psiFile).foreach(vf => VfsUtil.markDirtyAndRefresh(true, true, true, vf))
    }, null, null)
  }
}
