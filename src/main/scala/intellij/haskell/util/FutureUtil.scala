package intellij.haskell.util

import java.util.concurrent.{Future, TimeUnit, TimeoutException}

import com.intellij.openapi.project.Project
import intellij.haskell.HaskellNotificationGroup

object FutureUtil {

  def waitForValue[T](project: Project, future: Future[T], actionDescription: String, timeoutInSeconds: Int = 5): Option[T] = {
    try {
      Option(future.get(timeoutInSeconds, TimeUnit.SECONDS))
    } catch {
      case _: TimeoutException =>
        HaskellNotificationGroup.logInfoEvent(project, s"Timeout while $actionDescription")
        None
    }
  }

}
