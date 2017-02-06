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

package intellij.haskell.external.component

import java.util.concurrent.Future
import java.util.regex.Pattern

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import intellij.haskell.HaskellNotificationGroup
import intellij.haskell.external.commandLine.StackCommandLine

import scala.collection.JavaConverters._
import scala.concurrent.duration._

object HoogleComponent {

  private val Timout = 10.seconds.toMillis

  var hoogleAvailable = false

  def runHoogle(project: Project, pattern: String, count: Int = 100): Option[Seq[String]] = {
    if (hoogleAvailable) {
      StackCommandLine.runCommand(Seq("hoogle", "--", s""""$pattern"""", s"--count=$count"), project, timeoutInMillis = Timout).
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
      HaskellNotificationGroup.logWarningBalloonEvent(project, "Stack Hoogle is not yet available")
      None
    }
  }

  def findDocumentation(project: Project, name: String, moduleName: Option[String]): Option[String] = {
    if (hoogleAvailable) {
      StackCommandLine.runCommand(Seq("hoogle", "--", name) ++ moduleName.map(mn => Seq(s"+$mn", "-i")).getOrElse(Seq()), project, timeoutInMillis = Timout).
        flatMap(processOutput =>
          if (processOutput.getStdoutLines.isEmpty || processOutput.getStdout.contains("No results found")) {
            None
          } else {
            Option(processOutput.getStdout).map(o => s"${Pattern.compile("$", Pattern.MULTILINE).matcher(o).replaceAll("<br>").replace(" ", "&nbsp;")}")
          }
        )
    } else {
      HaskellNotificationGroup.logWarningBalloonEvent(project, "Stack Hoogle is not yet available")
      None
    }
  }

  def rebuildHoogle(project: Project): Future[_] = {
    hoogleAvailable = false
    ApplicationManager.getApplication.executeOnPooledThread(new Runnable {
      override def run(): Unit = {
        try {
          StackCommandLine.runCommand(Seq("hoogle", "--rebuild"), project, timeoutInMillis = 10.minutes.toMillis)
        } finally {
          hoogleAvailable = true
        }
      }
    })
  }
}
