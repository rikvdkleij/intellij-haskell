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
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi._
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTreeUtil
import intellij.haskell.external.component.NameInfoComponentResult.{LibraryNameInfo, NameInfo, ProjectNameInfo}
import intellij.haskell.external.component._
import intellij.haskell.psi._
import intellij.haskell.util._
import intellij.haskell.util.index.HaskellModuleNameIndex

class HaskellReference(element: HaskellNamedElement, textRange: TextRange) extends PsiPolyVariantReferenceBase[HaskellNamedElement](element, textRange) {

  override def multiResolve(incompleteCode: Boolean): Array[ResolveResult] = {
    val project = element.getProject
    if (StackProjectManager.isBuilding(project)) {
      HaskellEditorUtil.showHaskellSupportIsNotAvailableWhileBuilding(project)
      Array()
    } else {
      ProgressManager.checkCanceled()
      val result = Option(element.getContainingFile).flatMap { psiFile =>

        element match {
          case mi: HaskellModid => HaskellModuleNameIndex.findHaskellFileByModuleName(project, mi.getName, GlobalSearchScope.allScope(project)) match {
            case Right(pf) => pf.map(HaskellFileResolveResult)
            case Left(noInfo) => Some(NoResolveResult(noInfo))
          }

          case qe: HaskellQualifierElement =>
            val importDeclarations = HaskellPsiUtil.findImportDeclarations(psiFile)
            findQualifier(importDeclarations, qe) match {
              case Some(q) => HaskellPsiUtil.findNamedElement(q).map(HaskellNamedElementResolveResult)
              case None => findHaskellFile(importDeclarations, qe, project) match {
                case Right(r) => r.map(HaskellFileResolveResult)
                case Left(noInfo) => Some(NoResolveResult(noInfo))
              }
            }
          case ne: HaskellNamedElement if HaskellPsiUtil.findImportHidingDeclarationParent(ne).isDefined => None
          case ne: HaskellNamedElement =>
            if (HaskellPsiUtil.findQualifierParent(ne).isDefined) {
              None
            } else {
              ProgressManager.checkCanceled()
              HaskellPsiUtil.findTypeSignatureDeclarationParent(ne) match {
                case None => resolveReference(ne, psiFile, project) match {
                  case Right(r) => r.map(HaskellNamedElementResolveResult)
                  case Left(noInfo) => Some(NoResolveResult(noInfo))
                }
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
                    case None => resolveReference(ne, psiFile, project) match {
                      case Right(r) => r.map(HaskellNamedElementResolveResult)
                      case Left(noInfo) => Some(NoResolveResult(noInfo))
                    }
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

  private def resolveReference(namedElement: HaskellNamedElement, psiFile: PsiFile, project: Project): Either[NoInfo, Option[HaskellNamedElement]] = {
    ProgressManager.checkCanceled()

    HaskellPsiUtil.findQualifiedNameParent(namedElement) match {
      case Some(qualifiedNameElement) =>
        ProgressManager.checkCanceled()

        val isLibraryFile = HaskellProjectUtil.isLibraryFile(psiFile)
        if (isLibraryFile) {
          resolveReferenceByNameInfo(qualifiedNameElement, namedElement, psiFile, project)
        } else {
          resolveReferenceByDefinitionLocation(qualifiedNameElement, psiFile)
        }
      case None => Left(NoInfoAvailable(ApplicationUtil.runReadAction(namedElement.getName), psiFile.getName))
    }
  }

  private def resolveReferenceByNameInfo(qualifiedNameElement: HaskellQualifiedNameElement, namedElement: HaskellNamedElement, psiFile: PsiFile, project: Project): Either[NoInfo, Option[HaskellNamedElement]] = {
    ProgressManager.checkCanceled()

    val referenceNamedElement = HaskellComponentsManager.findNameInfo(qualifiedNameElement) match {
      case Some(result) => result match {
        case Right(infos) => infos.headOption match {
          case Some(info) => HaskellReference.findIdentifiersByNameInfo(info, namedElement, project)
          case None => Right(None)
        }
        case Left(_) => Right(None)
      }
      case None => Right(None)
    }

    if (referenceNamedElement.exists(_.isEmpty)) {
      ProgressManager.checkCanceled()
      Right(HaskellPsiUtil.findHaskellDeclarationElements(psiFile).flatMap(_.getIdentifierElements).filter(_.getName == namedElement.getName).toSeq.headOption)
    } else {
      referenceNamedElement
    }
  }

  private def resolveReferenceByDefinitionLocation(qualifiedNameElement: HaskellQualifiedNameElement, psiFile: PsiFile): Either[NoInfo, Option[HaskellNamedElement]] = {
    ProgressManager.checkCanceled()
    val project = psiFile.getProject

    val isCurrentSelectedFile = HaskellFileUtil.findVirtualFile(psiFile).exists(vf => FileEditorManager.getInstance(project).getSelectedFiles.headOption.contains(vf))

    HaskellComponentsManager.findDefinitionLocation(psiFile, qualifiedNameElement, isCurrentFile = isCurrentSelectedFile) match {
      case Right(DefinitionLocation(_, ne)) => Right(Some(ne))
      case Left(noInfo) => Left(noInfo)
    }
  }

  private def findQualifier(importDeclarations: Iterable[HaskellImportDeclaration], qualifierElement: HaskellQualifierElement): Option[HaskellNamedElement] = {
    importDeclarations.flatMap(id => Option(id.getImportQualifiedAs)).flatMap(iqa => Option(iqa.getQualifier)).find(_.getName == qualifierElement.getName).
      orElse(importDeclarations.filter(id => Option(id.getImportQualified).isDefined && Option(id.getImportQualifiedAs).isEmpty).find(mi => Option(mi.getModid).map(_.getName).contains(qualifierElement.getName)).map(_.getModid))
  }

  private def findHaskellFile(importDeclarations: Iterable[HaskellImportDeclaration], qualifierElement: HaskellQualifierElement, project: Project): Either[NoInfo, Option[PsiFile]] = {
    val result = for {
      id <- importDeclarations.find(id => id.getModuleName.contains(qualifierElement.getName))
      mn <- id.getModuleName
    } yield HaskellModuleNameIndex.findHaskellFileByModuleName(project, mn, GlobalSearchScope.allScope(project))

    result match {
      case None => Right(None)
      case Some(r) => r
    }
  }
}

object HaskellReference {

  def resolveInstanceReferences(project: Project, namedElement: HaskellNamedElement, nameInfos: Iterable[NameInfoComponentResult.NameInfo]): Seq[HaskellNamedElement] = {
    val result = nameInfos.map(ni => findIdentifiersByNameInfo(ni, namedElement, project)).toSeq.distinct
    if (result.contains(Left(ReadActionTimeout))) {
      HaskellEditorUtil.showStatusBarBalloonMessage(project, "Navigating to instnace declarations is not available at this moment")
      Seq()
    } else {
      result.flatMap(_.toSeq).flatten
    }
  }

  def findIdentifiersByLibraryNameInfo(project: Project, module: Option[Module], libraryNameInfo: LibraryNameInfo, name: String): Either[NoInfo, Option[HaskellNamedElement]] = {
    findIdentifiersByModuleAndName(project, module, libraryNameInfo.moduleName, name)
  }

  def findIdentifiersByModuleAndName(project: Project, module: Option[Module], moduleName: String, name: String): Either[NoInfo, Option[HaskellNamedElement]] = {
    for {
      file <- findFileByModuleName(project, module, moduleName)
      ne <- Right(file.flatMap(findIdentifierInFileByName(_, name)))
    } yield ne
  }

  def findIdentifierInFileByName(file: PsiFile, name: String): Option[HaskellNamedElement] = {
    import scala.collection.JavaConverters._

    ProgressManager.checkCanceled()

    val topLevelExpressions = HaskellPsiUtil.findTopLevelExpressions(file)

    ProgressManager.checkCanceled()

    val expressionIdentifiers = topLevelExpressions.flatMap(_.getQNameList.asScala.headOption.map(_.getIdentifierElement)).filter(_.getName == name)

    ProgressManager.checkCanceled()

    if (expressionIdentifiers.isEmpty) {
      val declarationElements = HaskellPsiUtil.findHaskellDeclarationElements(file)

      ProgressManager.checkCanceled()

      val declarationIdentifiers = declarationElements.flatMap(_.getIdentifierElements).filter(_.getName == name)

      ProgressManager.checkCanceled()

      declarationIdentifiers.toSeq.sortWith(sortByClassDeclarationFirst).headOption
    } else {
      expressionIdentifiers.headOption
    }
  }

  def findIdentifierByLocation(project: Project, virtualFile: Option[VirtualFile], psiFile: Option[PsiFile], lineNr: Integer, columnNr: Integer, name: String): (Option[String], Option[HaskellNamedElement]) = {
    ProgressManager.checkCanceled()
    val namedElement = for {
      pf <- psiFile
      () = ProgressManager.checkCanceled()
      vf <- virtualFile
      () = ProgressManager.checkCanceled()
      offset <- LineColumnPosition.getOffset(vf, LineColumnPosition(lineNr, columnNr))
      () = ProgressManager.checkCanceled()
      element <- Option(pf.findElementAt(offset))
      () = ProgressManager.checkCanceled()
      namedElement <- HaskellPsiUtil.findNamedElement(element).find(e => e.getName == name).
        orElse {
          ProgressManager.checkCanceled()
          None
        }.orElse(HaskellPsiUtil.findHighestDeclarationElementParent(element).flatMap(_.getIdentifierElements.find(e => e.getName == name)).
        orElse {
          ProgressManager.checkCanceled()
          None
        }.orElse(HaskellPsiUtil.findQualifiedNameParent(element).map(_.getIdentifierElement).find(e => e.getName == name)))
    } yield namedElement

    ProgressManager.checkCanceled()

    (psiFile.flatMap(HaskellPsiUtil.findModuleName), namedElement)
  }

  private def sortByClassDeclarationFirst(namedElement1: HaskellNamedElement, namedElement2: HaskellNamedElement): Boolean = {
    (HaskellPsiUtil.findDeclarationElementParent(namedElement1), HaskellPsiUtil.findDeclarationElementParent(namedElement1)) match {
      case (Some(_: HaskellClassDeclaration), _) => true
      case (_, _) => false
    }
  }

  def findFileByModuleName(project: Project, module: Option[Module], moduleName: String): Either[NoInfo, Option[PsiFile]] = {
    module match {
      case None => HaskellModuleNameIndex.findHaskellFileByModuleName(project, moduleName, GlobalSearchScope.allScope(project))
      case Some(m) => HaskellModuleNameIndex.findHaskellFileByModuleName(project, moduleName, m.getModuleWithDependenciesAndLibrariesScope(true))
    }
  }


  private def findIdentifiersByNameInfo(nameInfo: NameInfo, namedElement: HaskellNamedElement, project: Project): Either[NoInfo, Option[HaskellNamedElement]] = {
    nameInfo match {
      case pni: ProjectNameInfo =>
        val (virtualFile, psiFile) = HaskellProjectUtil.findFile(pni.filePath, project)
        psiFile match {
          case Right(pf) => Right(findIdentifierByLocation(project, virtualFile, pf, pni.lineNr, pni.columnNr, namedElement.getName)._2)
          case Left(noInfo) => Left(noInfo)
        }
      case lni: LibraryNameInfo => findIdentifiersByLibraryNameInfo(project, HaskellProjectUtil.findModule(namedElement), lni, namedElement.getName)
      case _ => Right(None)
    }
  }
}

case class HaskellNamedElementResolveResult(element: HaskellNamedElement) extends PsiElementResolveResult(element)

case class HaskellFileResolveResult(element: PsiElement) extends PsiElementResolveResult(element)

case class NoResolveResult(noInfo: NoInfo) extends ResolveResult {
  override def getElement: PsiElement = null

  override def isValidResult: Boolean = false
}

