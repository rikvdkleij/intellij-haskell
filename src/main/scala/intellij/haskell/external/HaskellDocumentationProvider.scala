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
import intellij.haskell.HaskellNotificationGroup
import intellij.haskell.external.HaskellDocumentationProvider.HaskellDocsName
import intellij.haskell.external.component.{BuiltInNameInfo, LibraryNameInfo, ProjectNameInfo, StackReplsComponentsManager}
import intellij.haskell.psi.{HaskellPsiUtil, HaskellQualifiedNameElement}
import intellij.haskell.util.StackUtil

class HaskellDocumentationProvider extends AbstractDocumentationProvider {

  override def generateDoc(psiElement: PsiElement, originalPsiElement: PsiElement): String = {
    HaskellPsiUtil.findQualifiedNameElement(originalPsiElement) match {
      case Some(ne) => findDocumentation(ne).getOrElse("No documentation found")
      case _ => s"No documentation because this is not Haskell identifier: ${psiElement.getText}"
    }
  }

  private def findDocumentation(namedElement: HaskellQualifiedNameElement): Option[String] = {
    val name = namedElement.getIdentifierElement.getName
    val nameInfo = StackReplsComponentsManager.findNameInfo(namedElement).headOption
    if (nameInfo.isEmpty) {
      HaskellNotificationGroup.logWarning(s"No documenation because no info could be found for identifier: $name")
    }
    val arguments = nameInfo.flatMap {
      case (lei: LibraryNameInfo) => Some(Seq(lei.moduleName, name))
      case (pei: ProjectNameInfo) => HaskellPsiUtil.findModuleName(namedElement.getContainingFile).map(mn => Seq(mn, name))
      case (bei: BuiltInNameInfo) => Some(Seq("Prelude", name))
    }

    arguments.map(args => runHaskellDocs(namedElement, args)).
      map(output => s"${Pattern.compile("$", Pattern.MULTILINE).matcher(output).replaceAll("<br>").replace(" ", "&nbsp;")}")
  }

  private def runHaskellDocs(namedElement: HaskellQualifiedNameElement, args: Seq[String]): String = {
    val output = StackUtil.runCommand(Seq("exec", "--", HaskellDocsName) ++ args, namedElement.getContainingFile.getProject)
    if (output.getStderr.nonEmpty) {
      HaskellNotificationGroup.logError(s"Error while running $HaskellDocsName: ${output.getStderr}")
      if (output.getStderr.toLowerCase.contains("couldn't find file: haskell-docs")) {
        HaskellNotificationGroup.notifyBalloonWarning("No documentation because `haskell-docs` build still has to be started or build is not finished yet")
      }
    }
    output.getStdout
  }
}

object HaskellDocumentationProvider {
  final val HaskellDocsName = "haskell-docs"
}