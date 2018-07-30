package intellij.haskell.util

import java.util.concurrent.atomic.AtomicReference

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.util.ProgressIndicatorUtils
import com.intellij.openapi.project.Project

import scala.concurrent.duration._

object ApplicationUtil {

  private final val Timeout = 50.millis

  def runReadAction[T](f: => T): T = {
    ApplicationManager.getApplication.runReadAction(ScalaUtil.computable(f))
  }

  def runInReadActionWithWriteActionPriority[A](project: Project, f: => A): Either[String, A] = {
    val r = new AtomicReference[A]

    def run(): Boolean = {
      ProgressIndicatorUtils.runInReadActionWithWriteActionPriority {
        ScalaUtil.runnable {
          ProgressManager.checkCanceled()
          r.set(f)
        }
      }
    }

    val deadline = Timeout.fromNow

    while (deadline.hasTimeLeft && !run() && !project.isDisposed) {
      Thread.sleep(1)
    }

    val result = r.get()
    if (result == null) {
      Left("No result because of timeout")
    } else {
      Right(result)
    }
  }
}

