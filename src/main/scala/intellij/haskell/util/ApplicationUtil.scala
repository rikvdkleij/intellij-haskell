package intellij.haskell.util

import java.util.concurrent.atomic.AtomicReference

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.util.ProgressIndicatorUtils
import com.intellij.openapi.project.Project

object ApplicationUtil {

  def runReadAction[T](f: => T): T = {
    ApplicationManager.getApplication.runReadAction(ScalaUtil.computable(f))
  }

  def runReadActionWithWriteActionPriority[A](project: Project, f: => A): A = {
    val r = new AtomicReference[A]

    def run() = {
      ProgressIndicatorUtils.runInReadActionWithWriteActionPriority {
        ProgressManager.checkCanceled()
        ScalaUtil.runnable(r.set(f))
      }
    }

    while (r.get() == null && !project.isDisposed) {
      run()
    }
    r.get()
  }
}

