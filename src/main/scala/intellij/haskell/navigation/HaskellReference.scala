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
import intellij.haskell.HaskellFile
import intellij.haskell.external.component._
import intellij.haskell.psi.HaskellPsiUtil._
import intellij.haskell.psi._
import intellij.haskell.util.{HaskellProjectUtil, LineColumnPosition}

class HaskellReference(element: HaskellNamedElement, textRange: TextRange) extends PsiReferenceBase[HaskellNamedElement](element, textRange) {

  override def resolve: PsiElement = {
    val project = myElement.getProject
    val psiFile = myElement.getContainingFile

    val result = myElement match {
      case mi: HaskellModid => HaskellComponentsManager.findHaskellFiles(project, mi.getName).headOption
      case qe: HaskellQualifierElement =>
        val importDeclarations = findImportDeclarations(psiFile)
        val resolveResults = findQualifier(importDeclarations, qe).orElse(
          findModuleFiles(importDeclarations, qe, project).headOption.map(_.getOriginalFile))
        if (resolveResults.isEmpty) {
          // return itself
          findNamedElement(myElement)
        } else {
          resolveResults
        }
      case ne: HaskellNamedElement if findImportHidingDeclarationParent(ne).isDefined => None
      case ne: HaskellNamedElement => HaskellReference.resolveReference(ne, psiFile, project)
      case _ => None
    }
    result.orNull
  }

  /** Implemented in [[intellij.haskell.editor.HaskellCompletionContributor]] **/
  override def getVariants: Array[AnyRef] = {
    Array()
  }

  private def findQualifier(importDeclarations: Iterable[HaskellImportDeclaration], qualifierElement: HaskellQualifierElement): Option[HaskellQualifier] = {
    importDeclarations.flatMap(id => Option(id.getImportQualifiedAs)).flatMap(iqa => Option(iqa.getQualifier)).find(_.getName == qualifierElement.getName)
  }

  private def findModuleFiles(importDeclarations: Iterable[HaskellImportDeclaration], qualifierElement: HaskellQualifierElement, project: Project): Iterable[HaskellFile] = {
    importDeclarations.flatMap(id => Option(id.getModid)).find(mi => mi.getName == qualifierElement.getName).map(mi => HaskellComponentsManager.findHaskellFiles(project, mi.getName)).getOrElse(Iterable())
  }

}

object HaskellReference {

  def resolveReference(namedElement: HaskellNamedElement, psiFile: PsiFile, project: Project): Option[HaskellNamedElement] = {
    HaskellProjectUtil.isLibraryFile(psiFile).flatMap(isLibraryFile => {
      if (isLibraryFile) {
        resolveReferenceByNameInfo(namedElement, psiFile, project)
      }
      else {
        resolveReferenceByDefinitionLocation(namedElement, project).orElse(if (HaskellPsiUtil.findExpressionParent(namedElement).isDefined) None else resolveReferenceByNameInfo(namedElement, psiFile, project))
      }
    })
  }

  def resolveInstanceReferences(namedElement: HaskellNamedElement, psiFile: PsiFile, project: Project): Iterable[HaskellNamedElement] = {
    HaskellComponentsManager.findNameInfo(namedElement).flatMap { ni =>
      findReferenceByNameInfo(ni, namedElement, project)
    }
  }

  def findNamedElementsByLibraryNameInfo(libraryNameInfo: LibraryNameInfo, name: String, project: Project): Iterable[HaskellNamedElement] = {
    HaskellComponentsManager.findHaskellFiles(project, libraryNameInfo.moduleName).flatMap { f =>
      val declarationElements = findHaskellDeclarationElements(f)
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
    HaskellComponentsManager.findHaskellFiles(project, moduleName).flatMap { hf =>
      HaskellPsiUtil.findHaskellDeclarationElements(hf).flatMap(_.getIdentifierElements).find(_.getName == name)
    }
  }

  private def resolveReferenceByNameInfo(namedElement: HaskellNamedElement, psiFile: PsiFile, project: Project) = {
    HaskellComponentsManager.findNameInfo(namedElement).headOption.flatMap(ni => findReferenceByNameInfo(ni, namedElement, project)).
      orElse(findHaskellDeclarationElements(psiFile).flatMap(_.getIdentifierElements).find(_.getName == namedElement.getName))
  }

  private def resolveReferenceByDefinitionLocation(namedElement: HaskellNamedElement, project: Project): Option[HaskellNamedElement] = {
    HaskellComponentsManager.findDefinitionLocation(namedElement).flatMap {
      case DefinitionLocationInfo(filePath, startLineNr, startColumnNr, _, _) => findNamedElementByLocation(filePath, startLineNr, startColumnNr, namedElement.getName, project)
      case ModuleLocationInfo(moduleName) => findNamedElementsInModule(moduleName, namedElement.getName, project).headOption
    }
  }

  def findNamedElementByLocation(filePath: String, lineNr: Integer, columnNr: Integer, name: String, project: Project): Option[HaskellNamedElement] = {
    for {
      haskellFile <- HaskellProjectUtil.findFile(filePath, project)
      offset <- LineColumnPosition.getOffset(haskellFile, LineColumnPosition(lineNr, columnNr))
      element <- Option(haskellFile.findElementAt(offset))
      namedElement <- findHighestDeclarationElementParent(element).flatMap(_.getIdentifierElements.find(_.getName == name)).
        orElse(findQualifiedNameElement(element).map(_.getIdentifierElement))
    } yield namedElement
  }

  private def findReferenceByNameInfo(nameInfo: NameInfo, namedElement: HaskellNamedElement, project: Project): Option[HaskellNamedElement] = {
    nameInfo match {
      case pni: ProjectNameInfo => findNamedElementByLocation(pni.filePath, pni.lineNr, pni.columnNr, namedElement.getName, project)
      case lni: LibraryNameInfo => findNamedElementsByLibraryNameInfo(lni, namedElement.getName, project).headOption
      case _ => None
    }
  }
}


