package intellij.haskell.cabal.completion

import com.intellij.codeInsight.completion._
import com.intellij.codeInsight.lookup.{LookupElement, LookupElementBuilder}
import com.intellij.openapi.project.Project
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiElement
import com.intellij.util.ProcessingContext
import icons.HaskellIcons
import intellij.haskell.cabal.CabalLanguage
import intellij.haskell.cabal.lang.psi.impl.ExtensionsImpl
import intellij.haskell.cabal.lang.psi.{BuildDepends, CabalPsiUtil, ExposedModules}
import intellij.haskell.external.component.HaskellComponentsManager
import intellij.haskell.external.component.HaskellComponentsManager.{getAvailableStackagePackages, getSupportedLanguageExtension}
import intellij.haskell.util.HaskellProjectUtil

import scala.collection.JavaConverters._

final class CabalCompletionContributor extends CompletionContributor {

  private val provider: CompletionProvider[CompletionParameters] = new CompletionProvider[CompletionParameters] {

    def addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
      val project = parameters.getEditor.getProject
      val position = parameters.getPosition

      CabalPsiUtil.getFieldContext(position).foreach {
        case ei: ExtensionsImpl => result.addAllElements(filterExtensions(ei, project).asJavaCollection)
        case bd: BuildDepends => result.addAllElements(filterPackageNames(bd, project).asJavaCollection)
        case em: ExposedModules => result.addAllElements(filterExposedModuleNames(em, position).asJavaCollection)
        case _ => ()
      }
    }

    private def filterExtensions(el: ExtensionsImpl, project: Project): Iterable[LookupElement] = {
      val currentExts = el.getValue.toSet
      val negExts = currentExts.map(v => if (v.startsWith("No")) v.substring(2) else "No" + v)
      // Skip already provided extensions or their negation.
      val skipExts = currentExts ++ negExts
      getSupportedLanguageExtension(project)
        .filter(!skipExts.contains(_))
        .map(n => LookupElementBuilder.create(n).withIcon(HaskellIcons.HaskellSmallBlueLogo))
    }

    private def filterPackageNames(el: BuildDepends, project: Project): Seq[LookupElement] = {
      val skipPackageNames = el.getPackageNames.toSet
      getAvailableStackagePackages(project)
        .filter(!skipPackageNames.contains(_))
        .map(n => LookupElementBuilder.create(n).withIcon(HaskellIcons.HaskellSmallBlueLogo)).toSeq
    }

    // TODO Take into account stanza type, currently always project library module names are suggested.
    private def filterExposedModuleNames(em: ExposedModules, position: PsiElement): Iterable[LookupElement] = {
      val skipModuleNames = em.getModuleNames.toSet
      val module = HaskellProjectUtil.findModule(position)
      val moduleNames = module.map(HaskellComponentsManager.findAvailableModuleLibraryModuleNamesWithIndex).getOrElse(Iterable())
      moduleNames
        .filterNot(skipModuleNames.contains)
        .map(n => LookupElementBuilder.create(n).withIcon(HaskellIcons.HaskellSmallBlueLogo))
    }
  }

  // Extends this class with completion for Cabal files.
  extend(CompletionType.BASIC, PlatformPatterns.psiElement().withLanguage(CabalLanguage.Instance), provider)

}
