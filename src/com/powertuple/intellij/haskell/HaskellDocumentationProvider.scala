/*
 * Copyright 2014 Rik van der Kleij

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

package com.powertuple.intellij.haskell

import java.util.regex.Pattern

import com.intellij.lang.documentation.AbstractDocumentationProvider
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.powertuple.intellij.haskell.external.{ExternalProcess, GhcModiManager, LibraryExpressionInfo}
import com.powertuple.intellij.haskell.settings.HaskellSettings

import scala.io.Source
import scala.util.{Failure, Success, Try}

class HaskellDocumentationProvider extends AbstractDocumentationProvider {

  private final val CabalSandboxConfigFile = "cabal.sandbox.config"
  private final val PackageDb = "package-db"
  private final val SandboxPackageDbPattern = s"""$PackageDb: (.*)""".r

  private var sandboxPackageDbPath: Option[String] = None

  override def generateDoc(psiElement: PsiElement, originalPsiElement: PsiElement): String = {
    val expression = originalPsiElement.getText
    val psiFile = originalPsiElement.getContainingFile
    val project = psiFile.getProject
    val haskellDocs = HaskellSettings.getInstance().getState.haskellDocsPath

    val expressionInfo = GhcModiManager.findInfoFor(psiFile, expression)
    val arguments = (expressionInfo, getSandboxPackageDbPath(project)) match {
      case (Some(lei: LibraryExpressionInfo), Some(dbPath)) => Seq("-g", s"-package-db=$dbPath", s"${lei.module}", expression)
      case (Some(lei: LibraryExpressionInfo), None) => Seq(s"${lei.module}", expression)
      case (_, _) => HaskellNotificationGroup.notifyInfo("Can not determine haskell-docs argument 'module'"); Seq()
    }

    val stdOutputput = ExternalProcess.getProcessOutput(project.getBasePath, haskellDocs, arguments).getStdout
    Pattern.compile("$", Pattern.MULTILINE).matcher(stdOutputput).replaceAll("<br>").replace(" ", "&nbsp;")
  }

  private def getSandboxPackageDbPath(project: Project) = {
    sandboxPackageDbPath.orElse(findSandboxPackageDbPath(project))
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
