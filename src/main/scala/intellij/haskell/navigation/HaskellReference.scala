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

import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi._
import com.intellij.psi.util.PsiTreeUtil
import intellij.haskell.external.component.NameInfoComponentResult.{LibraryNameInfo, NameInfo, ProjectNameInfo}
import intellij.haskell.external.component._
import intellij.haskell.psi._
import intellij.haskell.util._
import intellij.haskell.util.index.HaskellModuleNameIndex

class HaskellReference(element: HaskellNamedElement, textRange: TextRange) extends PsiPolyVariantReferenceBase[HaskellNamedElement](element, textRange) {

  override def multiResolve(incompleteCode: Boolean): Array[ResolveResult] = {
    val project = element.getProject
    if (StackProjectManager.isInitializing(project)) {
      HaskellEditorUtil.showHaskellSupportIsNotAvailableWhileInitializing(project)
      Array()
    } else if (!element.isPhysical) {
      // Can happen during code completion that element is virtual element
      Array()
    } else {
      ProgressManager.checkCanceled()
      val psiFile = element.getContainingFile.getOriginalFile

      val result = element match {
        case mi: HaskellModid => HaskellModuleNameIndex.findFileByModuleName(project, mi.getName) match {
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
          if (HaskellPsiUtil.findQualifierParent(ne).isDefined || HaskellPsiUtil.findModIdElement(element).isDefined) {
            // Because they are already handled by HaskellQualifierElement and HaskellModId case
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
                    case _ => None
                  }
                }

                ProgressManager.checkCanceled()

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
      result.toArray[ResolveResult]
    }
  }

  /** Implemented in [[intellij.haskell.editor.HaskellCompletionContributor]] **/
  override def getVariants: Array[AnyRef] = {
    Array()
  }

  private def resolveReference(namedElement: HaskellNamedElement, psiFile: PsiFile, project: Project): Either[NoInfo, Option[HaskellNamedElement]] = {
    ProgressManager.checkCanceled()

    def noInfo = {
      NoInfoAvailable(ApplicationUtil.runReadAction(namedElement.getName), psiFile.getName)
    }

    HaskellPsiUtil.findQualifiedNameParent(namedElement) match {
      case Some(qualifiedNameElement) =>
        ProgressManager.checkCanceled()

        ProgressManager.checkCanceled()
        if (HaskellProjectUtil.isSourceFile(psiFile)) {
          resolveReferenceByDefinitionLocation(qualifiedNameElement, psiFile)
        } else if (HaskellProjectUtil.isLibraryFile(psiFile)) {
          resolveReferenceByNameInfo(qualifiedNameElement, namedElement, psiFile, project)
        } else {
          Left(noInfo)
        }
      case None => Left(noInfo)
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
      Right(HaskellPsiUtil.findHaskellDeclarationElements(psiFile).toSeq.flatMap(_.getIdentifierElements).find(_.getName == namedElement.getName))
    } else {
      referenceNamedElement
    }
  }

  private def resolveReferenceByDefinitionLocation(qualifiedNameElement: HaskellQualifiedNameElement, psiFile: PsiFile): Either[NoInfo, Option[HaskellNamedElement]] = {
    ProgressManager.checkCanceled()

    HaskellComponentsManager.findDefinitionLocation(psiFile, qualifiedNameElement) match {
      case Right(PackageModuleLocation(_, ne)) => Right(Some(ne))
      case Right(LocalModuleLocation(_, ne)) => Right(Some(ne))
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
    } yield HaskellModuleNameIndex.findFileByModuleName(project, mn)

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

  def findIdentifiersByLibraryNameInfo(project: Project, libraryNameInfo: LibraryNameInfo, name: String): Either[NoInfo, Option[HaskellNamedElement]] = {
    findIdentifiersByModuleAndName(project, libraryNameInfo.moduleName, name)
  }

  def findIdentifiersByModuleAndName(project: Project, moduleName: String, name: String): Either[NoInfo, Option[HaskellNamedElement]] = {
    ProgressManager.checkCanceled()

    for {
      file <- HaskellModuleNameIndex.findFileByModuleName(project, moduleName)
      () = ProgressManager.checkCanceled()
      ne <- Right(file.flatMap(findIdentifierInFileByName(_, name)))
    } yield ne
  }

  def findIdentifierInFileByName(psifile: PsiFile, name: String): Option[HaskellNamedElement] = {
    import scala.collection.JavaConverters._

    def findInDeclarations = {
      val declarationElements = HaskellPsiUtil.findHaskellDeclarationElements(psifile)

      ProgressManager.checkCanceled()

      val declarationIdentifiers = declarationElements.flatMap(_.getIdentifierElements).filter(_.getName == name)

      ProgressManager.checkCanceled()

      declarationIdentifiers.toSeq.sortWith(sortByClassDeclarationFirst).headOption
    }

    ProgressManager.checkCanceled()

    if (HaskellProjectUtil.isLibraryFile(psifile)) {
      findInDeclarations
    } else if (HaskellProjectUtil.isSourceFile(psifile)) {
      ProgressManager.checkCanceled()

      val topLevelExpressions = HaskellPsiUtil.findTopLevelExpressions(psifile)

      ProgressManager.checkCanceled()

      val expressionIdentifiers = topLevelExpressions.flatMap(_.getQNameList.asScala.headOption.map(_.getIdentifierElement)).find(_.getName == name)

      ProgressManager.checkCanceled()

      if (expressionIdentifiers.isEmpty) {
        findInDeclarations
      } else {
        expressionIdentifiers
      }
    } else {
      None
    }
  }

  import scala.collection.JavaConverters._

  def findIdentifierByLocation(project: Project, virtualFile: VirtualFile, psiFile: PsiFile, lineNr: Integer, columnNr: Integer, name: String): Option[HaskellNamedElement] = {
    ProgressManager.checkCanceled()
    val namedElement = for {
      offset <- LineColumnPosition.getOffset(virtualFile, LineColumnPosition(lineNr, columnNr))
      () = ProgressManager.checkCanceled()
      element <- Option(psiFile.findElementAt(offset))
      () = ProgressManager.checkCanceled()
      namedElement <- HaskellPsiUtil.findNamedElement(element).find(_.getName == name).
        orElse {
          ProgressManager.checkCanceled()
          None
        }.orElse(HaskellPsiUtil.findHighestDeclarationElementParent(element).flatMap(_.getIdentifierElements.find(_.getName == name)).
        orElse {
          ProgressManager.checkCanceled()
          None
        }.orElse(HaskellPsiUtil.findQualifiedNameParent(element).map(_.getIdentifierElement)).find(_.getName == name)).orElse {
        HaskellPsiUtil.findTypeParent(element).flatMap(_.getQNameList.asScala.map(_.getIdentifierElement).find(_.getName == name))
      }
    } yield namedElement

    ProgressManager.checkCanceled()

    namedElement
  }

  private def sortByClassDeclarationFirst(namedElement1: HaskellNamedElement, namedElement2: HaskellNamedElement): Boolean = {
    (HaskellPsiUtil.findDeclarationElementParent(namedElement1), HaskellPsiUtil.findDeclarationElementParent(namedElement2)) match {
      case (Some(_: HaskellClassDeclaration), _) => true
      case (_, _) => false
    }
  }

  private def findIdentifiersByNameInfo(nameInfo: NameInfo, namedElement: HaskellNamedElement, project: Project): Either[NoInfo, Option[HaskellNamedElement]] = {
    ProgressManager.checkCanceled()

    nameInfo match {
      case pni: ProjectNameInfo =>
        val (virtualFile, psiFile) = HaskellProjectUtil.findFile(pni.filePath, project)
        ProgressManager.checkCanceled()
        (virtualFile, psiFile) match {
          case (Some(vf), Right(Some(pf))) => Right(findIdentifierByLocation(project, vf, pf, pni.lineNr, pni.columnNr, namedElement.getName))
          case (_, Right(_)) => Left(NoInfoAvailable(namedElement.getName, "-"))
          case (_, Left(noInfo)) => Left(noInfo)
        }
      case lni: LibraryNameInfo => findIdentifiersByLibraryNameInfo(project, lni, namedElement.getName)
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

