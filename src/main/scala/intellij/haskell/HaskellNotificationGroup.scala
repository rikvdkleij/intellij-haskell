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

import com.intellij.notification.{Notification, NotificationDisplayType, NotificationGroup}
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.MessageType
import com.intellij.openapi.ui.MessageType._

object HaskellNotificationGroup {

  private val LogOnlyGroup = new NotificationGroup("Haskell Log", NotificationDisplayType.NONE, false)
  private val BalloonGroup = new NotificationGroup("Haskell Balloon", NotificationDisplayType.BALLOON, false)

  def logErrorEvent(project: Option[Project], message: String) {
    logEvent(project, message, ERROR, LogOnlyGroup.createNotification)
  }

  def logErrorEvent(project: Project, message: String) {
    logEvent(Option(project), message, ERROR, LogOnlyGroup.createNotification)
  }

  def logErrorEvent(message: String) {
    logEvent(None, message, ERROR, LogOnlyGroup.createNotification)
  }

  def logWarningEvent(project: Project, message: String) {
    logEvent(Option(project), message, WARNING, LogOnlyGroup.createNotification)
  }

  def logWarningEvent(message: String) {
    logEvent(None, message, WARNING, LogOnlyGroup.createNotification)
  }

  def logInfoEvent(project: Option[Project], message: String) {
    logEvent(project, message, INFO, LogOnlyGroup.createNotification)
  }

  def logInfoEvent(project: Project, message: String) {
    logEvent(Option(project), message, INFO, LogOnlyGroup.createNotification)
  }

  def logInfoEvent(message: String) {
    logEvent(None, message, INFO, LogOnlyGroup.createNotification)
  }

  def logErrorBalloonEvent(project: Option[Project], message: String) {
    balloonEvent(project, message, ERROR)
  }

  def logErrorBalloonEvent(project: Project, message: String) {
    balloonEvent(Option(project), message, ERROR)
  }

  def logErrorBalloonEvent(message: String) {
    balloonEvent(None, message, ERROR)
  }

  def logWarningBalloonEvent(project: Option[Project], message: String) {
    balloonEvent(project, message, WARNING)
  }

  def logWarningBalloonEvent(project: Project, message: String) {
    balloonEvent(Option(project), message, WARNING)
  }

  def logWarningBalloonEvent(message: String) {
    balloonEvent(None, message, WARNING)
  }

  def logInfoBalloonEvent(project: Project, message: String) {
    balloonEvent(Option(project), message, INFO)
  }

  def logInfoBalloonEvent(message: String) {
    balloonEvent(None, message, INFO)
  }

  private def logEvent(project: Option[Project], message: String, messageType: MessageType, notification: (String, MessageType) => Notification) = {
    log(project, message, messageType, LogOnlyGroup.createNotification)
  }

  private def balloonEvent(project: Option[Project], message: String, messageType: MessageType) = {
    log(project, message, messageType, BalloonGroup.createNotification)
  }

  private def log(project: Option[Project], message: String, messageType: MessageType, notification: (String, MessageType) => Notification) = {
    project match {
      case Some(p) if !p.isDisposed && p.isInitialized => notification(message, messageType).notify(p)
      case None => notification(message, messageType).notify(null)
      case _ => ()
    }
  }
}
