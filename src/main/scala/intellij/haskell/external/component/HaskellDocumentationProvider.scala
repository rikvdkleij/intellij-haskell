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
import intellij.haskell.psi.{HaskellPsiUtil, HaskellQualifiedNameElement}
import intellij.haskell.util.index.HaskellFilePathIndex
import intellij.haskell.util.{HaskellEditorUtil, HaskellProjectUtil}

class HaskellDocumentationProvider extends AbstractDocumentationProvider {

  override def getQuickNavigateInfo(element: PsiElement, originalElement: PsiElement): String = {
    val psiFile = Option(element.getContainingFile)
    val moduleName = psiFile.flatMap(pf => HaskellFilePathIndex.findModuleName(pf, GlobalSearchScope.projectScope(element.getProject)))
    val projectFile = psiFile.exists(HaskellProjectUtil.isProjectFile)
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

  override def generateDoc(element: PsiElement, originalElement: PsiElement): String = {
    val project = element.getProject
    if (!StackProjectManager.isBuilding(project)) {
      HaskellPsiUtil.findQualifiedNameParent(originalElement) match {
        case Some(qne) =>
          val doc = findDocumentation(project, qne).getOrElse("No documentation found")
          getQuickNavigateInfo(element, originalElement) + "<br/><br/><hr/><br/>" + doc
        case _ => getQuickNavigateInfo(element, originalElement) + "<br/><br/><hr/><br/>" + s"No documentation (yet) available for identifier: `${element.getText}`"
      }
    } else {
      HaskellEditorUtil.HaskellSupportIsNotAvailableWhileBuildingText
    }
  }

  private def findDocumentation(project: Project, qualifiedNameElement: HaskellQualifiedNameElement): Option[String] = {
    val name = qualifiedNameElement.getIdentifierElement.getName
    val namedElement = qualifiedNameElement.getIdentifierElement
    Option(qualifiedNameElement.getContainingFile).flatMap { psiFile =>
      if (HaskellProjectUtil.isProjectFile(psiFile)) {
        DefinitionLocationComponent.findDefinitionLocation(psiFile, qualifiedNameElement, namedElement, isCurrentFile = true) match {
          case Left(noInfo) =>
            HaskellNotificationGroup.logWarningEvent(project, s"No documentation because no location info could be found for identifier `$name` because ${noInfo.message}")
            None
          case Right(info) =>
            info.moduleName match {
              case None =>
                HaskellNotificationGroup.logWarningEvent(project, s"No documentation because could not find module for identifier `$name`")
                None
              case Some(moduleName) => HoogleComponent.findDocumentation(project, name, moduleName)
            }
        }
      } else {
        val moduleName = HaskellFilePathIndex.findModuleName(psiFile, GlobalSearchScope.allScope(project))
        moduleName.flatMap(mn => HoogleComponent.findDocumentation(project, name, mn))
      }
    }
  }
}