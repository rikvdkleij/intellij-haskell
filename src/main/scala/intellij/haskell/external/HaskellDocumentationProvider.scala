/*
 * Copyright 2016 Rik van der Kleij
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

package intellij.haskell.external

import java.util.regex.Pattern

import com.intellij.lang.documentation.AbstractDocumentationProvider
import com.intellij.psi.PsiElement
import intellij.haskell.external.component.{BuiltInNameInfo, LibraryNameInfo, ProjectNameInfo, StackReplsComponentsManager}
import intellij.haskell.psi.{HaskellPsiUtil, HaskellQualifiedNameElement}
import intellij.haskell.settings.HaskellSettingsState
import intellij.haskell.util.StackUtil

class HaskellDocumentationProvider extends AbstractDocumentationProvider {

  override def generateDoc(psiElement: PsiElement, originalPsiElement: PsiElement): String = {
    HaskellPsiUtil.findQualifiedNameElement(originalPsiElement) match {
      case Some(hne) => getHaskellDoc(hne).getOrElse("No documentation found")
      case _ => s"No documentation because this is not Haskell identifier: ${psiElement.getText}"
    }
  }

  private def getHaskellDoc(namedElement: HaskellQualifiedNameElement): Option[String] = {
    HaskellSettingsState.getHaskellDocsPath.flatMap { hdp =>
      val name = namedElement.getIdentifierElement.getName
      val nameInfo = StackReplsComponentsManager.findNameInfo(namedElement).headOption
      val arguments = nameInfo.flatMap {
        case (lei: LibraryNameInfo) => Some(Seq(lei.moduleName, name))
        case (pei: ProjectNameInfo) => HaskellPsiUtil.findModuleName(namedElement.getContainingFile).map(mn => Seq(mn, name))
        case (bei: BuiltInNameInfo) => Some(Seq("Prelude", name))
      }

      arguments.map(args => StackUtil.runCommand(Seq("exec", "--", hdp) ++ args, namedElement.getContainingFile.getProject).getStdout).
        map(output => s"${Pattern.compile("$", Pattern.MULTILINE).matcher(output).replaceAll("<br>").replace(" ", "&nbsp;")}")
    }
  }
}
