/*
 * Copyright 2016 Rik van der Kleij
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package intellij.haskell.navigation

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi._
import intellij.haskell.external.component._
import intellij.haskell.psi.HaskellPsiUtil._
import intellij.haskell.psi._
import intellij.haskell.util.{HaskellProjectUtil, LineColumnPosition}

import scala.Option.option2Iterable

class HaskellReference(element: HaskellNamedElement, textRange: TextRange) extends PsiPolyVariantReferenceBase[HaskellNamedElement](element, textRange) {

  override def resolve: PsiElement = {
    val resolveResults: Array[ResolveResult] = multiResolve(false)
    resolveResults.headOption.map(_.getElement).orNull
  }

  override def multiResolve(incompleteCode: Boolean): Array[ResolveResult] = {
    val project = myElement.getProject
    val psiFile = myElement.getContainingFile

    val result = myElement match {
      case mi: HaskellModid =>
        (for {
          haskellFile <- HaskellComponentsManager.findHaskellFiles(project, mi.getName)
        } yield new HaskellFileResolveResult(haskellFile)).toArray
      case qe: HaskellQualifierElement =>
        val importDeclarations = findImportDeclarations(psiFile)
        val resolveResults = findQualifier(importDeclarations, qe).map(q => Iterable(new HaskellQualifierResolveResult(q))).getOrElse(
          findModuleFiles(importDeclarations, qe, project).map(f => new HaskellFileResolveResult(f.getOriginalElement)))
        if (resolveResults.isEmpty) {
          // return itself
          findNamedElement(myElement).map(new HaskellProjectResolveResult(_)).toArray
        } else {
          resolveResults.toArray
        }
      case ne: HaskellNamedElement if findImportDeclarationParent(ne).isDefined =>
        val importModuleName = findImportDeclarationParent(ne).flatMap(_.getModuleName)
        importModuleName.map(mn => createResolveResultsByNameInfos(ne, project).
          find(rr => findModuleName(rr.getElement.getContainingFile.getOriginalFile).contains(mn))).map(_.toArray).getOrElse(Array[ResolveResult]())
      case ne: HaskellNamedElement if findTopDeclarationParent(ne).isDefined | findModuleDeclarationParent(ne).isDefined =>
        findResolveResults(ne, psiFile, project).toArray.distinct
      case _ => Array[ResolveResult]()
    }
    result.asInstanceOf[Array[ResolveResult]]
  }

  override def isReferenceTo(element: PsiElement): Boolean = {
    val psiManager = getElement.getManager
    val resolveResults = multiResolve(false)
    resolveResults.exists(rr => psiManager.areElementsEquivalent(rr.getElement, element))
  }

  /** Implemented in [[intellij.haskell.editor.HaskellCompletionContributor]] **/
  override def getVariants: Array[AnyRef] = {
    Array()
  }

  private def findQualifier(importDeclarations: Iterable[HaskellImportDeclaration], qualifierElement: HaskellQualifierElement) = {
    importDeclarations.flatMap(id => Option(id.getImportQualifiedAs)).flatMap(iqa => Option(iqa.getQualifier)).find(_.getName == qualifierElement.getName)
  }

  private def findModuleFiles(importDeclarations: Iterable[HaskellImportDeclaration], qualifierElement: HaskellQualifierElement, project: Project) = {
    importDeclarations.flatMap(id => Option(id.getModid)).find(mi => mi.getName == qualifierElement.getName).map(mi => HaskellComponentsManager.findHaskellFiles(project, mi.getName)).getOrElse(Iterable())
  }

  private def findResolveResults(namedElement: HaskellNamedElement, psiFile: PsiFile, project: Project) = {
    if (HaskellProjectUtil.isLibraryFile(psiFile)) {
      createResolveResultsByNameInfos(namedElement, project)
    } else {
      val definitionLocationResolveResult = findReferenceByDefinitionLocation(namedElement, project).map(prr => Iterable(new HaskellProjectResolveResult(prr)))
      definitionLocationResolveResult.getOrElse(createResolveResultsByNameInfos(namedElement, project))
    }
  }

  private def createResolveResultsByNameInfos(namedElement: HaskellNamedElement, project: Project): Iterable[ResolveResult] = {
    HaskellComponentsManager.findNameInfo(namedElement).flatMap {
      case pni: ProjectNameInfo => findReferenceByProjectNameInfo(pni, namedElement, project).map(new HaskellProjectResolveResult(_)).toIterable
      case lni: LibraryNameInfo => HaskellReference.findNamedElementsByLibraryNameInfo(lni, namedElement.getName, project).map(ne => new HaskellLibraryResolveResult(ne))
      case bini: BuiltInNameInfo => Some(new BuiltInResolveResult(bini.declaration, bini.libraryName, bini.moduleName))
    }
  }

  private def findReferenceByDefinitionLocation(namedElement: HaskellNamedElement, project: Project): Option[HaskellNamedElement] = {
    HaskellComponentsManager.findDefinitionLocation(namedElement).flatMap(location => {
      findReferenceByLocation(location.filePath, location.startLineNr, location.startColumnNr, namedElement, project)
    })
  }

  private def findReferenceByProjectNameInfo(projectNameInfo: ProjectNameInfo, namedElement: HaskellNamedElement, project: Project): Option[HaskellNamedElement] = {
    findReferenceByLocation(projectNameInfo.filePath, projectNameInfo.lineNr, projectNameInfo.columnNr, namedElement, project)
  }

  private def findReferenceByLocation(filePath: String, lineNr: Integer, columnNr: Integer, namedElement: HaskellNamedElement, project: Project): Option[HaskellNamedElement] = {
    HaskellReference.findReferenceByLocation(filePath, lineNr, columnNr, namedElement.getName, project)
  }

}

object HaskellReference {

  def findNamedElementsByLibraryNameInfo(libraryNameInfo: LibraryNameInfo, name: String, project: Project): Iterable[HaskellNamedElement] = {
    HaskellComponentsManager.findHaskellFiles(project, libraryNameInfo.moduleName).flatMap { f =>
      val declarationElements = findDeclarationElements(f)
      val namedElementsByNameInfo = declarationElements.flatMap(_.getIdentifierElements).
        filter(_.getName == name).
        filter(ne => HaskellComponentsManager.findNameInfo(ne).exists(ni => ni.shortenedDeclaration == libraryNameInfo.shortenedDeclaration))

      if (namedElementsByNameInfo.isEmpty) {
        val namedElementsByDeclaration = declarationElements.filter(de => de.getIdentifierElements.forall(e => libraryNameInfo.shortenedDeclaration.contains(e.getName))).flatMap(_.getIdentifierElements).filter(_.getName == name)
        if (namedElementsByDeclaration.isEmpty) {
          declarationElements.flatMap(_.getIdentifierElements).filter(_.getName == name)
        } else {
          namedElementsByDeclaration
        }
      } else {
        namedElementsByNameInfo
      }
    }
  }

  def findNamedElementsInModule(moduleName: String, name: String, project: Project): Iterable[HaskellNamedElement] = {
    HaskellComponentsManager.findHaskellFiles(project, moduleName).flatMap { f =>
      HaskellPsiUtil.findDeclarationElements(f).flatMap(_.getIdentifierElements).filter(_.getName == name)
    }
  }

  def findReferenceByLocation(filePath: String, lineNr: Integer, columnNr: Integer, name: String, project: Project): Option[HaskellNamedElement] = {
    for {
      haskellFile <- HaskellProjectUtil.findFile(filePath, project)
      offset <- LineColumnPosition.getOffset(haskellFile, LineColumnPosition(lineNr, columnNr))
      element <- Option(haskellFile.findElementAt(offset))
      namedElement <- findDeclarationElementParent(element).flatMap(_.getIdentifierElements.find(_.getName == name)).
        orElse(findQualifiedNameElement(element).map(_.getIdentifierElement))
    } yield namedElement
  }
}

class HaskellProjectResolveResult(val element: HaskellNamedElement) extends PsiElementResolveResult(element)

class HaskellLibraryResolveResult(val element: HaskellNamedElement) extends PsiElementResolveResult(element)

class HaskellFileResolveResult(val element: PsiElement) extends PsiElementResolveResult(element)

class HaskellQualifierResolveResult(val element: HaskellNamedElement) extends PsiElementResolveResult(element)

private class BuiltInResolveResult(declaration: String, val libraryName: String, val moduleName: String) extends NoElementToReferResolveResult(declaration)

private sealed abstract class NoElementToReferResolveResult(val declaration: String) extends ResolveResult {
  override def getElement: PsiElement = null

  override def isValidResult: Boolean = false
}

