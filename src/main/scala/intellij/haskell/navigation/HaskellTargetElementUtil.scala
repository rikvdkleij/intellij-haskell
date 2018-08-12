package intellij.haskell.navigation

import java.util

import com.intellij.codeInsight.TargetElementUtil
import com.intellij.psi.{PsiElement, PsiPolyVariantReference, PsiReference}
import intellij.haskell.external.component.NoInfoAvailable
import intellij.haskell.util.HaskellEditorUtil

import scala.collection.JavaConverters._
import scala.collection.mutable.ListBuffer

class HaskellTargetElementUtil extends TargetElementUtil {

  override def getTargetCandidates(reference: PsiReference): util.Collection[PsiElement] = {
    val project = reference.getElement.getProject
    val resolveResults = reference.asInstanceOf[PsiPolyVariantReference].multiResolve(false)
    val navigatableResults = ListBuffer[PsiElement]()

    for (r <- resolveResults) {
      r match {
        case NoResolveResult(noInfo) =>
          noInfo match {
            case NoInfoAvailable(_, _) => ()
            case ni => HaskellEditorUtil.showStatusBarBalloonMessage(project, s"Navigation is not available at this moment: ${ni.message}")
          }
        case _ =>
          val element = r.getElement
          if (isNavigatableSource(element)) navigatableResults.append(element)
      }
    }
    navigatableResults.asJava
  }
}
