/*
 * Copyright 2014-2017 Rik van der Kleij
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

import com.intellij.openapi.module.Module
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi._
import com.intellij.psi.search.GlobalSearchScope
import intellij.haskell.HaskellFile
import intellij.haskell.external.component._
import intellij.haskell.psi.HaskellPsiUtil._
import intellij.haskell.psi._
import intellij.haskell.util.index.HaskellModuleNameIndex
import intellij.haskell.util.{HaskellProjectUtil, LineColumnPosition}

class HaskellReference(element: HaskellNamedElement, textRange: TextRange) extends PsiPolyVariantReferenceBase[HaskellNamedElement](element, textRange) {

  override def resolve: PsiElement = {
    val resolveResults = multiResolve(false)
    if (resolveResults.length > 0) resolveResults(0).getElement else null
  }

  override def multiResolve(incompleteCode: Boolean): Array[ResolveResult] = {
    val psiFile = element.getContainingFile
    val project = element.getProject

    ProgressManager.checkCanceled()

    val result = element match {
      case mi: HaskellModid => HaskellModuleNameIndex.findHaskellFileByModuleName(project, mi.getName, GlobalSearchScope.allScope(project)).map(HaskellFileResolveResult).toIterable
      case qe: HaskellQualifierElement =>
        val importDeclarations = findImportDeclarations(psiFile)
        findQualifier(importDeclarations, qe) match {
          case Some(q) => findNamedElement(q).map(HaskellNamedElementResolveResult).toIterable
          case None => val files = findHaskellFiles(importDeclarations, qe, project)
            if (files.isEmpty) {
              // return itself
              findNamedElement(element).map(HaskellNamedElementResolveResult).toIterable
            } else {
              files.map(HaskellFileResolveResult)
            }
        }
      case ne: HaskellNamedElement if findImportHidingDeclarationParent(ne).isDefined => Iterable()
      case ne: HaskellNamedElement =>
        ProgressManager.checkCanceled()
        HaskellReference.resolveReferences(ne, psiFile, project).map(HaskellNamedElementResolveResult)
      case _ => Iterable()
    }
    result.toArray[ResolveResult]
  }

  /** Implemented in [[intellij.haskell.editor.HaskellCompletionContributor]] **/
  override def getVariants: Array[AnyRef] = {
    Array()
  }

  private def findQualifier(importDeclarations: Iterable[HaskellImportDeclaration], qualifierElement: HaskellQualifierElement): Option[HaskellNamedElement] = {
    importDeclarations.flatMap(id => Option(id.getImportQualifiedAs)).flatMap(iqa => Option(iqa.getQualifier)).find(_.getName == qualifierElement.getName).
      orElse(importDeclarations.filter(id => Option(id.getImportQualified).isDefined && Option(id.getImportQualifiedAs).isEmpty).find(mi => Option(mi.getModid).map(_.getName).contains(qualifierElement.getName)).map(_.getModid))
  }

  private def findHaskellFiles(importDeclarations: Iterable[HaskellImportDeclaration], qualifierElement: HaskellQualifierElement, project: Project): Iterable[HaskellFile] = {
    importDeclarations.flatMap(id => Option(id.getModid)).find(_.getName == qualifierElement.getName).
      flatMap(mi => HaskellModuleNameIndex.findHaskellFileByModuleName(project, mi.getName, GlobalSearchScope.allScope(project))).toIterable
  }
}

object HaskellReference {

  private def resolveReferences(namedElement: HaskellNamedElement, psiFile: PsiFile, project: Project): Iterable[HaskellNamedElement] = {
    ProgressManager.checkCanceled()
    HaskellProjectUtil.isLibraryFile(psiFile).map(isLibraryFile => {
      ProgressManager.checkCanceled()

      if (isLibraryFile) {
        resolveReferencesByNameInfo(namedElement, psiFile, project, preferExpression = false)
      } else {
        if (HaskellPsiUtil.findQualifiedNameParent(namedElement).exists(_.getQualifierName.isDefined)) {
          resolveReferencesByNameInfo(namedElement, psiFile, project, preferExpression = true)
        } else {
          resolveReferenceByDefinitionLocation(namedElement, psiFile)
        }
      }
    }).getOrElse(Iterable())
  }

  def resolveInstanceReferences(namedElement: HaskellNamedElement, psiFile: PsiFile, project: Project): Seq[HaskellNamedElement] = {
    HaskellComponentsManager.findNameInfo(namedElement, forceGetInfo = false).flatMap { ni =>
      findIdentifiersByNameInfo(ni, namedElement, project, preferExpressions = false)
    }.toSeq.distinct
  }

  def findIdentifiersByLibraryNameInfo(libraryNameInfo: LibraryNameInfo, name: String, project: Project, module: Option[Module], preferExpressions: Boolean): Iterable[HaskellNamedElement] = {
    findHaskellFileByModuleName(project, module, libraryNameInfo.moduleName).map(haskellFile => {

      def findDeclarationIdentifiers = {
        val declarationElements = findHaskellDeclarationElements(haskellFile)

        val identifiers = declarationElements.filter(_.getIdentifierElements.forall(e => libraryNameInfo.shortenedDeclaration.contains(e.getName))).flatMap(_.getIdentifierElements).filter(_.getName == name)
        if (identifiers.isEmpty) {
          findIdentifiersInDeclarations(haskellFile, name)
        } else {
          identifiers
        }
      }

      ProgressManager.checkCanceled()

      val identifiers =
        if (preferExpressions) {
          val identifiersInExpressions = findIdentifiersInExpressions(haskellFile, name)
          if (identifiersInExpressions.isEmpty) {
            findDeclarationIdentifiers
          } else {
            identifiersInExpressions
          }
        } else {
          findDeclarationIdentifiers
        }
      identifiers.toSeq.sortWith(sortByClassDeclarationFirst)
    }).getOrElse(Iterable())
  }

  def findIdentifiersByModuleName(moduleName: String, name: String, project: Project, module: Option[Module], preferExpressions: Boolean): Iterable[HaskellNamedElement] = {
    findHaskellFileByModuleName(project, module, moduleName).map(haskellFile => {

      ProgressManager.checkCanceled()

      val namedElements = if (preferExpressions && HaskellProjectUtil.isProjectFile(haskellFile).contains(true)) {
        val expressionNamedElements = findIdentifiersInExpressions(haskellFile, name)
        if (expressionNamedElements.isEmpty) {
          findIdentifiersInDeclarations(haskellFile, name)
        } else {
          expressionNamedElements
        }
      } else {
        findIdentifiersInDeclarations(haskellFile, name)
      }
      namedElements.toSeq.sortWith(sortByClassDeclarationFirst)
    }).getOrElse(Iterable())
  }

  def findIdentifierByLocation(filePath: String, lineNr: Integer, columnNr: Integer, name: String, project: Project): Option[HaskellNamedElement] = {
    ProgressManager.checkCanceled()
    for {
      haskellFile <- HaskellProjectUtil.findFile(filePath, project)
      offset <- LineColumnPosition.getOffset(haskellFile, LineColumnPosition(lineNr, columnNr))
      element <- Option(haskellFile.findElementAt(offset))
      namedElement <- HaskellPsiUtil.findNamedElement(element).find(_.getName == name).orElse(findHighestDeclarationElementParent(element).flatMap(_.getIdentifierElements.find(_.getName == name))).
        orElse(findQualifiedNameParent(element).map(_.getIdentifierElement).find(_.getName == name))
    } yield namedElement
  }

  private def findIdentifiersInDeclarations(haskellFile: HaskellFile, name: String) = {
    HaskellPsiUtil.findHaskellDeclarationElements(haskellFile).flatMap(_.getIdentifierElements).filter(_.getName == name)
  }

  private def findIdentifiersInExpressions(haskellFile: HaskellFile, name: String) = {
    HaskellPsiUtil.findTopLevelExpressions(haskellFile).flatMap(e => getChildOfType(e, classOf[HaskellQName])).map(_.getIdentifierElement).filter(ne => ne.getName == name)
  }

  private def sortByClassDeclarationFirst(namedElement1: HaskellNamedElement, namedElement2: HaskellNamedElement): Boolean = {
    (HaskellPsiUtil.findDeclarationElementParent(namedElement1), HaskellPsiUtil.findDeclarationElementParent(namedElement1)) match {
      case (Some(_: HaskellClassDeclaration), _) => true
      case (_, _) => false
    }
  }

  private def findHaskellFileByModuleName(project: Project, module: Option[Module], moduleName: String): Option[HaskellFile] = {
    module match {
      case None => HaskellModuleNameIndex.findHaskellFileByModuleName(project, moduleName, GlobalSearchScope.allScope(project))
      case Some(m) => HaskellModuleNameIndex.findHaskellFileByModuleName(project, moduleName, m.getModuleWithDependenciesAndLibrariesScope(true))
    }
  }

  private def resolveReferencesByNameInfo(namedElement: HaskellNamedElement, psiFile: PsiFile, project: Project, preferExpression: Boolean): Iterable[HaskellNamedElement] = {
    ProgressManager.checkCanceled()

    HaskellComponentsManager.findNameInfo(namedElement, forceGetInfo = false).headOption.map(ni => findIdentifiersByNameInfo(ni, namedElement, project, preferExpression)) match {
      case Some(nes) => nes
      case None =>
        ProgressManager.checkCanceled()
        findHaskellDeclarationElements(psiFile).flatMap(_.getIdentifierElements).filter(_.getName == namedElement.getName).toSeq.distinct
    }
  }

  private def resolveReferenceByDefinitionLocation(namedElement: HaskellNamedElement, psiFile: PsiFile): Iterable[HaskellNamedElement] = {
    ProgressManager.checkCanceled()

    val project = psiFile.getProject

    HaskellComponentsManager.findDefinitionLocation(namedElement) match {
      case Some(DefinitionLocationInfo(filePath, startLineNr, startColumnNr, _, _)) =>
        ProgressManager.checkCanceled()
        findIdentifierByLocation(filePath, startLineNr, startColumnNr, namedElement.getName, project)
      case Some(ModuleLocationInfo(moduleName)) =>
        ProgressManager.checkCanceled()
        val module = HaskellProjectUtil.findModule(namedElement)
        findIdentifiersByModuleName(moduleName, namedElement.getName, project, module, preferExpressions = true)
      case None => resolveReferencesByNameInfo(namedElement, psiFile, project, preferExpression = true)
    }
  }

  private def findIdentifiersByNameInfo(nameInfo: NameInfo, namedElement: HaskellNamedElement, project: Project, preferExpressions: Boolean): Iterable[HaskellNamedElement] = {
    nameInfo match {
      case pni: ProjectNameInfo => findIdentifierByLocation(pni.filePath, pni.lineNr, pni.columnNr, namedElement.getName, project).toIterable
      case lni: LibraryNameInfo => findIdentifiersByLibraryNameInfo(lni, namedElement.getName, project, HaskellProjectUtil.findModule(namedElement), preferExpressions)
      case _ => Iterable()
    }
  }
}

case class HaskellNamedElementResolveResult(element: HaskellNamedElement) extends PsiElementResolveResult(element)

case class HaskellFileResolveResult(element: PsiElement) extends PsiElementResolveResult(element)

