package intellij.haskell.spellchecker

import intellij.haskell.HaskellLanguage
import com.intellij.psi.PsiElement
import com.intellij.spellchecker.tokenizer.{Tokenizer, SpellcheckingStrategy}

/**
 * Provide spellchecker support for Haskell sources.
 */
class HaskellSpellcheckingStrategy extends SpellcheckingStrategy {
  // Use TEXT_TOKENIZER so prime' names won't be marked as a typo.
  override def getTokenizer(element: PsiElement): Tokenizer[_ <: PsiElement] = {
    SpellcheckingStrategy.TEXT_TOKENIZER
  }

  override def isMyContext(element: PsiElement): Boolean = HaskellLanguage.Instance.is(element.getLanguage)
}
