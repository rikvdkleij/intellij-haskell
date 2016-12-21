package intellij.haskell.cabal.lang.psi.impl

import java.util.regex.Pattern

import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.util.TextRange
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.{PsiDirectory, PsiElement, PsiReference}
import intellij.haskell.cabal.lang.psi._
import intellij.haskell.util.index.{HaskellFileIndex, HaskellModuleIndex}

import scala.collection.JavaConverters._

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
    HaskellFileIndex.findProjectProductionPsiFiles(getProject).flatMap { file =>
      file.getModuleName match {
        case None => None
        case Some(name) if name.startsWith(text) =>
          DOT_REGEX.split(name).take(numParts + 1).lastOption
        case _ => None
      }
    }.toArray[AnyRef]
  }

  override def resolve(): PsiElement = {
    val lastPart = getContext.getLastPart
    if (this != lastPart) resolveToModuleDir(lastPart) else resolveToModuleDecl()
  }

  private val DOT_REGEX = Pattern.compile("\\.")

  private def resolveToModuleDir(lastPart: ModulePart): PsiDirectory = {
    // Find the part's position from the end so we can walk up the directory tree.
    val revPos = getParent.getChildren.reverse.indexOf(this)
    if (revPos == -1) throw new AssertionError(s"$getText not in parent (${getParent.getText})")
    // Iterate up the directory tree 'revPos' times.

    Stream.iterate(
      lastPart.resolve().getContainingFile.getContainingDirectory, revPos
    )(_.getParent).lastOption.filter(
      // Ensure that the found directory name matches our element.
      dir => dir.getName == getText
    ).orNull
  }

  private def resolveToModuleDecl(): PsiElement = {
    // If the module part IS the last part, resolve to its file's module decl.
    val qualifiedName = getParent.getText
    val scope = Option(ModuleUtilCore.findModuleForPsiElement(this)) match {
      case Some(m) => GlobalSearchScope.moduleScope(m)
      case None => GlobalSearchScope.projectScope(getProject)
    }
    val files = HaskellModuleIndex.getFilesByModuleName(getProject, qualifiedName, scope)

    files.map { file =>
      // Get the last "part" of the module decl if the name matches.
      file.getModuleElement.filter(_.getText == qualifiedName).flatMap(
        qconid => Option(qconid.getLastChild)
      )
    }.collectFirst {
      // Stop at the first match.
      case Some(x) => x
    }.orElse {
      // If there are no matches, just guess at the first file found, if exists.
      files.headOption
    }.orNull
  }
}
