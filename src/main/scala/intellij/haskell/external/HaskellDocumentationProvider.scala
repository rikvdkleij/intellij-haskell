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
import com.intellij.openapi.project.Project
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.{PsiElement, PsiFile}
import intellij.haskell.HaskellNotificationGroup
import intellij.haskell.external.component._
import intellij.haskell.psi.{HaskellModuleDeclaration, HaskellNamedElement}
import intellij.haskell.sdk.HaskellSdkType
import intellij.haskell.settings.HaskellSettingsState

import scala.collection.JavaConversions._

class HaskellDocumentationProvider extends AbstractDocumentationProvider {

  override def generateDoc(psiElement: PsiElement, originalPsiElement: PsiElement): String = {
    originalPsiElement.getParent match {
      case hne: HaskellNamedElement => getHaskellDoc(hne).getOrElse("No documentation found")
      case e => s"No documentation because this is not Haskell identifier: ${e.getText}"
    }
  }

  private def getHaskellDoc(namedElement: HaskellNamedElement): Option[String] = {
    HaskellSettingsState.getHaskellDocsPath.flatMap { hd =>
      val identifier = namedElement.getName
      val file = namedElement.getContainingFile
      val project = file.getProject
      val identifierInfo = StackReplsComponentsManager.findNameInfo(namedElement).headOption
      val arguments = identifierInfo.map { ii =>
        (ii, getModuleName(file), getPackageDbOption(project)) match {
          case (lei: LibraryNameInfo, _, Some(dbPathOption)) => Seq("-g", dbPathOption, lei.moduleName, identifier)
          case (pei: ProjectNameInfo, _, Some(dbPathOption)) => Seq("-g", dbPathOption, PsiTreeUtil.findChildOfType(file, classOf[HaskellModuleDeclaration]).getModuleName.get, identifier)
          case (bei: BuiltInNameInfo, _, _) => Seq("Prelude", identifier)
          case (_, Some(moduleName), Some(dbPathOption)) => Seq("-g", dbPathOption, moduleName, identifier)
          case (_, _, _) => Seq()
        }
      }

      arguments.map(args => CommandLine.getProcessOutput(project.getBasePath, hd, args).getStdout).filterNot(_.trim.isEmpty).map(output =>
        "<p>" + s"${Pattern.compile("$", Pattern.MULTILINE).matcher(output).replaceAll("<br>").replace(" ", "&nbsp;")}" + "</p>"
      )
    }
  }

  private def getModuleName(psiFile: PsiFile) = {
    Option(PsiTreeUtil.findChildOfType(psiFile, classOf[HaskellModuleDeclaration])).flatMap(_.getModuleName)
  }

  private def getPackageDbOption(project: Project) = {
    val path = HaskellSdkType.getStackPath(project).flatMap(sp => CommandLine.getProcessOutput(project.getBasePath, sp, Seq("path", "--local-pkg-db")).getStdoutLines.headOption)
    if (path.isEmpty) {
      HaskellNotificationGroup.logWarning("Could not determine locale package db with `stack path --local-pkg-db`")
    }
    path
  }
}
