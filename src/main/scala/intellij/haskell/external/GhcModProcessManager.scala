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

import com.intellij.openapi.project.Project

object GhcModProcessManager {

  private var restartState = false

  def setInRestartState() {
    synchronized {
      restartState = true
    }
  }

  def doRestart(project: Project) {
    synchronized {
      val ghcModProcesses = Seq(getGhcModCheckComponent(project), getGhcModCheckComponent(project))
      ghcModProcesses.foreach { p =>
        p.exit()
        p.start()
      }
    }
  }

  def getGhcModCheckProcess(project: Project) = {
    synchronized {
      if (restartState) {
        doRestart(project)
        restartState = false
      }
      getGhcModCheckComponent(project)
    }
  }

  def getGhcModInfoProcess(project: Project) = {
    synchronized {
      if (restartState) {
        doRestart(project)
        restartState = false
      }
      getGhcModInfoComponent(project)
    }
  }

  private def getGhcModCheckComponent(project: Project) = {
    project.getComponent(classOf[GhcModCheckProcess])
  }

  private def getGhcModInfoComponent(project: Project) = {
    project.getComponent(classOf[GhcModInfoProcess])
  }
}
