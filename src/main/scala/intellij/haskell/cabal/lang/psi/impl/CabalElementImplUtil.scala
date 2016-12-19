package intellij.haskell.cabal.lang.psi.impl

import scala.reflect.ClassTag

import com.intellij.psi.PsiElement

object CabalElementImplUtil {

  def assertUpCast[A <: PsiElement : ClassTag](el: PsiElement): A = {
    val ct = implicitly[ClassTag[A]]
    el match {
      case ct(x) => x
      case other =>
        throw new AssertionError(
          s"Expected ${ct.runtimeClass.getName} but got: " +
          s"${other.getClass.getName} (${other.getText})"
        )
    }
  }

}
