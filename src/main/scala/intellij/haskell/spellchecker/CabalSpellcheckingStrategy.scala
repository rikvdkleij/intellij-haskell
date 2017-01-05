package intellij.haskell.spellchecker

import com.intellij.psi.PsiElement
import com.intellij.spellchecker.tokenizer.{SpellcheckingStrategy, Tokenizer}
import intellij.haskell.cabal.CabalLanguage

/**
  * Provide spellchecker support for Cabal sources.
  */
class CabalSpellcheckingStrategy extends SpellcheckingStrategy {
  override def isMyContext(element: PsiElement): Boolean = CabalLanguage.Instance.is(element.getLanguage)
}
