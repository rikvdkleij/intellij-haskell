package intellij.haskell.spellchecker

import com.intellij.spellchecker.BundledDictionaryProvider

/**
 * Provides a custom dictionary for the Haskell spellchecker.
 */
class HaskellBundledDictionaryProvider extends BundledDictionaryProvider {
  override def getBundledDictionaries: Array[String] = Array("/dictionary/haskell.dic")
}
