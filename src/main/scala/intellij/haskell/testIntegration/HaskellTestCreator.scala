package intellij.haskell.testIntegration

import com.intellij.navigation.ItemPresentation
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.testIntegration.TestCreator
import intellij.haskell.HaskellIcons
import javax.swing.Icon

/**
  * Provides the "Create new test" action in the "GotoTestOrCodeAction" action
  */
class HaskellTestCreator extends TestCreator with ItemPresentation {

  /**
    * Should this action be available for this context?
    */
  override def isAvailable(project: Project, editor: Editor, psiFile: PsiFile): Boolean = {
    //TODO Is there any check we should do here?
    true
  }

  /**
    * What to do if the user actually clicked on the "Create new test" action
    */
  override def createTest(project: Project, editor: Editor, psiFile: PsiFile): Unit = {
    val offset = editor.getCaretModel.getOffset
    var element = psiFile.findElementAt(offset)
    if (element == null && offset == psiFile.getTextLength) element = psiFile.findElementAt(offset - 1)

    new CreateHaskellTestAction().invoke(project, editor, element)
  }

  override def getPresentableText: String = {
    "Create New Test..."
  }

  override def getLocationString: String = {
    //TODO What's this?
    "This is my location string"
  }

  /**
    * Would be cool to have a Haskell Test icon, unfortunately I suck really hard at anything graphical...
    */
  override def getIcon(unused: Boolean): Icon = {
    HaskellIcons.HaskellSmallLogo
  }
}
