package intellij.haskell.cabal.lang.psi.impl

import java.util.regex.Pattern

import com.intellij.openapi.project.DumbService
import com.intellij.openapi.util.TextRange
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.search.{GlobalSearchScope, GlobalSearchScopesCore}
import com.intellij.psi.{PsiDirectory, PsiElement, PsiReference}
import intellij.haskell.cabal.lang.psi._
import intellij.haskell.psi.HaskellPsiUtil
import intellij.haskell.util.index.{HaskellFileIndex, HaskellModuleNameIndex}
import intellij.haskell.util.{HaskellProjectUtil, ScalaUtil}

trait ModulePartImpl extends CabalNamedElementImpl {

  override def getContext: Module = getParent match {
    case el: Module => el
    case other => throw new CabalElementTypeError("Module", other)
  }

  override def getName: String = getNode.getText

  override def setName(name: String): PsiElement = {
    replace(new ModulePart(new LeafPsiElement(CabalTypes.IDENT, name)))
  }

  override def getNameIdentifier: PsiElement = this

  override def getReference: PsiReference = {
    new CabalReference(this, TextRange.from(0, getName.length))
  }

  override def getVariants: Array[AnyRef] = {
    val parts = getParent.getChildren.init
    val numParts = parts.length
    val text = parts.map(_.getText).mkString(".") match {
      case s if s.isEmpty => ""
      case s => s + "."
    }
    DumbService.getInstance(getProject).tryRunReadActionInSmartMode(ScalaUtil.computable(HaskellFileIndex.findProjectHaskellFiles(getProject)), "Finding modules is not available until indices are ready").flatMap { file =>
      HaskellPsiUtil.findModuleDeclaration(file).flatMap(decl => Option(decl.getModid)).map(_.getText) match {
        case None => None
        case Some(name) if name.startsWith(text) =>
          DotRegex.split(name).take(numParts + 1).lastOption
        case _ => None
      }
    }.toArray[AnyRef]
  }

  override def resolve(): Option[PsiElement] = {
    val lastPart = getContext.getLastPart
    if (this != lastPart) resolveToModuleDir(lastPart) else resolveToModuleDecl()
  }

  private val DotRegex = Pattern.compile("\\.")

  private def resolveToModuleDir(lastPart: ModulePart): Option[PsiDirectory] = {
    lastPart.resolve().flatMap(lp => {
      // Find the part's position from the end so we can walk up the directory tree.
      val revPos = getParent.getChildren.reverse.indexOf(this)
      if (revPos == -1) throw new AssertionError(s"$getText not in parent (${getParent.getText})")
      // Iterate up the directory tree 'revPos' times.

      LazyList.iterate(
        lp.getContainingFile.getContainingDirectory, revPos
      )(_.getParent).lastOption.filter(
        // Ensure that the found directory name matches our element.
        dir => dir.getName == getText
      )
    })
  }

  private def resolveToModuleDecl(): Option[PsiElement] = {
    // If the module part IS the last part, resolve to its file's module decl.
    val moduleName = getParent.getText
    val scope = HaskellProjectUtil.findModule(this) match {
      case Some(m) => GlobalSearchScope.moduleScope(m)
      case None => GlobalSearchScopesCore.projectProductionScope(getProject)
    }
    val haskellFile = HaskellModuleNameIndex.findFilesByModuleName(getProject, moduleName).toOption.flatMap(_.headOption)
    haskellFile.flatMap(f => HaskellPsiUtil.findModuleDeclaration(f).find(_.getModuleName.contains(moduleName)).flatMap(_.getIdentifierElements.headOption))
  }
}
