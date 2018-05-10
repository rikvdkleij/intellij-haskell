/*
 * Copyright 2014-2017 Rik van der Kleij
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
import intellij.haskell.HaskellNotificationGroup
import intellij.haskell.external.component.NameInfoComponentResult.{BuiltInNameInfo, LibraryNameInfo, ProjectNameInfo}
import intellij.haskell.psi.{HaskellPsiUtil, HaskellQualifiedNameElement}
import intellij.haskell.util.{HaskellEditorUtil, HaskellProjectUtil}

class HaskellDocumentationProvider extends AbstractDocumentationProvider {

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
          case _: ProjectNameInfo => HaskellPsiUtil.findModuleName(namedElement.getContainingFile, runInRead = true)
          case _: BuiltInNameInfo => Some(HaskellProjectUtil.Prelude)
          case _ => None
        }
        HoogleComponent.findDocumentation(project, name, moduleName)
    }
  }
}
