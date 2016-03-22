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

package com.powertuple.intellij.haskell.view

import java.util.regex.Pattern

import com.intellij.lang.documentation.AbstractDocumentationProvider
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.{PsiElement, PsiFile}
import com.powertuple.intellij.haskell.HaskellNotificationGroup
import com.powertuple.intellij.haskell.external._
import com.powertuple.intellij.haskell.psi.{HaskellModuleDeclaration, HaskellNamedElement}
import com.powertuple.intellij.haskell.settings.HaskellSettingsState

class HaskellDocumentationProvider extends AbstractDocumentationProvider {

  override def generateDoc(psiElement: PsiElement, originalPsiElement: PsiElement): String = {
    originalPsiElement.getParent match {
      case hne: HaskellNamedElement => getHaskellDoc(hne).getOrElse("-")
      case e => s"No documentation because this is not Haskell identifier: ${e.getText}"
    }
  }

  private def getHaskellDoc(namedElement: HaskellNamedElement) = {
    val haskellDocs = HaskellSettingsState.getHaskellDocsPath
    haskellDocs match {
      case Some(hd) =>
        val identifier = namedElement.getName
        val file = namedElement.getContainingFile
        val project = file.getProject
        val identifierInfo = GhcModInfo.findInfoFor(file, namedElement).headOption
        val arguments = (identifierInfo, getModuleName(file)) match {
          case (Some(lei: LibraryIdentifierInfo), _) => Seq(lei.module, identifier)
          case (Some(pei: ProjectIdentifierInfo), _) => Seq(PsiTreeUtil.findChildOfType(file, classOf[HaskellModuleDeclaration]).getModuleName, identifier)
          case (Some(bei: BuiltInIdentifierInfo), _) => Seq("Prelude", identifier)
          case (Some(lei: LibraryIdentifierInfo), _) => Seq(lei.module, identifier)
          case (None, Some(moduleName)) => Seq(getModuleName(file).get, identifier)
        }

        val output = ExternalProcess.getProcessOutput(project.getBasePath, hd, arguments).getStdout
        Some(if (output.isEmpty) {
          "No documentation found."
        } else {
          Pattern.compile("$", Pattern.MULTILINE).matcher(output).replaceAll("<br>").replace(" ", "&nbsp;")
        })
      case None =>
        HaskellNotificationGroup.notifyError("Path to `haskell-docs` not set")
        None
    }
  }

  private def getModuleName(psiFile: PsiFile) = {
    Option(PsiTreeUtil.findChildOfType(psiFile, classOf[HaskellModuleDeclaration])).map(_.getModuleName)
  }
}
