package intellij.haskell.spellchecker

import intellij.haskell.HaskellLanguage
import com.intellij.psi.PsiElement
import com.intellij.spellchecker.tokenizer.{Tokenizer, SpellcheckingStrategy}

/**
 * Provide spellchecker support for Haskell sources.
 */
class HaskellSpellcheckingStrategy extends SpellcheckingStrategy {
  override def isMyContext(element: PsiElement): Boolean = HaskellLanguage.Instance.is(element.getLanguage)
}
