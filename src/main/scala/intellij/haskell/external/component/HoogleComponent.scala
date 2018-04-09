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

import java.util.regex.Pattern

import com.intellij.openapi.project.Project
import intellij.haskell.HaskellNotificationGroup
import intellij.haskell.external.execution.StackCommandLine

import scala.collection.JavaConverters._
import scala.concurrent.duration._

object HoogleComponent {

  final val HoogleName = "hoogle"

  private val Timout = 10.seconds.toMillis

  def runHoogle(project: Project, pattern: String, count: Int = 100): Option[Seq[String]] = {
    if (isHoogleFeatureAvailable(project)) {
      StackCommandLine.run(project, Seq(HoogleName, "--", s""""$pattern"""", s"--count=$count"), timeoutInMillis = Timout).
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

  def findDocumentation(project: Project, name: String, moduleName: Option[String]): Option[String] = {
    if (isHoogleFeatureAvailable(project)) {
      StackCommandLine.run(project, Seq(HoogleName, "--", name) ++ moduleName.map(mn => Seq(s"+$mn", "-i")).getOrElse(Seq()), timeoutInMillis = Timout).
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
      HaskellNotificationGroup.logWarningBalloonEvent(project, s"$HoogleName is not yet available")
      false
    } else if (!doesHoogleDatabaseExist(project)) {
      showHoogleDatabaseDoesNotExistNotification(project)
      false
    } else {
      true
    }
  }

  def rebuildHoogle(project: Project): Option[Boolean] = {
    StackCommandLine.executeInMessageView(project, Seq(HoogleName, "--rebuild", "--test", "--no-run-tests"))
  }

  def doesHoogleDatabaseExist(project: Project): Boolean = {
    StackCommandLine.run(project, Seq(HoogleComponent.HoogleName, "--no-setup")) match {
      case Some(output) if output.getStderr.isEmpty => true
      case _ => false
    }
  }

  def showHoogleDatabaseDoesNotExistNotification(project: Project): Unit = {
    HaskellNotificationGroup.logWarningBalloonEvent(project, "Hoogle database does not exist. Hoogle database can be created by menu option `Tools`/`Haskell`/`(Re)Build Hoogle database`")
  }
}
