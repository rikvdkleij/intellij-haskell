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

import java.io.File
import java.nio.file.Paths
import java.util.regex.Pattern

import com.intellij.execution.process.ProcessOutput
import com.intellij.openapi.project.Project
import intellij.haskell.external.execution.{CommandLine, StackCommandLine}
import intellij.haskell.util.HaskellEditorUtil
import intellij.haskell.{GlobalInfo, HaskellNotificationGroup}

import scala.collection.JavaConverters._

object HoogleComponent {

  final val HoogleName = "hoogle"
  private final val HooglePath = GlobalInfo.toolPath(HoogleName).toString
  private final val HoogleDbName = "hoogle"

  def runHoogle(project: Project, pattern: String, count: Int = 100): Option[Seq[String]] = {
    if (isHoogleFeatureAvailable(project)) {
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

  def findDocumentation(project: Project, name: String, moduleName: String): Option[String] = {
    if (isHoogleFeatureAvailable(project)) {
      runHoogle(project, Seq(name, "-i", s"+$moduleName")).
        flatMap(processOutput =>
          if (processOutput.getStdoutLines.isEmpty || processOutput.getStdout.contains("No results found")) {
            None
          } else {
            // Remove excessive newlines that Hoogle outputs
            val cleanOutput = processOutput.getStdout.trim.replaceAll("\n{3,}", "\n\n")
            Some(s"${Pattern.compile("$", Pattern.MULTILINE).matcher(cleanOutput).replaceAll("<br>").replace(" ", "&nbsp;")}")
          }
        )
    } else {
      None
    }
  }

  private def isHoogleFeatureAvailable(project: Project): Boolean = {
    if (!StackProjectManager.isHoogleAvailable(project)) {
      HaskellEditorUtil.showStatusBarMessage(project, s"$HoogleName is not (yet) available")
      false
    } else {
      doesHoogleDatabaseExist(project)
    }
  }

  def rebuildHoogle(project: Project): Unit = {
    val buildHaddockOutput = StackCommandLine.executeInMessageView(project, Seq("haddock", "--test", "--bench", "--no-run-tests", "--no-run-benchmarks"))
    if (buildHaddockOutput.contains(true)) {
      StackCommandLine.executeInMessageView(project, Seq("exec", "--", HooglePath, "generate", "--local", s"--database=${hoogleDbPath(project)}"))
    }
  }

  def doesHoogleDatabaseExist(project: Project): Boolean = {
    new File(hoogleDbPath(project)).exists()
  }

  def showHoogleDatabaseDoesNotExistNotification(project: Project): Unit = {
    HaskellNotificationGroup.logInfoBalloonEvent(project, "Hoogle database does not exist. Hoogle features can be optionally enabled by menu option `Tools`/`Haskell`/`(Re)Build Hoogle database`")
  }

  def versionInfo(project: Project): String = {
    CommandLine.run(Some(project), project.getBasePath, HooglePath, Seq("--version")).getStdout
  }

  private def runHoogle(project: Project, arguments: Seq[String]): Option[ProcessOutput] = {
    StackCommandLine.run(project, Seq("exec", "--", HooglePath, s"--database=${hoogleDbPath(project)}") ++ arguments, logOutput = true)
  }

  private def hoogleDbPath(project: Project) = {
    Paths.get(project.getBasePath, GlobalInfo.StackWorkDirName, HoogleDbName).toString
  }
}
