package intellij.haskell.cabal.completion

import com.intellij.codeInsight.completion._
import com.intellij.codeInsight.lookup.{LookupElement, LookupElementBuilder}
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiElement
import com.intellij.util.ProcessingContext
import intellij.haskell.HaskellIcons
import intellij.haskell.cabal.CabalLanguage
import intellij.haskell.cabal.lang.psi.impl.ExtensionsImpl
import intellij.haskell.cabal.lang.psi.{BuildDepends, CabalPsiUtil, ExposedModules}
import intellij.haskell.external.component.HaskellComponentsManager
import intellij.haskell.external.component.HaskellComponentsManager.{getAvailableStackagePackages, getSupportedLanguageExtension}
import intellij.haskell.util.HaskellProjectUtil

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
      completeExtensions()
    }

    lazy val position: PsiElement = parameters.getPosition

    def completeExtensions(): Unit = {
      CabalPsiUtil.getFieldContext(position).foreach {
        case el: ExtensionsImpl => filterExtensions(el).foreach {
          result.addElement
        }
        case el: BuildDepends => filterPackageNames(el).foreach {
          result.addElement
        }
        case em: ExposedModules => filterExposedModuleNames(em).foreach {
          result.addElement
        }
        case _ => ()
      }
    }

    private def filterExtensions(el: ExtensionsImpl): Iterable[LookupElement] = {
      val currentExts = el.getValue.toSet
      val negExts = currentExts.map(v => if (v.startsWith("No")) v.substring(2) else "No" + v)
      // Skip already provided extensions or their negation.
      val skipExts = currentExts ++ negExts
      getSupportedLanguageExtension(position.getProject)
        .filter(!skipExts.contains(_))
        .map(n => LookupElementBuilder.create(n).withIcon(HaskellIcons.HaskellSmallLogo))
    }

    private def filterPackageNames(el: BuildDepends): Iterable[LookupElement] = {
      val skipPackageNames = el.getPackageNames.toSet
      getAvailableStackagePackages(position.getProject)
        .filter(!skipPackageNames.contains(_))
        .map(n => LookupElementBuilder.create(n).withIcon(HaskellIcons.HaskellSmallLogo))
    }

    // TODO Take into account stanza type, currently always project library module names are suggested.
    private def filterExposedModuleNames(em: ExposedModules): Iterable[LookupElement] = {
      val skipModuleNames = em.getModuleNames.toSet
      val module = HaskellProjectUtil.findModule(position)
      val moduleNames = module.map(HaskellComponentsManager.findAvailableModuleLibraryModuleNamesWithIndex).getOrElse(Iterable())
      moduleNames
        .filterNot(skipModuleNames.contains)
        .map(n => LookupElementBuilder.create(n).withIcon(HaskellIcons.HaskellSmallLogo))
    }
  }
}
