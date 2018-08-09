package intellij.haskell.util

import java.util.concurrent.atomic.AtomicReference

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.util.ReadTask.Continuation
import com.intellij.openapi.progress.util.{ProgressIndicatorUtils, ReadTask}
import com.intellij.openapi.progress.{ProgressIndicator, ProgressManager}
import com.intellij.openapi.project.{DumbService, Project}
import intellij.haskell.HaskellNotificationGroup

import scala.concurrent.duration._

object ApplicationUtil {

  trait ReadActionTimeout
  case object ReadActionTimeout extends ReadActionTimeout

  def runReadAction[T](f: => T): T = {
    ApplicationManager.getApplication.runReadAction(ScalaUtil.computable(f))
  }

  final val RunInTimeout = 50.millis

  def runInReadActionWithWriteActionPriority[A](project: Project, f: => A, timeoutMessage: => String, timeout: FiniteDuration = RunInTimeout): Either[ReadActionTimeout, A] = {
    val r = new AtomicReference[A]

    def run(): Boolean = {
      ProgressIndicatorUtils.runInReadActionWithWriteActionPriority {
        ScalaUtil.runnable {
          ProgressManager.checkCanceled()
          r.set(f)
        }
      }
    }

    val deadline = timeout.fromNow

    while (deadline.hasTimeLeft && !run() && !project.isDisposed) {
      Thread.sleep(1)
    }

    val result = r.get()
    if (result == null) {
      HaskellNotificationGroup.logInfoEvent(project, s"Timout in runInReadActionWithWriteActionPriority while $timeoutMessage")
      Left(ReadActionTimeout)
    } else {
      Right(result)
    }
  }

  private final val ScheduleInWriteTimeout = 100.millis

  def scheduleInReadActionWithWriteActionPriority[A](project: Project, f: => A, timeoutMessage: => String): Either[ReadActionTimeout, A] = {
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

    val deadline = ScheduleInWriteTimeout.fromNow

    while (r.get == null && deadline.hasTimeLeft && !project.isDisposed) {
      Thread.sleep(1)
    }

    val result = r.get()
    if (result == null) {
      HaskellNotificationGroup.logInfoEvent(project, s"Timout in scheduleInReadActionWithWriteActionPriority while $timeoutMessage")
      Left(ReadActionTimeout)
    } else {
      Right(result)
    }
  }
}

