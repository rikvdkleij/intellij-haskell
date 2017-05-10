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

import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi._
import intellij.haskell.HaskellFile
import intellij.haskell.external.component._
import intellij.haskell.psi.HaskellPsiUtil._
import intellij.haskell.psi._
import intellij.haskell.util.{HaskellProjectUtil, LineColumnPosition}

class HaskellReference(element: HaskellNamedElement, textRange: TextRange) extends PsiPolyVariantReferenceBase[HaskellNamedElement](element, textRange) {

  override def resolve: PsiElement = {
    val resolveResults = multiResolve(false)
    if (resolveResults.length == 1) resolveResults(0).getElement else null
  }

  override def multiResolve(incompleteCode: Boolean): Array[ResolveResult] = {
    val psiFile = element.getContainingFile
    val project = element.getProject

    ProgressManager.checkCanceled()

    val result = element match {
      case mi: HaskellModid => HaskellComponentsManager.findHaskellFiles(project, mi.getName).map(HaskellFileResolveResult)
      case qe: HaskellQualifierElement =>
        val importDeclarations = findImportDeclarations(psiFile)
        findQualifier(importDeclarations, qe) match {
          case Some(q) => findNamedElement(q).map(HaskellNamedElementResolveResult).toIterable
          case None => val moduleFiles = findModuleFiles(importDeclarations, qe, project).map(_.getOriginalFile)
            if (moduleFiles.isEmpty) {
              // return itself
              findNamedElement(element).map(HaskellNamedElementResolveResult).toIterable
            } else {
              moduleFiles.map(HaskellFileResolveResult)
            }
        }
      case ne: HaskellNamedElement if findImportHidingDeclarationParent(ne).isDefined => Iterable()
      case ne: HaskellNamedElement =>
        ProgressManager.checkCanceled()
        HaskellReference.resolveReference(ne, psiFile, project).map(HaskellNamedElementResolveResult)
      case _ => Iterable()
    }
    result.toArray[ResolveResult]
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

  def resolveReference(namedElement: HaskellNamedElement, psiFile: PsiFile, project: Project): Iterable[HaskellNamedElement] = {
    HaskellProjectUtil.isLibraryFile(psiFile).map(isLibraryFile => {
      ProgressManager.checkCanceled()

      if (isLibraryFile) {
        resolveReferencesByNameInfo(namedElement, psiFile, project)
      }
      else {
        resolveReferenceByDefinitionLocation(namedElement, project) match {
          case Some(ne) => Iterable(ne)
          case None =>
            if (HaskellPsiUtil.findQualifiedNameParent(namedElement).exists(_.getQualifierName.isDefined)) {
              resolveReferencesByNameInfo(namedElement, psiFile, project)
            }
            else {
              Iterable()
            }
        }
      }
    }).getOrElse(Iterable())
  }

  def resolveInstanceReferences(namedElement: HaskellNamedElement, psiFile: PsiFile, project: Project): Iterable[HaskellNamedElement] = {
    HaskellComponentsManager.findNameInfo(namedElement).flatMap { ni =>
      findNamedElementsByNameInfo(ni, namedElement, project)
    }
  }

  def findNamedElementsByLibraryNameInfo(libraryNameInfo: LibraryNameInfo, name: String, project: Project): Iterable[HaskellNamedElement] = {
    HaskellComponentsManager.findHaskellFiles(project, libraryNameInfo.moduleName).flatMap { f =>
      ProgressManager.checkCanceled()
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
    }.toSeq.distinct
  }

  def findNamedElementsInModule(moduleName: String, name: String, project: Project): Iterable[HaskellNamedElement] = {
    HaskellComponentsManager.findHaskellFiles(project, moduleName).flatMap { hf =>
      HaskellPsiUtil.findHaskellDeclarationElements(hf).flatMap(_.getIdentifierElements).find(_.getName == name)
    }
  }

  private def resolveReferencesByNameInfo(namedElement: HaskellNamedElement, psiFile: PsiFile, project: Project): Iterable[HaskellNamedElement] = {
    HaskellComponentsManager.findNameInfo(namedElement).headOption.map(ni => findNamedElementsByNameInfo(ni, namedElement, project)) match {
      case Some(nes) => nes
      case None =>
        ProgressManager.checkCanceled()
        findHaskellDeclarationElements(psiFile).flatMap(_.getIdentifierElements).filter(_.getName == namedElement.getName).toSeq.distinct
    }
  }

  private def resolveReferenceByDefinitionLocation(namedElement: HaskellNamedElement, project: Project): Option[HaskellNamedElement] = {
    HaskellComponentsManager.findDefinitionLocation(namedElement) match {
      case Right(DefinitionLocationInfo(filePath, startLineNr, startColumnNr, _, _)) =>
        findNamedElementByLocation(filePath, startLineNr, startColumnNr, namedElement.getName, project)
      case Right(ModuleLocationInfo(moduleName)) =>
        findNamedElementsInModule(moduleName, namedElement.getName, project).headOption
      case Left(ReplNotAvailable()) => None
      case Left(NoAvailableLocationInfo()) => None
    }
  }

  def findNamedElementByLocation(filePath: String, lineNr: Integer, columnNr: Integer, name: String, project: Project): Option[HaskellNamedElement] = {
    for {
      haskellFile <- HaskellProjectUtil.findFile(filePath, project)
      offset <- {
        LineColumnPosition.getOffset(haskellFile, LineColumnPosition(lineNr, columnNr))
      }
      element <- Option(haskellFile.findElementAt(offset))
      namedElement <- HaskellPsiUtil.findNamedElement(element).orElse(findHighestDeclarationElementParent(element).flatMap(_.getIdentifierElements.find(_.getName == name))).
        orElse(findQualifiedNameParent(element).map(_.getIdentifierElement))
    } yield namedElement
  }

  private def findNamedElementsByNameInfo(nameInfo: NameInfo, namedElement: HaskellNamedElement, project: Project): Iterable[HaskellNamedElement] = {
    nameInfo match {
      case pni: ProjectNameInfo => findNamedElementByLocation(pni.filePath, pni.lineNr, pni.columnNr, namedElement.getName, project).toIterable
      case lni: LibraryNameInfo => findNamedElementsByLibraryNameInfo(lni, namedElement.getName, project)
      case _ => Iterable()
    }
  }
}

case class HaskellNamedElementResolveResult(element: HaskellNamedElement) extends PsiElementResolveResult(element)

case class HaskellFileResolveResult(element: PsiElement) extends PsiElementResolveResult(element)

