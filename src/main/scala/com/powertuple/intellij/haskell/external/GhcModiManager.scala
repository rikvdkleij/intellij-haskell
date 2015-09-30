/*
 * Copyright 2015 Rik van der Kleij
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

package com.powertuple.intellij.haskell.external

import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.project.Project

object GhcModiManager {

  private var restartState = false

  def setInRestartState(): Unit = synchronized {
    restartState = true
  }

  def doRestart(project: Project) = synchronized {
    val ghcModi = getGhcModiService(project)
    ghcModi.exit()
    ghcModi.startGhcModi()
  }

  def getGhcModi(project: Project) = synchronized {
    if (restartState) {
      doRestart(project)
      restartState = false
    }
    getGhcModiService(project)
  }

  private def getGhcModiService(project: Project) = {
    ServiceManager.getService(project, classOf[GhcModi])
  }
}
