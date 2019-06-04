package intellij.haskell.util

import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.util.WaitFor
import intellij.haskell.HaskellNotificationGroup

import scala.concurrent.duration._
import scala.concurrent.{Await, Future, TimeoutException}

object ScalaFutureUtil {

  def waitForValue[T](project: Project, future: Future[T], actionDescription: String, timeout: FiniteDuration = 5.seconds): Option[T] = {
    try {
      Option(Await.result(future, timeout))
    } catch {
      case _: TimeoutException =>
        HaskellNotificationGroup.logInfoEvent(project, s"Timeout while $actionDescription")
        None
    }
  }

  def waitWithCheckCancelled[T](project: Project, future: Future[T], actionDescription: String, timeout: FiniteDuration = 5.seconds): Option[T] = {
    try {
      new WaitFor(timeout.toMillis.toInt, 1) {
        override def condition(): Boolean = {
          ProgressManager.checkCanceled()
          future.isCompleted || project.isDisposed
        }
      }

      if (project.isDisposed) {
        None
      } else {
        Option(Await.result(future, 1.milli))
      }
    } catch {
      case _: TimeoutException =>
        HaskellNotificationGroup.logInfoEvent(project, s"Timeout in waitWithCheckCancelled while $actionDescription")
        None
    }
  }

}
