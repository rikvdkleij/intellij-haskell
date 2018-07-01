/*
 * Copyright 2014-2018 Rik van der Kleij
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

import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.module.Module
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.{IndexNotReadyException, Project}
import com.intellij.openapi.util.TextRange
import com.intellij.psi._
import com.intellij.psi.search.GlobalSearchScope
import intellij.haskell.external.component.NameInfoComponentResult.{LibraryNameInfo, NameInfo, ProjectNameInfo}
import intellij.haskell.external.component._
import intellij.haskell.psi.HaskellPsiUtil._
import intellij.haskell.psi._
import intellij.haskell.util._
import intellij.haskell.util.index.{HaskellFilePathIndex, HaskellModuleNameIndex}
import intellij.haskell.{HaskellFile, HaskellNotificationGroup}

class HaskellReference(element: HaskellNamedElement, textRange: TextRange) extends PsiPolyVariantReferenceBase[HaskellNamedElement](element, textRange) {

  override def resolve: PsiElement = {
    val resolveResults = multiResolve(false)
    if (resolveResults.length > 0) resolveResults(0).getElement else null
  }

  override def multiResolve(incompleteCode: Boolean): Array[ResolveResult] = {
    val project = element.getProject
    if (StackProjectManager.isBuilding(project)) {
      HaskellEditorUtil.showHaskellSupportIsNotAvailableWhileBuilding(project)
      Array()
    } else {
      ProgressManager.checkCanceled()
      val result = Option(element.getContainingFile).flatMap { psiFile =>

        element match {
          case mi: HaskellModid => HaskellReference.findHaskellFileByModuleNameIndex(project, mi.getName, GlobalSearchScope.allScope(project)).map(HaskellFileResolveResult)
          case qe: HaskellQualifierElement =>
            val importDeclarations = findImportDeclarations(psiFile)
            findQualifier(importDeclarations, qe) match {
              case Some(q) => findNamedElement(q).map(HaskellNamedElementResolveResult)
              case None => val files = findHaskellFiles(importDeclarations, qe, project)
                if (files.isEmpty) {
                  // return itself
                  findNamedElement(element).map(HaskellNamedElementResolveResult)
                } else {
                  files.map(HaskellFileResolveResult).headOption
                }
            }
          case ne: HaskellNamedElement if findImportHidingDeclarationParent(ne).isDefined => None
          case ne: HaskellNamedElement =>
            ProgressManager.checkCanceled()
            resolveReference(ne, psiFile, project).map(HaskellNamedElementResolveResult)
          case _ => None
        }
      }
      result.toArray[ResolveResult]
    }
  }

  /** Implemented in [[intellij.haskell.editor.HaskellCompletionContributor]] **/
  override def getVariants: Array[AnyRef] = {
    Array()
  }

  private def resolveReference(namedElement: HaskellNamedElement, psiFile: PsiFile, project: Project): Option[HaskellNamedElement] = {
    ProgressManager.checkCanceled()
    HaskellPsiUtil.findQualifiedNameParent(namedElement).flatMap(qualifiedNameElement => {
      val isLibraryFile = HaskellProjectUtil.isLibraryFile(psiFile)
      if (isLibraryFile) {
        resolveReferenceByNameInfo(qualifiedNameElement, namedElement, psiFile, project, preferExpression = false)
      } else {
        resolveReferenceByDefinitionLocation(qualifiedNameElement, namedElement, psiFile)
      }
    })
  }

  private def resolveReferenceByNameInfo(qualifiedNameElement: HaskellQualifiedNameElement, namedElement: HaskellNamedElement, psiFile: PsiFile, project: Project, preferExpression: Boolean): Option[HaskellNamedElement] = {
    ProgressManager.checkCanceled()

    val referenceNamedElement = HaskellComponentsManager.findNameInfo(qualifiedNameElement) match {
      case Some(result) => result match {
        case Right(infos) => infos.headOption.flatMap(info => HaskellReference.findIdentifiersByNameInfo(info, namedElement, project, preferExpression))
        case Left(noInfo) =>
          HaskellEditorUtil.showStatusBarMessage(project, noInfo.message)
          None
      }
      case None => None
    }

    if (referenceNamedElement.isEmpty) {
      ProgressManager.checkCanceled()
      findHaskellDeclarationElements(psiFile).flatMap(_.getIdentifierElements).filter(_.getName == namedElement.getName).toSeq.headOption
    } else {
      referenceNamedElement
    }
  }

  private def resolveReferenceByDefinitionLocation(qualifiedNameElement: HaskellQualifiedNameElement, namedElement: HaskellNamedElement, psiFile: PsiFile): Option[HaskellNamedElement] = {
    ProgressManager.checkCanceled()
    val project = psiFile.getProject

    def noNavigationMessage(noInfo: NoInfo) = {
      val message = s"Navigation is not available at this moment for ${namedElement.getName} because ${noInfo.message}"
      HaskellEditorUtil.showStatusBarMessage(project, message)
      HaskellNotificationGroup.logInfoEvent(project, message)
    }

    val isCurrentSelectedFile = HaskellFileUtil.findVirtualFile(psiFile).exists(vf => FileEditorManager.getInstance(project).getSelectedFiles.headOption.contains(vf))

    HaskellComponentsManager.findDefinitionLocation(psiFile, qualifiedNameElement: HaskellQualifiedNameElement, namedElement, isCurrentFile = isCurrentSelectedFile) match {
      case Right(DefinitionLocation(_, ne)) => Some(ne)
      case Left(noInfo) =>
        noNavigationMessage(noInfo)
        None
    }
  }

  private def findQualifier(importDeclarations: Iterable[HaskellImportDeclaration], qualifierElement: HaskellQualifierElement): Option[HaskellNamedElement] = {
    importDeclarations.flatMap(id => Option(id.getImportQualifiedAs)).flatMap(iqa => Option(iqa.getQualifier)).find(_.getName == qualifierElement.getName).
      orElse(importDeclarations.filter(id => Option(id.getImportQualified).isDefined && Option(id.getImportQualifiedAs).isEmpty).find(mi => Option(mi.getModid).map(_.getName).contains(qualifierElement.getName)).map(_.getModid))
  }

  private def findHaskellFiles(importDeclarations: Iterable[HaskellImportDeclaration], qualifierElement: HaskellQualifierElement, project: Project): Iterable[HaskellFile] = {
    importDeclarations.flatMap(id => Option(id.getModid).toIterable).find(_.getName == qualifierElement.getName).
      flatMap(mi => HaskellReference.findHaskellFileByModuleNameIndex(project, mi.getName, GlobalSearchScope.allScope(project)))
  }
}

object HaskellReference {

  private def findHaskellFileByModuleNameIndex(project: Project, moduleName: String, scope: GlobalSearchScope): Option[HaskellFile] = {
    try {
      ApplicationUtil.runReadAction(HaskellModuleNameIndex.findHaskellFileByModuleName(project, moduleName, scope))
    } catch {
      case _: IndexNotReadyException => None
    }
  }

  def resolveInstanceReferences(namedElement: HaskellNamedElement, nameInfos: Iterable[NameInfoComponentResult.NameInfo], project: Project): Seq[HaskellNamedElement] = {
    nameInfos.flatMap(ni => findIdentifiersByNameInfo(ni, namedElement, project, preferExpressions = false)).toSeq.distinct
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

  def findIdentifiersByModuleName(project: Project, module: Option[Module], moduleName: String, name: String): Iterable[HaskellNamedElement] = {
    findHaskellFileByModuleName(project, module, moduleName).map { haskellFile =>

      ProgressManager.checkCanceled()

      val expressionNamedElements = findIdentifiersInExpressions(haskellFile, name)
      val namedElements = if (expressionNamedElements.isEmpty) {
        findIdentifiersInDeclarations(haskellFile, name)
      } else {
        expressionNamedElements
      }
      namedElements.toSeq.sortWith(sortByClassDeclarationFirst)
    }.getOrElse(Iterable())
  }

  def findIdentifierByLocation(project: Project, filePath: String, lineNr: Integer, columnNr: Integer, name: String): (Option[String], Option[HaskellNamedElement]) = {
    ProgressManager.checkCanceled()
    val psiFile = HaskellProjectUtil.findFile(filePath, project)
    val namedElement = for {
      pf <- psiFile
      offset <- LineColumnPosition.getOffset(pf, LineColumnPosition(lineNr, columnNr))
      element <- Option(pf.findElementAt(offset))
      namedElement <- HaskellPsiUtil.findNamedElement(element).find(_.getName == name).orElse(findHighestDeclarationElementParent(element).flatMap(_.getIdentifierElements.find(_.getName == name))).
        orElse(findQualifiedNameParent(element).map(_.getIdentifierElement).find(_.getName == name))
    } yield namedElement

    (psiFile.flatMap(pf => HaskellFilePathIndex.findModuleName(pf, GlobalSearchScope.projectScope(project))), namedElement)
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
      case None => HaskellReference.findHaskellFileByModuleNameIndex(project, moduleName, GlobalSearchScope.allScope(project))
      case Some(m) => HaskellReference.findHaskellFileByModuleNameIndex(project, moduleName, m.getModuleWithDependenciesAndLibrariesScope(true))
    }
  }


  private def findIdentifiersByNameInfo(nameInfo: NameInfo, namedElement: HaskellNamedElement, project: Project, preferExpressions: Boolean): Option[HaskellNamedElement] = {
    nameInfo match {
      case pni: ProjectNameInfo => findIdentifierByLocation(project, pni.filePath, pni.lineNr, pni.columnNr, namedElement.getName)._2
      case lni: LibraryNameInfo => findIdentifiersByLibraryNameInfo(lni, namedElement.getName, project, HaskellProjectUtil.findModule(namedElement), preferExpressions).headOption
      case _ => None
    }
  }
}

case class HaskellNamedElementResolveResult(element: HaskellNamedElement) extends PsiElementResolveResult(element)

case class HaskellFileResolveResult(element: PsiElement) extends PsiElementResolveResult(element)

