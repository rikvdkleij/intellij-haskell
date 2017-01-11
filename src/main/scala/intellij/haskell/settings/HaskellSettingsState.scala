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

package intellij.haskell.settings

import com.intellij.openapi.project.Project
import intellij.haskell.HaskellNotificationGroup

object HaskellSettingsState {
  private def state = HaskellSettingsPersistentStateComponent.getInstance().getState

  def getHindentPath(project: Project): Option[String] = {
    val path = findPath(state.hindentPath)
    notifyIfPathIsNotSet(project, path, HaskellConfigurable.Hindent)
    path
  }

  def getStylishHaskellPath(project: Project): Option[String] = {
    val path = findPath(state.stylishHaskellPath)
    notifyIfPathIsNotSet(project, path, HaskellConfigurable.StylishHaskell)
    path
  }

  private def notifyIfPathIsNotSet(project: Project, path: Option[String], name: String) {
    if (path.isEmpty) {
      HaskellNotificationGroup.logErrorBalloonEvent(project, s"Path to <b>$name</b> is not set. Please do in <b>Settings</b>/<b>Other Settings</b>/<b>Haskell</b>.")
    }
  }

  private def findPath(path: String): Option[String] = {
    Option(path).filterNot(_.trim.isEmpty)
  }
}
