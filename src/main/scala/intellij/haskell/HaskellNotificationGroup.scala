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

package intellij.haskell

import com.intellij.notification.NotificationGroup
import com.intellij.openapi.ui.MessageType._

object HaskellNotificationGroup {

  private val LogOnlyGroup = NotificationGroup.logOnlyGroup("Log only Haskell")
  private val BalloonGroup = NotificationGroup.balloonGroup("Balloon Haskell")

  def logError(message: String) {
    LogOnlyGroup.createNotification(message, ERROR).notify(null)
  }

  def logWarning(message: String) {
    LogOnlyGroup.createNotification(message, WARNING).notify(null)
  }

  def logInfo(message: String) {
    LogOnlyGroup.createNotification(message, INFO).notify(null)
  }

  def notifyBalloonError(message: String) {
    BalloonGroup.createNotification(message, ERROR).notify(null)
  }

  def notifyBalloonWarning(message: String) {
    BalloonGroup.createNotification(message, WARNING).notify(null)
  }

  def notifyBalloonInfo(message: String) {
    BalloonGroup.createNotification(message, INFO).notify(null)
  }
}
