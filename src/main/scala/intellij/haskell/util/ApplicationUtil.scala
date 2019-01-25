package intellij.haskell.util

import java.util.concurrent.atomic.AtomicReference

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.util.ReadTask.Continuation
import com.intellij.openapi.progress.util.{ProgressIndicatorUtils, ReadTask}
import com.intellij.openapi.progress.{ProgressIndicator, ProgressManager}
import com.intellij.openapi.project.{DumbService, Project}
import intellij.haskell.HaskellNotificationGroup
import intellij.haskell.external.component.{NoInfo, ReadActionTimeout}

import scala.concurrent.duration._

object ApplicationUtil {

  def timeout: Int = {
    if (ApplicationManager.getApplication.isDispatchThread) {
      10
    } else {
      60000
    }
  }

  private def isReadAccessAllowed = {
    ApplicationManager.getApplication.isReadAccessAllowed
  }

  def runReadAction[T](f: => T): T = {
    if (isReadAccessAllowed) {
      f
    } else {
      ApplicationManager.getApplication.runReadAction(ScalaUtil.computable(f))
    }
  }

  private final val RunInReadActionTimeout = 5.seconds

  def runInReadActionWithWriteActionPriority[A](project: Project, f: => A, readActionDescription: => String, timeout: FiniteDuration = RunInReadActionTimeout): Either[NoInfo, A] = {
    val r = new AtomicReference[A]

    if (isReadAccessAllowed) {
      Right(f)
    } else {
      def run(): Boolean = {
        ProgressIndicatorUtils.runInReadActionWithWriteActionPriority {
          ScalaUtil.runnable {
            ProgressManager.checkCanceled()
            r.set(f)
          }
        }
      }

      val deadline = timeout.fromNow

      while (!run() && deadline.hasTimeLeft && !project.isDisposed) {
        Thread.sleep(1)
      }

      val result = r.get()
      if (result == null) {
        HaskellNotificationGroup.logInfoEvent(project, s"Timeout ($timeout) in runInReadActionWithWriteActionPriority while $readActionDescription")
        Left(ReadActionTimeout(readActionDescription))
      } else {
        Right(result)
      }
    }
  }

  private final val ScheduleInReadActionTimeout = 60.seconds

  def scheduleInReadActionWithWriteActionPriority[A](project: Project, f: => A, scheduleInReadActionDescription: => String, timeout: FiniteDuration = ScheduleInReadActionTimeout, reschedule: Boolean = true): Either[NoInfo, A] = {
    val r = new AtomicReference[A]
    var cancelled = false

    ProgressIndicatorUtils.scheduleWithWriteActionPriority {
      new ReadTask {

        override def runBackgroundProcess(indicator: ProgressIndicator): Continuation = {
          DumbService.getInstance(project).runReadActionInSmartMode(() => {
            performInReadAction(indicator)
          })
        }

        override def onCanceled(indicator: ProgressIndicator): Unit = {
          cancelled = true
          // When user is typing it does not make sense to reschedule the action because it makes the UI unresponsive
          if (reschedule) {
            HaskellNotificationGroup.logInfoEvent(project, s"scheduleInReadActionWithWriteActionPriority while $scheduleInReadActionDescription" + " is cancelled!! Retrying")
            ProgressIndicatorUtils.scheduleWithWriteActionPriority(this)
          }
        }

        override def computeInReadAction(indicator: ProgressIndicator): Unit = {
          r.set(f)
        }
      }
    }

    val deadline = timeout.fromNow

    if (reschedule) {
      while (r.get == null && deadline.hasTimeLeft && !project.isDisposed) {
        Thread.sleep(1)
      }
    } else {
      while (r.get == null && !cancelled && deadline.hasTimeLeft && !project.isDisposed) {
        Thread.sleep(1)
      }
    }

    val result = r.get()
    if (result == null) {
      HaskellNotificationGroup.logInfoEvent(project, s"Timeout ($timeout) in scheduleInReadActionWithWriteActionPriority while $scheduleInReadActionDescription and reschedule $reschedule")
      Left(ReadActionTimeout(scheduleInReadActionDescription))
    } else {
      Right(result)
    }
  }
}

