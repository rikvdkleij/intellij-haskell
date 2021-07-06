/*
 * Copyright 2014-2020 Rik van der Kleij
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

package intellij.haskell.refactor

import com.intellij.lang.refactoring.RefactoringSupportProvider
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.command.WriteCommandAction.writeCommandAction
import com.intellij.openapi.editor.{Editor, SelectionModel}
import com.intellij.openapi.project.Project
import com.intellij.psi._
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.refactoring.RefactoringActionHandler
import intellij.haskell.psi.HaskellExpression
import intellij.haskell.util.HaskellProjectUtil

class HaskellRefactoringSupportProvider extends RefactoringSupportProvider {

  override def isMemberInplaceRenameAvailable(psiElement: PsiElement, context: PsiElement): Boolean = {
    !psiElement.isInstanceOf[PsiFile] && isDefinedInProject(psiElement)
  }

  private def isDefinedInProject(psiElement: PsiElement) = {
    Option(psiElement.getReference).flatMap(x => Option(x.resolve)) match {
      case Some(e) => Option(e.getContainingFile).map(_.getOriginalFile).exists(pf => HaskellProjectUtil.isSourceFile(pf))
      case _ => false
    }
  }

  override def getIntroduceVariableHandler: RefactoringActionHandler = {
    new RefactoringActionHandler {
      override def invoke(project: Project, editor: Editor, file: PsiFile, dataContext: DataContext): Unit = {
        val model: SelectionModel = editor.getSelectionModel

        if (!model.hasSelection) return

        val element1: PsiElement = file.findElementAt(model.getSelectionStart)
        val element2: PsiElement = file.findElementAt(model.getSelectionEnd - 1)

        // Ideally, once we fix the parser, this would be the expression selection
        var parent = PsiTreeUtil.findCommonParent(element1, element2)
        parent = PsiTreeUtil.getParentOfType[HaskellExpression](parent, classOf[HaskellExpression])

        if (!(parent != null
          && PsiTreeUtil.getDeepestFirst(parent) == element1
          && PsiTreeUtil.getDeepestLast(parent) == element2))
          return

        writeCommandAction(project, file).withName("Introduce Variable").withGroupId(null).run(() => {
          editor.getDocument.replaceString(model.getSelectionStart, model.getSelectionEnd, "x")
        })
      }

      override def invoke(project: Project, elements: Array[PsiElement], dataContext: DataContext): Unit = {
        // This does not get called
      }
    }
  }
}
