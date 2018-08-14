package intellij.haskell.psi.impl

import com.intellij.lang.java.lexer.JavaLexer
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.LanguageLevelProjectExtension
import com.intellij.pom.java.LanguageLevel
import com.intellij.psi.PsiNameHelper

object HaskellPsiNameHelper {
  def getInstance: PsiNameHelper = new HaskellPsiNameHelper {
    override protected def getLanguageLevel: LanguageLevel = return LanguageLevel.HIGHEST
  }
}

class HaskellPsiNameHelper private() extends PsiNameHelper {
  final private var myLanguageLevelExtension: LanguageLevelProjectExtension = null

  def this(project: Project) {
    this()
    myLanguageLevelExtension = LanguageLevelProjectExtension.getInstance(project)
  }

  override def isIdentifier(text: String): Boolean = isIdentifier(text, getLanguageLevel)

  protected def getLanguageLevel: LanguageLevel = myLanguageLevelExtension.getLanguageLevel

  override def isIdentifier(text: String, languageLevel: LanguageLevel): Boolean = text != null

  override def isKeyword(text: String): Boolean = text != null && JavaLexer.isKeyword(text, getLanguageLevel)

  override def isQualifiedName(text: String): Boolean = {
    if (text == null) return false

    if (text.contains(".") && text.size > 2) {
      true
    } else {
      false
    }
  }
}
