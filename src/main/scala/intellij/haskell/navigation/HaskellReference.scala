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
import com.intellij.psi.util.PsiTreeUtil
import intellij.haskell.HaskellFile
import intellij.haskell.external.component.NameInfoComponentResult.{LibraryNameInfo, NameInfo, ProjectNameInfo}
import intellij.haskell.external.component._
import intellij.haskell.psi._
import intellij.haskell.util._
import intellij.haskell.util.index.HaskellModuleNameIndex

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
            val importDeclarations = HaskellPsiUtil.findImportDeclarations(psiFile)
            findQualifier(importDeclarations, qe) match {
              case Some(q) => HaskellPsiUtil.findNamedElement(q).map(HaskellNamedElementResolveResult)
              case None => val files = findHaskellFiles(importDeclarations, qe, project)
                if (files.isEmpty) {
                  // return itself
                  HaskellPsiUtil.findNamedElement(element).map(HaskellNamedElementResolveResult)
                } else {
                  files.map(HaskellFileResolveResult).headOption
                }
            }
          case ne: HaskellNamedElement if HaskellPsiUtil.findImportHidingDeclarationParent(ne).isDefined => None
          case ne: HaskellNamedElement =>
            if (HaskellPsiUtil.findQualifierParent(ne).isDefined) {
              None
            } else {
              ProgressManager.checkCanceled()
              HaskellPsiUtil.findTypeSignatureDeclarationParent(ne) match {
                case None => resolveReference(ne, psiFile, project).map(HaskellNamedElementResolveResult)
                case Some(ts) =>
                  def find(e: PsiElement): Option[HaskellNamedElement] = {
                    Option(PsiTreeUtil.findSiblingForward(e, HaskellTypes.HS_TOP_DECLARATION, null)) match {
                      case Some(d) if Option(d.getFirstChild).exists(_.isInstanceOf[HaskellExpression]) => HaskellPsiUtil.findNamedElements(d).headOption.find(_.getName == ne.getName)
                      case Some(_) => find(e)
                      case None => None
                    }
                  }

                  // Work around Intero bug.
                  find(ts.getParent) match {
                    case Some(ee) => Some(HaskellNamedElementResolveResult(ee))
                    case None => resolveReference(ne, psiFile, project).map(HaskellNamedElementResolveResult)
                  }
              }
            }
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
        resolveReferenceByNameInfo(qualifiedNameElement, namedElement, psiFile, project)
      } else {
        resolveReferenceByDefinitionLocation(qualifiedNameElement, psiFile)
      }
    })
  }

  private def resolveReferenceByNameInfo(qualifiedNameElement: HaskellQualifiedNameElement, namedElement: HaskellNamedElement, psiFile: PsiFile, project: Project): Option[HaskellNamedElement] = {
    ProgressManager.checkCanceled()

    val referenceNamedElement = HaskellComponentsManager.findNameInfo(qualifiedNameElement) match {
      case Some(result) => result match {
        case Right(infos) => infos.headOption.flatMap(info => HaskellReference.findIdentifiersByNameInfo(info, namedElement, project))
        case Left(_) => None
      }
      case None => None
    }

    if (referenceNamedElement.isEmpty) {
      ProgressManager.checkCanceled()
      HaskellPsiUtil.findHaskellDeclarationElements(psiFile).flatMap(_.getIdentifierElements).filter(_.getName == namedElement.getName).toSeq.headOption
    } else {
      referenceNamedElement
    }
  }

  private def resolveReferenceByDefinitionLocation(qualifiedNameElement: HaskellQualifiedNameElement, psiFile: PsiFile): Option[HaskellNamedElement] = {
    ProgressManager.checkCanceled()
    val project = psiFile.getProject

    def noNavigationMessage(noInfo: NoInfo) = {
      val message = s"Navigation is not available at this moment"
      HaskellEditorUtil.showStatusBarBalloonMessage(project, message)
    }

    val isCurrentSelectedFile = HaskellFileUtil.findVirtualFile(psiFile).exists(vf => FileEditorManager.getInstance(project).getSelectedFiles.headOption.contains(vf))

    HaskellComponentsManager.findDefinitionLocation(psiFile, qualifiedNameElement, isCurrentFile = isCurrentSelectedFile) match {
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

  def resolveInstanceReferences(project: Project, namedElement: HaskellNamedElement, nameInfos: Iterable[NameInfoComponentResult.NameInfo]): Seq[HaskellNamedElement] = {
    nameInfos.flatMap(ni => findIdentifiersByNameInfo(ni, namedElement, project)).toSeq.distinct
  }

  def findIdentifiersByLibraryNameInfo(project: Project, module: Option[Module], libraryNameInfo: LibraryNameInfo, name: String): Iterable[HaskellNamedElement] = {
    findIdentifiersByModuleAndName(project, module, libraryNameInfo.moduleName, name)
  }

  def findIdentifiersByModuleAndName(project: Project, module: Option[Module], moduleName: String, name: String): Iterable[HaskellNamedElement] = {
    findFileByModuleName(project, module, moduleName).map(file => {
      findIdentifiersInFileByName(file, name)
    }).getOrElse(Iterable())
  }

  def findIdentifiersInFileByName(file: HaskellFile, name: String): Iterable[HaskellNamedElement] = {
    import scala.collection.JavaConverters._

    ProgressManager.checkCanceled()

    val topLevelExpressions = HaskellPsiUtil.findTopLevelExpressions(file)

    ProgressManager.checkCanceled()

    val topLevelIdentifiers = topLevelExpressions.flatMap(_.getQNameList.asScala.headOption.map(_.getIdentifierElement)).filter(_.getName == name)

    ProgressManager.checkCanceled()

    val declarationElements = HaskellPsiUtil.findHaskellDeclarationElements(file)

    ProgressManager.checkCanceled()
    val namedElements = declarationElements.flatMap(_.getIdentifierElements).filter(_.getName == name)

    ProgressManager.checkCanceled()

    val identifiers = if (namedElements.isEmpty) {
      HaskellPsiUtil.findHaskellDeclarationElements(file).flatMap(_.getIdentifierElements).filter(_.getName == name)
    } else {
      namedElements
    }

    topLevelIdentifiers.toSeq ++ identifiers.toSeq.sortWith(sortByClassDeclarationFirst)
  }

  def findIdentifierByLocation(project: Project, filePath: String, lineNr: Integer, columnNr: Integer, name: String): (Option[String], Option[HaskellNamedElement]) = {
    ProgressManager.checkCanceled()
    val psiFile = HaskellProjectUtil.findFile(filePath, project)
    ProgressManager.checkCanceled()
    val namedElement = for {
      pf <- psiFile
      offset <- LineColumnPosition.getOffset(pf, LineColumnPosition(lineNr, columnNr))
      element <- Option(pf.findElementAt(offset))
      namedElement <- HaskellPsiUtil.findNamedElement(element).find(_.getName == name).orElse(HaskellPsiUtil.findHighestDeclarationElementParent(element).flatMap(_.getIdentifierElements.find(_.getName == name))).
        orElse(HaskellPsiUtil.findQualifiedNameParent(element).map(_.getIdentifierElement).find(_.getName == name))
    } yield namedElement

    (psiFile.flatMap(HaskellPsiUtil.findModuleName), namedElement)
  }

  private def sortByClassDeclarationFirst(namedElement1: HaskellNamedElement, namedElement2: HaskellNamedElement): Boolean = {
    (HaskellPsiUtil.findDeclarationElementParent(namedElement1), HaskellPsiUtil.findDeclarationElementParent(namedElement1)) match {
      case (Some(_: HaskellClassDeclaration), _) => true
      case (_, _) => false
    }
  }

  def findFileByModuleName(project: Project, module: Option[Module], moduleName: String): Option[HaskellFile] = {
    module match {
      case None => HaskellReference.findHaskellFileByModuleNameIndex(project, moduleName, GlobalSearchScope.allScope(project))
      case Some(m) => HaskellReference.findHaskellFileByModuleNameIndex(project, moduleName, m.getModuleWithDependenciesAndLibrariesScope(true))
    }
  }


  private def findIdentifiersByNameInfo(nameInfo: NameInfo, namedElement: HaskellNamedElement, project: Project): Option[HaskellNamedElement] = {
    nameInfo match {
      case pni: ProjectNameInfo => findIdentifierByLocation(project, pni.filePath, pni.lineNr, pni.columnNr, namedElement.getName)._2
      case lni: LibraryNameInfo => findIdentifiersByLibraryNameInfo(project, HaskellProjectUtil.findModule(namedElement), lni, namedElement.getName).headOption
      case _ => None
    }
  }
}

case class HaskellNamedElementResolveResult(element: HaskellNamedElement) extends PsiElementResolveResult(element)

case class HaskellFileResolveResult(element: PsiElement) extends PsiElementResolveResult(element)

