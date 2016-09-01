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
import intellij.haskell.psi._
import intellij.haskell.util.{HaskellProjectUtil, LineColumnPosition}

import scala.Option.option2Iterable

class HaskellReference(element: HaskellNamedElement, textRange: TextRange) extends PsiPolyVariantReferenceBase[HaskellNamedElement](element, textRange) {

  override def multiResolve(incompleteCode: Boolean): Array[ResolveResult] = {
    val project = myElement.getProject
    val thisFile = myElement.getContainingFile

    val result = myElement match {
      case mi: HaskellModid =>
        (for {
          file <- HaskellProjectUtil.findFilesForModule(mi.getName, project)
        } yield new HaskellFileResolveResult(file)).toArray
      case qe: HaskellQualifierElement =>
        val importDeclarations = HaskellPsiUtil.findImportDeclarations(myElement.getContainingFile)
        val resolveResults = findQualifier(importDeclarations, qe).map(q => Iterable(new HaskellQualifierResolveResult(q))).getOrElse(
          findModuleFiles(importDeclarations, qe, project).map(f => new HaskellFileResolveResult(f.getOriginalElement)))
        if (resolveResults.isEmpty) {
          // return itself
          HaskellPsiUtil.findNamedElement(myElement).map(new HaskellProjectResolveResult(_)).toArray
        } else {
          resolveResults.toArray
        }
      case ne: HaskellNamedElement =>
        val isProjectFile = HaskellProjectUtil.isProjectFile(thisFile)
        findReferences(ne, isProjectFile, project).toArray
      case _ => Array()
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
    importDeclarations.flatMap(id => Option(id.getModid)).find(mi => mi.getName == qualifierElement.getName).map(mi => HaskellProjectUtil.findFilesForModule(mi.getName, project)).getOrElse(Iterable())
  }

  private def findReferences(namedElement: HaskellNamedElement, isProjectFile: Boolean, project: Project) = {
    val nameInfos = findNameInfos(namedElement)
    val nameInfosResolvedResults = nameInfos.flatMap {
      case pni: ProjectNameInfo => findProjectReference(pni, namedElement, project).map(new HaskellProjectResolveResult(_)).toIterable
      case lni: LibraryNameInfo => findLibraryReferenceTo(lni, namedElement, project).map(ne => new HaskellLibraryResolveResult(ne))
      case bini: BuiltInNameInfo => Some(new BuiltInResolveResult(bini.declaration, bini.libraryName, bini.moduleName))
    }
    (if (isProjectFile) {
      nameInfosResolvedResults ++ findDefinitionLocationReference(namedElement, project).map(new HaskellProjectResolveResult(_)).toIterable
    } else {
      nameInfosResolvedResults
    }).toSeq.distinct
  }

  private def findDefinitionLocationReference(namedElement: HaskellNamedElement, project: Project): Option[HaskellNamedElement] = {
    StackReplsComponentsManager.findDefinitionLocation(namedElement).flatMap(location => {
      findReference(location.filePath, location.startLineNr, location.startColumnNr, namedElement, project)
    })
  }

  private def findProjectReference(projectNameInfo: ProjectNameInfo, namedElement: HaskellNamedElement, project: Project): Option[HaskellNamedElement] = {
    findReference(projectNameInfo.filePath, projectNameInfo.lineNr, projectNameInfo.colNr, namedElement, project)
  }

  private def findReference(filePath: String, lineNr: Integer, columnNr: Integer, namedElement: HaskellNamedElement, project: Project): Option[HaskellNamedElement] = {
    for {
      haskellFile <- HaskellProjectUtil.findFile(filePath, project)
      offset <- LineColumnPosition.getOffset(haskellFile, LineColumnPosition(lineNr, columnNr))
      element <- Option(haskellFile.findElementAt(offset))
      namedElement <- HaskellPsiUtil.findDeclarationElementParent(element).flatMap(_.getIdentifierElements.find(_.getName == namedElement.getName)).
        orElse(HaskellPsiUtil.findQualifiedNameElement(element).map(_.getIdentifierElement))
    } yield namedElement
  }

  private def findLibraryReferenceTo(libraryNameInfo: LibraryNameInfo, namedElement: HaskellNamedElement, project: Project): Iterable[HaskellNamedElement] = {
    val file = HaskellProjectUtil.findFilesForModule(libraryNameInfo.moduleName, project).toSeq
    file.flatMap { f =>
      val namedElements = HaskellPsiUtil.findTopLevelDeclarationElements(f).flatMap(_.getIdentifierElements).filter(e => e.getName == namedElement.getName)
      val result = namedElements.filter(ne => StackReplsComponentsManager.findNameInfo(ne).exists(ni => compareWithoutSpaces(ni.unqualifiedDeclaration, libraryNameInfo.declaration)))
      if (result.isEmpty) {
        HaskellPsiUtil.findTopLevelDeclarationElements(f).filter(de => compareWithoutSpaces(libraryNameInfo.unqualifiedDeclaration, de.getText)).flatMap(_.getIdentifierElements)
      } else {
        result
      }
    }
  }

  private def compareWithoutSpaces(s1: String, s2: String): Boolean = {
    s1.replaceAll("""\s+""", "") == s2.replaceAll("""\s+""", "")
  }

  private def findNameInfos(namedElement: HaskellNamedElement): Iterable[NameInfo] = {
    StackReplsComponentsManager.findNameInfo(namedElement)
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

