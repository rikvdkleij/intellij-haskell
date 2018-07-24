package intellij.haskell.util

import java.util.concurrent.atomic.AtomicReference

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.util.ProgressIndicatorUtils
import com.intellij.openapi.project.Project

object ApplicationUtil {

  def runReadAction[T](f: => T): T = {
    ApplicationManager.getApplication.runReadAction(ScalaUtil.computable(f))
  }

  def runReadActionWithWriteActionPriority[A](project: Project, f: => A): Option[A] = {
    val r = new AtomicReference[A]

    def run(): Boolean = {
      ProgressIndicatorUtils.runInReadActionWithWriteActionPriority {
        ScalaUtil.runnable(r.set(f))
      }
    }

    while (!run() && !project.isDisposed) {
      Thread.sleep(100)
    }
    Option(r.get())
  }
}

