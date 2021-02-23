package intellij.haskell.runconfig.console

import java.util
import java.util.Comparator

import com.intellij.openapi.actionSystem.{ActionPromoter, AnAction, DataContext}
import com.intellij.openapi.editor.actions.EnterAction
import com.intellij.util.containers.ContainerUtil

class HaskellConsoleActionPromoter extends ActionPromoter {
  private val Comparator = new Comparator[AnAction]() {
    def compare(o1: AnAction, o2: AnAction): Int = {
      (notEnter(o1), notEnter(o2)) match {
        case (false, true) => 1
        case (true, false) => -1
        case _ => 0
      }
    }

    private def notEnter(o: AnAction) = !o.isInstanceOf[EnterAction]
  }

  def promote(actions: util.List[AnAction], context: DataContext): util.List[AnAction] = {
    val result = new util.ArrayList[AnAction](actions)
    ContainerUtil.sort(result, Comparator)
    result
  }
}
