package intellij.haskell.util

import com.intellij.openapi.application.ApplicationManager

object ApplicationUtil {

  def runReadAction[T](f: => T, runInRead: Boolean = false): T = {
    if (runInRead) {
      ApplicationManager.getApplication.runReadAction(ScalaUtil.computable(f))
    } else {
      f
    }
  }
}

