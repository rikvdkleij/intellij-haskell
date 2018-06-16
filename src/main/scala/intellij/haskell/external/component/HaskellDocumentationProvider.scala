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
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import intellij.haskell.HaskellNotificationGroup
import intellij.haskell.external.component.NameInfoComponentResult.{BuiltInNameInfo, LibraryNameInfo, ProjectNameInfo}
import intellij.haskell.psi.{HaskellPsiUtil, HaskellQualifiedNameElement}
import intellij.haskell.util.index.HaskellFilePathIndex
import intellij.haskell.util.{HaskellEditorUtil, HaskellProjectUtil}

class HaskellDocumentationProvider extends AbstractDocumentationProvider {

  override def getQuickNavigateInfo(element: PsiElement, originalElement: PsiElement): String = {
    val moduleName = HaskellFilePathIndex.findModuleName(element.getContainingFile, GlobalSearchScope.projectScope(element.getProject))
    val projectFile = Option(originalElement.getContainingFile).exists(HaskellProjectUtil.isProjectFile)
    val typeSignature = if (projectFile) {
      TypeInfoComponent.findTypeInfoForElement(originalElement).toOption.map(_.typeSignature)
    } else {
      None
    }
    (moduleName, typeSignature) match {
      case (Some(mn), Some(ts)) => s"""$ts &nbsp;&nbsp; -- $mn """
      case (Some(mn), None) => s"""$mn &nbsp;&nbsp; -- No type info available""" + (if (projectFile) " (at this moment)" else "")
      case (None, Some(ts)) => s"""$ts &nbsp;&nbsp; -- No module info available (at this moment)"""
      case (None, None) => "No info available (at this moment)"
    }
  }

  override def generateDoc(psiElement: PsiElement, originalPsiElement: PsiElement): String = {
    val project = psiElement.getProject
    if (!StackProjectManager.isBuilding(project)) {
      HaskellPsiUtil.findQualifiedNameParent(originalPsiElement) match {
        case Some(ne) => findDocumentation(project, ne).getOrElse("No documentation found")
        case _ => s"No documentation because this is not Haskell identifier: ${psiElement.getText}"
      }
    } else {
      HaskellEditorUtil.HaskellSupportIsNotAvailableWhileBuildingText
    }
  }

  private def findDocumentation(project: Project, namedElement: HaskellQualifiedNameElement): Option[String] = {
    val name = namedElement.getIdentifierElement.getName
    val nameInfo = HaskellComponentsManager.findNameInfo(namedElement).flatMap(_.right.toOption)
    nameInfo match {
      case None =>
        HaskellNotificationGroup.logWarningEvent(project, s"No documentation because no info could be found for identifier: $name")
        None
      case Some(ni) =>
        val moduleName = ni match {
          case lei: LibraryNameInfo => Option(lei.moduleName)
          case _: ProjectNameInfo => HaskellFilePathIndex.findModuleName(namedElement.getContainingFile, GlobalSearchScope.projectScope(project))
          case _: BuiltInNameInfo => Some(HaskellProjectUtil.Prelude)
          case _ => None
        }
        HoogleComponent.findDocumentation(project, name, moduleName)
    }
  }
}
