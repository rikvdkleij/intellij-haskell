package intellij.haskell.alex.lang.psi

import com.intellij.openapi.util.TextRange
import com.intellij.psi.{InjectedLanguagePlaces, LanguageInjector, PsiLanguageInjectionHost}
import intellij.haskell.HaskellLanguage

class AlexHaskellInjector extends LanguageInjector {
  override def getLanguagesToInject(host: PsiLanguageInjectionHost, places: InjectedLanguagePlaces): Unit = {
    host match {
      case _: AlexTopModuleSection =>
        places.addPlace(HaskellLanguage.Instance, new TextRange(2, host.getTextLength - 1), null, null)
      case _: AlexUserCodeSection =>
        places.addPlace(HaskellLanguage.Instance, new TextRange(2, host.getTextLength - 1), null, null)
      case _ =>
    }
  }
}
