/*
 * Copyright 2014 Rik van der Kleij
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
import com.intellij.openapi.project.Project
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.{PsiElement, PsiFile}
import com.powertuple.intellij.haskell.HaskellNotificationGroup
import com.powertuple.intellij.haskell.external._
import com.powertuple.intellij.haskell.psi.{HaskellModuleDeclaration, HaskellNamedElement}
import com.powertuple.intellij.haskell.settings.HaskellSettings

import scala.io.Source
import scala.util.{Failure, Success, Try}

class HaskellDocumentationProvider extends AbstractDocumentationProvider {

  private final val CabalSandboxConfigFile = "cabal.sandbox.config"
  private final val PackageDb = "package-db"
  private final val SandboxPackageDbPattern = s"""$PackageDb: (.*)""".r

  private var sandboxPackageDbPath: Option[String] = None

  override def generateDoc(psiElement: PsiElement, originalPsiElement: PsiElement): String = {
    originalPsiElement.getParent match {
      case hne: HaskellNamedElement => getHaskellDoc(hne)
      case e => s"No documentation because this is not Haskell identifier: ${e.getText}"
    }
  }

  private def getHaskellDoc(namedElement: HaskellNamedElement): String = {
    val identifier = namedElement.getName
    val file = namedElement.getContainingFile
    val project = file.getProject
    val identifierInfo = GhcModiInfo.findInfoFor(file, namedElement).headOption
    val haskellDocs = HaskellSettings.getInstance().getState.haskellDocsPath
    val arguments = (identifierInfo, getSandboxPackageDbPath(project), getModuleName(file)) match {
      case (Some(lei: LibraryIdentifierInfo), Some(dbPath), _) => Seq("-g", s"-package-db=$dbPath", lei.module, identifier)
      case (Some(pei: ProjectIdentifierInfo), Some(dbPath), _) => Seq("-g", s"-package-db=$dbPath", PsiTreeUtil.findChildOfType(file, classOf[HaskellModuleDeclaration]).getModuleName, identifier)
      case (Some(bei: BuiltInIdentifierInfo), _, _) => Seq("Prelude", identifier)
      case (Some(lei: LibraryIdentifierInfo), None, _) => Seq(lei.module, identifier)
      case (None, Some(dbPath), Some(moduleName)) => Seq("-g", s"-package-db=$dbPath", getModuleName(file).get, identifier)
      case (_, None, _) => HaskellNotificationGroup.notifyInfo(s"Can not determine haskell-docs GHC option `package-db` for $identifier"); Seq()
      case (_, _, _) => HaskellNotificationGroup.notifyInfo(s"Can not determine haskell-docs arguments <module-name> for $identifier"); Seq()
    }

    val stdOutputput = ExternalProcess.getProcessOutput(project.getBasePath, haskellDocs, arguments).getStdout
    if (stdOutputput.isEmpty) {
      "No documentation found."
    } else {
      Pattern.compile("$", Pattern.MULTILINE).matcher(stdOutputput).replaceAll("<br>").replace(" ", "&nbsp;")
    }
  }

  private def getSandboxPackageDbPath(project: Project) = {
    sandboxPackageDbPath.orElse(findSandboxPackageDbPath(project))
  }

  private def getModuleName(psiFile: PsiFile) = {
    Option(PsiTreeUtil.findChildOfType(psiFile, classOf[HaskellModuleDeclaration])).map(_.getModuleName)
  }

  private def findSandboxPackageDbPath(project: Project): Option[String] = {
    val configFile = findCabalSandboxConfigFile(project) match {
      case Success(cf) => Some(cf)
      case Failure(e) => HaskellNotificationGroup.notifyInfo(s"Can not find $CabalSandboxConfigFile"); None
    }

    sandboxPackageDbPath = for {
      cf <- configFile
      pdbl <- cf.getLines().find(s => s.startsWith(PackageDb))
      dbPath <- pdbl match {
        case SandboxPackageDbPattern(dbPath) => Some(dbPath);
        case _ => HaskellNotificationGroup.notifyInfo(s"Can not find $PackageDb in $CabalSandboxConfigFile"); None
      }
    } yield dbPath
    sandboxPackageDbPath
  }

  private def findCabalSandboxConfigFile(project: Project) = Try {
    Source.fromFile(project.getBasePath + "/" + CabalSandboxConfigFile)
  }
}
