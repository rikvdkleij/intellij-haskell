package intellij.haskell.cabal.completion

import com.intellij.codeInsight.completion._
import com.intellij.codeInsight.lookup.{LookupElement, LookupElementBuilder}
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiElement
import com.intellij.util.ProcessingContext
import intellij.haskell.cabal.CabalLanguage
import intellij.haskell.cabal.lang.psi
import intellij.haskell.cabal.lang.psi.CabalPsiUtil
import intellij.haskell.external.component.HaskellComponentsManager.findGlobalProjectInfo
import intellij.haskell.{HaskellIcons, HaskellNotificationGroup}

final class CabalCompletionContributor extends CompletionContributor {

  // Extends this class with completion for Cabal files.
  extend(
    CompletionType.BASIC,
    PlatformPatterns.psiElement().withLanguage(CabalLanguage.Instance),
    newProvider()
  )

  private def newProvider() = new CompletionProvider[CompletionParameters]() {
    override def addCompletions(parameters: CompletionParameters,
                                context: ProcessingContext,
                                result: CompletionResultSet): Unit
    = new CompletionWrapper(parameters, context, result).run()
  }

  private class CompletionWrapper(parameters: CompletionParameters,
                                  context: ProcessingContext,
                                  result: CompletionResultSet) {
    def run(): Unit = {
      if (completeExtensions()) return
    }

    lazy val position: PsiElement = parameters.getPosition

    def completeExtensions(): Boolean = {
      val a = CabalPsiUtil.getFieldContext(position).collect {
        case el: psi.impl.ExtensionsImpl => filterExtensions(el).foreach {
          result.addElement
        }
      }.isDefined
      HaskellNotificationGroup.logWarningEvent(position.getProject, s"predicate -> $a !!!!!!!!!!!")
      a
    }

    /** Skip already provided extensions or their negation. */
    private def filterExtensions(el: psi.impl.ExtensionsImpl): Iterable[LookupElement] = {
      val project = position.getProject
      val currentExts = el.getValue.toSet
      val negExts = currentExts.map(v => if (v.startsWith("No")) v.substring(2) else "No" + v)
      val skipExts = currentExts ++ negExts
      findGlobalProjectInfo(el.getContainingFile.getProject)
        .map(_.languageExtensions
          .filter(!skipExts.contains(_))
          .map(n => LookupElementBuilder.create(n).withIcon(HaskellIcons.HaskellSmallLogo)))
        .getOrElse(Iterable())
    }
  }

}
