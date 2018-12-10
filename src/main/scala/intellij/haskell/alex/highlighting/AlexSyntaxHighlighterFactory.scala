package intellij.haskell.alex.highlighting

import com.intellij.openapi.fileTypes.{SyntaxHighlighter, SyntaxHighlighterFactory}
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

/**
  * @author ice1000
  */
class AlexSyntaxHighlighterFactory extends SyntaxHighlighterFactory {
  override def getSyntaxHighlighter(project: Project, virtualFile: VirtualFile): SyntaxHighlighter = {
    new AlexSyntaxHighlighter
  }
}
