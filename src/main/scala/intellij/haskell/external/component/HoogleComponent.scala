/*
 * Copyright 2014-2019 Rik van der Kleij
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

import java.io.File

import com.intellij.execution.process.ProcessOutput
import com.intellij.lang.documentation.DocumentationMarkup
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import intellij.haskell.external.execution.{CommandLine, StackCommandLine}
import intellij.haskell.psi.{HaskellPsiUtil, HaskellQualifiedNameElement}
import intellij.haskell.util.{HtmlElement, ScalaFutureUtil}
import intellij.haskell.{GlobalInfo, HaskellNotificationGroup}

import scala.collection.JavaConverters._
import scala.concurrent.Future

object HoogleComponent {

  final val HoogleName = "hoogle"
  private final val HooglePath = GlobalInfo.toolPath(HoogleName).toString
  private final val HoogleDbName = "hoogle"

  def runHoogle(project: Project, pattern: String, count: Int = 100): Option[Seq[String]] = {
    if (isHoogleFeatureAvailable(project)) {
      ProgressManager.checkCanceled()

      runHoogle(project, Seq( s""""$pattern"""", s"--count=$count")).
        map(o =>
          if (o.getStdoutLines.isEmpty || o.getStdout.contains("No results found"))
            Seq()
          else if (o.getStdoutLines.asScala.last.startsWith("-- ")) {
            o.getStdoutLines.asScala.init
          } else {
            o.getStdoutLines.asScala
          }
        )
    } else {
      None
    }
  }

  def findDocumentation(project: Project, qualifiedNameElement: HaskellQualifiedNameElement): Option[String] = {
    if (isHoogleFeatureAvailable(project)) {
      ProgressManager.checkCanceled()

      val name = qualifiedNameElement.getIdentifierElement.getName
      val psiFile = qualifiedNameElement.getContainingFile.getOriginalFile
      DefinitionLocationComponent.findDefinitionLocation(psiFile, qualifiedNameElement, None) match {
        case Left(noInfo) =>
          HaskellNotificationGroup.logWarningEvent(project, s"No documentation because no location info could be found for identifier `$name` because ${noInfo.message}")
          None
        case Right(info) =>
          val locationName = info match {
            case PackageModuleLocation(_, _, _, pn) => pn
            case LocalModuleLocation(pf, _, _) => HaskellPsiUtil.findModuleName(pf)
          }
          ProgressManager.checkCanceled()
          HoogleComponent.createDocumentation(project, name, locationName)
      }
    } else {
      Some("No documentation because Hoogle (database) is not available")
    }
  }

  private def createDocumentation(project: Project, name: String, locationName: Option[String]): Option[String] = {
    def mkString(lines: Seq[String]) = {
      lines.mkString("\n").
        replace("<", HtmlElement.Lt).
        replace(">", HtmlElement.Gt).
        replace(" ", HtmlElement.Nbsp).
        replace("\n", HtmlElement.Break)
    }

    ProgressManager.checkCanceled()

    runHoogle(project, Seq("-i", name) ++ locationName.map("+" + _).toSeq).
      flatMap(processOutput =>
        if (processOutput.getStdoutLines.isEmpty || processOutput.getStdout.contains("No results found")) {
          None
        } else {
          val output = processOutput.getStdoutLines(false)
          val (definition, content) = output.asScala.splitAt(2)
          Some(
            DocumentationMarkup.DEFINITION_START +
              mkString(definition) +
              DocumentationMarkup.DEFINITION_END +
              DocumentationMarkup.CONTENT_START +
              HtmlElement.PreStart +
              mkString(content) +
              HtmlElement.PreEnd +
              DocumentationMarkup.CONTENT_END
          )
        }
      )
  }

  private def isHoogleFeatureAvailable(project: Project): Boolean = {
    if (!StackProjectManager.isHoogleAvailable(project)) {
      HaskellNotificationGroup.logInfoEvent(project, s"$HoogleName is not (yet) available")
      false
    } else {
      doesHoogleDatabaseExist(project)
    }
  }

  def rebuildHoogle(project: Project): Unit = {
    val buildHaddockOutput = try {
      StackProjectManager.setHaddockBuilding(project, state = true)
      StackCommandLine.executeStackCommandInMessageView(project, Seq("haddock", "--no-haddock-hyperlink-source"))
    } finally {
      StackProjectManager.setHaddockBuilding(project, state = false)
    }

    if (buildHaddockOutput.contains(true)) {
      GlobalProjectInfoComponent.findGlobalProjectInfo(project).map(_.localDocRoot) match {
        case Some(localDocRoot) => StackCommandLine.executeInMessageView(project, HooglePath, Seq("generate", s"--local=$localDocRoot", s"--database=${hoogleDbPath(project)}"))
        case None => HaskellNotificationGroup.logErrorBalloonEvent(project, "Can not generate Hoogle db because path to local doc root is unknown")
      }
    }
  }

  def doesHoogleDatabaseExist(project: Project): Boolean = {
    hoogleDbPath(project).exists()
  }

  def showHoogleDatabaseDoesNotExistNotification(project: Project): Unit = {
    HaskellNotificationGroup.logInfoBalloonEvent(project, "Hoogle database does not exist. Hoogle features can be optionally enabled by menu option `Haskell`/`(Re)Build Hoogle database`")
  }

  def versionInfo(project: Project): String = {
    if (StackProjectManager.isHoogleAvailable(project)) {
      CommandLine.run(project, HooglePath, Seq("--version")).getStdout
    } else {
      "-"
    }
  }

  import scala.concurrent.ExecutionContext.Implicits.global

  private def runHoogle(project: Project, arguments: Seq[String]): Option[ProcessOutput] = {
    ProgressManager.checkCanceled()

    ScalaFutureUtil.waitWithCheckCancelled(project, Future(CommandLine.run(project, HooglePath, Seq(s"--database=${hoogleDbPath(project)}") ++ arguments, logOutput = true)), "runHoogle")
  }

  private def hoogleDbPath(project: Project) = {
    new File(GlobalInfo.getIntelliJProjectDirectory(project), HoogleDbName)
  }
}
