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
import com.intellij.psi.{PsiElement, PsiFile}
import intellij.haskell.psi.HaskellPsiUtil
import intellij.haskell.psi.impl.HaskellPsiImplUtil
import intellij.haskell.util.{HaskellEditorUtil, HaskellProjectUtil, HtmlElement, StringUtil}

class HaskellDocumentationProvider extends AbstractDocumentationProvider {

  private final val DoubleNbsp = HtmlElement.Nbsp + HtmlElement.Nbsp

  override def getQuickNavigateInfo(element: PsiElement, originalElement: PsiElement): String = {
    val project = Option(element).map(_.getProject)
    if (project.exists(p => !StackProjectManager.isBuilding(p))) {
      (Option(element), Option(originalElement)) match {
        case (Some(e), Some(oe)) =>
          val psiFile = Option(e.getContainingFile)
          val moduleName = psiFile.flatMap(HaskellPsiUtil.findModuleName)
          val originalPsiFile = Option(oe.getContainingFile)
          val projectFile = originalPsiFile.exists(HaskellProjectUtil.isProjectFile)
          val typeSignature = if (projectFile) {
            val typeInfo = TypeInfoComponent.findTypeInfoForElement(oe).toOption.map(_.typeSignature)
            typeInfo.map(StringUtil.escapeString)
          } else {
            None
          }

          (moduleName, typeSignature) match {
            case (Some(mn), Some(ts)) => s"""$DoubleNbsp $ts $DoubleNbsp -- $mn """
            case (Some(mn), None) => s"""$DoubleNbsp $mn $DoubleNbsp -- No type info available""" + (if (projectFile) " (at this moment)" else "")
            case (None, Some(ts)) => s"""$DoubleNbsp $ts $DoubleNbsp -- No module info available (at this moment)"""
            case (None, None) => s"${DoubleNbsp}No info available (at this moment)"
          }
        case _ => null
      }
    } else {
      HaskellEditorUtil.HaskellSupportIsNotAvailableWhileBuildingText
    }
  }

  private final val Separator = HtmlElement.Break + HtmlElement.Break + HtmlElement.HorizontalLine + HtmlElement.Break

  override def generateDoc(element: PsiElement, originalElement: PsiElement): String = {
    val project = Option(element).map(_.getProject)
    if (project.exists(p => !StackProjectManager.isBuilding(p))) {
      (Option(element), Option(originalElement)) match {
        case (Some(e), Some(oe)) =>
          val project = e.getProject
          if (e.isInstanceOf[PsiFile]) {
            getQuickNavigateInfo(e, oe)
          } else {
            val definedInSameFile = Option(e.getContainingFile) == Option(oe.getContainingFile)
            if (definedInSameFile) {
              getQuickNavigateInfo(e, oe)
            } else {
              HaskellPsiUtil.findQualifiedNameParent(oe) match {
                case Some(qone) =>
                  val presentationText = HaskellPsiUtil.findNamedElement(e).flatMap { ne =>
                    if (HaskellPsiUtil.findExpressionParent(ne).isDefined || HaskellPsiUtil.findTypeSignatureDeclarationParent(ne).isDefined) {
                      None
                    } else {
                      Some(DoubleNbsp + "<code>" +
                        HaskellPsiImplUtil.getItemPresentableText(ne, shortened = false).
                          replace(" ", HtmlElement.Nbsp).
                          replace("<", HtmlElement.Lt).
                          replace(">", HtmlElement.Gt).
                          replace("\n", HtmlElement.Break) +
                        "</code>")
                    }
                  }

                  val documentationText = HoogleComponent.findDocumentation(project, qone).getOrElse("No documentation found")
                  (documentationText + Separator + getQuickNavigateInfo(e, oe) + presentationText.map(t => Separator + t).getOrElse("")) + Separator
                case _ => getQuickNavigateInfo(e, oe)
              }
            }
          }
        case _ => null
      }
    } else {
      HaskellEditorUtil.HaskellSupportIsNotAvailableWhileBuildingText
    }
  }
}