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

package intellij.haskell.external.component

import com.intellij.lang.documentation.AbstractDocumentationProvider
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.{PsiElement, PsiFile}
import intellij.haskell.psi.HaskellPsiUtil
import intellij.haskell.psi.impl.HaskellPsiImplUtil
import intellij.haskell.util.index.HaskellFilePathIndex
import intellij.haskell.util.{HaskellEditorUtil, HaskellProjectUtil, HtmlElement, StringUtil}

class HaskellDocumentationProvider extends AbstractDocumentationProvider {

  private final val DoubleNbsp = HtmlElement.Nbsp + HtmlElement.Nbsp

  override def getQuickNavigateInfo(element: PsiElement, originalElement: PsiElement): String = {

    val project = element.getProject
    if (!StackProjectManager.isBuilding(project)) {
      val psiFile = Option(element.getContainingFile)
      val moduleName = psiFile.flatMap(pf => HaskellFilePathIndex.findModuleName(pf, GlobalSearchScope.projectScope(element.getProject)))
      val originalPsiFile = Option(originalElement.getContainingFile)
      val projectFile = originalPsiFile.exists(HaskellProjectUtil.isProjectFile)
      val typeSignature = if (projectFile) {
        val typeInfo = TypeInfoComponent.findTypeInfoForElement(originalElement).toOption.map(_.typeSignature)
        typeInfo.map(StringUtil.escapeString)
      } else {
        None
      }

      (moduleName, typeSignature) match {
        case (Some(mn), Some(ts)) => s"""$ts $DoubleNbsp -- $mn """
        case (Some(mn), None) => s"""$mn $DoubleNbsp -- No type info available""" + (if (projectFile) " (at this moment)" else "")
        case (None, Some(ts)) => s"""$ts $DoubleNbsp -- No module info available (at this moment)"""
        case (None, None) => "No info available (at this moment)"
      }
    } else {
      HaskellEditorUtil.HaskellSupportIsNotAvailableWhileBuildingText
    }
  }

  private final val Separator = HtmlElement.Break + HtmlElement.Break + HtmlElement.HorizontalLine + HtmlElement.Break

  override def generateDoc(element: PsiElement, originalElement: PsiElement): String = {
    val project = element.getProject
    if (!StackProjectManager.isBuilding(project)) {
      val definedInSameFile = Option(element.getContainingFile) == Option(originalElement.getContainingFile)
      if (definedInSameFile || element.isInstanceOf[PsiFile]) {
        getQuickNavigateInfo(element, originalElement)
      } else {
        HaskellPsiUtil.findQualifiedNameParent(originalElement) match {
          case Some(qone) =>
            val documentation = HoogleComponent.findDocumentation(project, qone).getOrElse("No documentation found")
            val presentation = HaskellPsiUtil.findNamedElement(element) map { ne =>
              "<code>" +
                HaskellPsiImplUtil.getItemPresentableText(ne, shortened = false).
                  replace(" ", HtmlElement.Nbsp).
                  replace("<", HtmlElement.Lt).
                  replace(">", HtmlElement.Gt).
                  replace("\n", HtmlElement.Break) +
                "</code>"
            }

            getQuickNavigateInfo(element, originalElement) + presentation.map(p => Separator + p).getOrElse("") + Separator + documentation

          case _ => getQuickNavigateInfo(element, originalElement) + Separator + s"No documentation available for identifier: `${originalElement.getText}`"
        }
      }
    } else {
      HaskellEditorUtil.HaskellSupportIsNotAvailableWhileBuildingText
    }
  }
}