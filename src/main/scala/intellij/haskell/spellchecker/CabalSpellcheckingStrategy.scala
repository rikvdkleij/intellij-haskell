package intellij.haskell.spellchecker

import com.intellij.psi.PsiElement
import com.intellij.spellchecker.tokenizer.{SpellcheckingStrategy, Tokenizer}
import intellij.haskell.cabal.CabalLanguage

/**
  * Provide spellchecker support for Cabal sources.
  */
class CabalSpellcheckingStrategy extends SpellcheckingStrategy {
  // Use TEXT_TOKENIZER so prime' names won't be marked as a typo.
  override def getTokenizer(element: PsiElement): Tokenizer[_ <: PsiElement] = {
    SpellcheckingStrategy.TEXT_TOKENIZER
  }

  override def isMyContext(element: PsiElement): Boolean = CabalLanguage.Instance.is(element.getLanguage)
}
