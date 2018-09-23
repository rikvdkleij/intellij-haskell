/*
 * Copyright 2014-2018 Rik van der Kleij
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
    //TODO
    true
  }

  /**
    * What to do if the user actually clicked on the "Create new test" action
    */
  override def createTest(project: Project, editor: Editor, psiFile: PsiFile): Unit = {
    val action = new CreateHaskellTestAction
    //TODO Check this
    val element = findElement(psiFile, editor.getCaretModel.getOffset)
    action.invoke(project, editor, element)
  }

  private def findElement(file: PsiFile, offset: Int) = {
    var element = file.findElementAt(offset)
    if (element == null && offset == file.getTextLength) element = file.findElementAt(offset - 1)
    element
  }

  override def getPresentableText: String = {
    "Create New Test..."
  }

  override def getLocationString: String = {
    "This is my location string"
  }

  override def getIcon(unused: Boolean): Icon = {
    HaskellIcons.HaskellSmallLogo
  }
}
