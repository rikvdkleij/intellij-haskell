package intellij.haskell.util

import java.util.concurrent.atomic.AtomicReference

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.util.ReadTask.Continuation
import com.intellij.openapi.progress.util.{ProgressIndicatorUtils, ReadTask}
import com.intellij.openapi.progress.{ProgressIndicator, ProgressManager}
import com.intellij.openapi.project.{DumbService, Project}

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


  def scheduleInReadActionWithWriteActionPriority[A](project: Project, f: => A): Either[String, A] = {
    val r = new AtomicReference[A]

    ProgressIndicatorUtils.scheduleWithWriteActionPriority {
      new ReadTask {

        override def runBackgroundProcess(indicator: ProgressIndicator): Continuation = {
          DumbService.getInstance(project).runReadActionInSmartMode(() => {
            performInReadAction(indicator)
          })
        }

        override def onCanceled(indicator: ProgressIndicator): Unit = {
          ProgressIndicatorUtils.scheduleWithWriteActionPriority(this)
        }

        override def computeInReadAction(indicator: ProgressIndicator): Unit = {
          r.set(f)
        }
      }
    }

    val deadline = Timeout.fromNow

    while (r.get == null && deadline.hasTimeLeft && !project.isDisposed) {
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

