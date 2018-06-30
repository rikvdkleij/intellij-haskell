package intellij.haskell.util

import com.intellij.openapi.application.ApplicationManager

object ApplicationUtil {

  def runReadAction[T](f: => T): T = {
    ApplicationManager.getApplication.runReadAction(ScalaUtil.computable(f))
  }
}

