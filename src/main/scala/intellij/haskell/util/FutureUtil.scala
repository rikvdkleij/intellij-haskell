package intellij.haskell.util

import java.util.concurrent.{Future, TimeUnit, TimeoutException}

import com.intellij.openapi.project.Project
import intellij.haskell.HaskellNotificationGroup

object FutureUtil {

  def getValue[T](future: Future[T], project: Project, timeOutMessage: String): Option[T] = {
    try {
      Option(future.get(1, TimeUnit.SECONDS))
    } catch {
      case _: TimeoutException =>
        HaskellNotificationGroup.logErrorEvent(project, s"Timeout while $timeOutMessage")
        None
    }
  }
}
