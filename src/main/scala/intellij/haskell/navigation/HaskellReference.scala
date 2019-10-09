/*
 * Copyright 2014-2019 Rik van der Kleij
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

  private def findModule(project: Project, modId: HaskellModid) = {
    ProgressManager.checkCanceled()

    HaskellModuleNameIndex.findFilesByModuleName(project, modId.getName) match {
      case Right(files) => files.headOption.map(HaskellFileResolveResult)
      case Left(noInfo) => Some(NoResolveResult(noInfo))
    }
  }

  private def findQualifierDeclaration(project: Project, psiFile: PsiFile, qualifier: HaskellQualifierElement) = {
    ProgressManager.checkCanceled()

    val importDeclarations = HaskellPsiUtil.findImportDeclarations(psiFile)
    findQualifier(importDeclarations, qualifier) match {
      case Some(ne) => Some(HaskellNamedElementResolveResult(ne))
      case None => None
        ProgressManager.checkCanceled()

        findHaskellFile(importDeclarations, qualifier, project) match {
          case Right(r) => r.map(HaskellFileResolveResult)
          case Left(noInfo) => Some(NoResolveResult(noInfo))
        }
    }
  }

  private def findImportedIdentifierDeclaration(project: Project, psiFile: PsiFile, namedElement: HaskellNamedElement) = {
    ProgressManager.checkCanceled()

    HaskellPsiUtil.findImportDeclaration(namedElement) match {
      case Some(d) =>
        val importQualifier = Option(d.getImportQualifiedAs).map(_.getQualifier.getName).orElse(d.getModuleName)
        ProgressManager.checkCanceled()

        resolveReference(namedElement, psiFile, project, importQualifier) match {
          case Right(r) => Some(HaskellNamedElementResolveResult(r))
          case Left(_) => None
        }
      case None => None
    }
  }

  private def isPartOfModId(namedElement: HaskellNamedElement): Boolean = {
    Option(namedElement.getParent).map(_.getNode.getElementType).contains(HaskellTypes.HS_MODID)
  }

  private def isPartOfQualifier(namedElement: HaskellNamedElement): Boolean = {
    Option(namedElement.getParent).map(_.getNode.getElementType).contains(HaskellTypes.HS_QUALIFIER)
  }

  private def isPartOfQualifiedAs(namedElement: HaskellNamedElement): Boolean = {
    Option(namedElement.getParent).map(_.getNode.getElementType).contains(HaskellTypes.HS_IMPORT_QUALIFIED_AS)
  }

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
        case q: HaskellQualifierElement if isPartOfQualifiedAs(q) => Some(HaskellNamedElementResolveResult(q))
        case q: HaskellQualifierElement => findQualifierDeclaration(project, psiFile, q)
        case mid: HaskellModid => findModule(project, mid)
        case ne: HaskellNamedElement if isPartOfQualifier(ne) | isPartOfQualifiedAs(ne) | isPartOfModId(ne) => None
        case ne: HaskellNamedElement =>
          ProgressManager.checkCanceled()

          findImportedIdentifierDeclaration(project, psiFile, ne).orElse {

            HaskellPsiUtil.findTypeSignatureDeclaration(ne) match {
              case None => resolveReference(ne, psiFile, project = project, None) match {
                case Right(r) => Some(HaskellNamedElementResolveResult(r))
                case Left(noInfo) => Some(NoResolveResult(noInfo))
              }
              case Some(ts) =>

                def find(e: PsiElement): Option[HaskellNamedElement] = {
                  Option(PsiTreeUtil.findSiblingForward(e, HaskellTypes.HS_TOP_DECLARATION_LINE, null)) match {
                    case Some(d) if Option(d.getFirstChild).flatMap(c => Option(c.getFirstChild)).exists(_.isInstanceOf[HaskellExpression]) => HaskellPsiUtil.findNamedElements(d).headOption.find(_.getName == ne.getName)
                    case _ => None
                  }
                }

                ProgressManager.checkCanceled()

                // For not exported identifiers the definition location for the type signature has to be resolved "manually".
                // Making no exception and doing this manually resolving for all type signatures.
                Option(ts.getParent).flatMap(p => Option(p.getParent)) match {
                  case Some(p) =>
                    find(p) match {
                      case Some(ee) => Some(HaskellNamedElementResolveResult(ee))
                      case None => resolveReference(ne, psiFile, project, None) match {
                        case Right(r) => Some(HaskellNamedElementResolveResult(r))
                        case Left(noInfo) => Some(NoResolveResult(noInfo))
                      }
                    }
                  case None => resolveReference(ne, psiFile, project, None) match {
                    case Right(r) => Some(HaskellNamedElementResolveResult(r))
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

  private def resolveReference(namedElement: HaskellNamedElement, psiFile: PsiFile, project: Project, importQualifier: Option[String]): Either[NoInfo, HaskellNamedElement] = {
    ProgressManager.checkCanceled()

    def noInfo = {
      NoInfoAvailable(ApplicationUtil.runReadAction(namedElement.getName), psiFile.getName)
    }

    HaskellPsiUtil.findQualifiedName(namedElement) match {
      case Some(qualifiedNameElement) =>
        ProgressManager.checkCanceled()
        resolveReferenceByDefinitionLocation(qualifiedNameElement, psiFile, importQualifier)
      case None => Left(noInfo)
    }
  }

  private def resolveReferenceByDefinitionLocation(qualifiedNameElement: HaskellQualifiedNameElement, psiFile: PsiFile, importQualifier: Option[String]): Either[NoInfo, HaskellNamedElement] = {
    ProgressManager.checkCanceled()

    HaskellComponentsManager.findDefinitionLocation(psiFile, qualifiedNameElement, importQualifier) match {
      case Right(PackageModuleLocation(_, ne, _, _)) => Right(ne)
      case Right(LocalModuleLocation(_, ne, _)) => Right(ne)
      case Left(noInfo) => Left(noInfo)
    }
  }

  private def findQualifier(importDeclarations: Iterable[HaskellImportDeclaration], qualifierElement: HaskellQualifierElement): Option[HaskellNamedElement] = {
    importDeclarations.flatMap(id => Option(id.getImportQualifiedAs)).flatMap(iqa => Option(iqa.getQualifier)).find(_.getName == qualifierElement.getName)
  }

  private def findHaskellFile(importDeclarations: Iterable[HaskellImportDeclaration], qualifierElement: HaskellNamedElement, project: Project): Either[NoInfo, Option[PsiFile]] = {
    (for {
      id <- importDeclarations.find(_.getModuleName.contains(qualifierElement.getName))
      mn <- id.getModuleName
    } yield HaskellModuleNameIndex.findFilesByModuleName(project, mn)) match {
      case Some(Right(files)) => Right(files.headOption)
      case Some(Left(noInfo)) => Left(noInfo)
      case None => Right(None)
    }
  }
}

object HaskellReference {

  import scala.jdk.CollectionConverters._

  def resolveInstanceReferences(project: Project, namedElement: HaskellNamedElement, nameInfos: Iterable[NameInfoComponentResult.NameInfo]): Seq[HaskellNamedElement] = {
    val identifiers = nameInfos.map(ni => findIdentifiersByNameInfo(ni, namedElement, project)).toSeq.distinct
    if (identifiers.contains(Left(ReadActionTimeout))) {
      HaskellEditorUtil.showStatusBarMessage(project, "Navigating to instance declarations is not available at this moment")
      Seq()
    } else {
      identifiers.flatMap(_.toOption.map(_._2))
    }
  }

  def findIdentifiersByLibraryNameInfo(project: Project, libraryNameInfo: LibraryNameInfo, name: String): Either[NoInfo, (String, HaskellNamedElement)] = {
    findIdentifiersByModulesAndName(project, Seq(libraryNameInfo.moduleName), name)
  }

  def findIdentifiersByModulesAndName(project: Project, moduleNames: Seq[String], name: String, prioIdInExpression: Boolean = true): Either[NoInfo, (String, HaskellNamedElement)] = {
    ProgressManager.checkCanceled()

    findIdentifiersByModuleAndName2(project, moduleNames, name, prioIdInExpression).headOption.map {
      case (mn, nes) => nes.headOption.map(ne => Right((mn, ne))).getOrElse(Left(NoInfoAvailable(name, moduleNames.mkString(" | "))))
    }.getOrElse(Left(ModuleNotAvailable(moduleNames.mkString(" | "))))
  }

  private def findIdentifiersByModuleAndName2(project: Project, moduleNames: Seq[String], name: String, prioIdInExpression: Boolean): Seq[(String, Seq[HaskellNamedElement])] = {
    ProgressManager.checkCanceled()

    moduleNames.distinct.flatMap(mn => HaskellModuleNameIndex.findFilesByModuleName(project, mn) match {
      case Left(_) => Seq()
      case Right(files) =>
        ProgressManager.checkCanceled()

        val identifiers = files.flatMap(f => findIdentifierInFileByName(f, name, prioIdInExpression))
        if (identifiers.isEmpty) {
          val importedModuleNames = files.flatMap(f => FileModuleIdentifiers.findAvailableModuleIdentifiers(f).filter(mid => mid.name == name || mid.name == "_" + name || mid.name == mid.moduleName + "." + name).map(_.moduleName))
          if (importedModuleNames.isEmpty) {
            Seq()
          } else {
            findIdentifiersByModuleAndName2(project, importedModuleNames, name, prioIdInExpression)
          }
        } else {
          Seq((mn, identifiers))
        }
    })
  }

  def findIdentifierInFileByName(psiFile: PsiFile, name: String, prioIdInExpression: Boolean): Option[HaskellNamedElement] = {

    def findIdInExpressions = {
      ProgressManager.checkCanceled()

      val topLevelExpressions = HaskellPsiUtil.findTopLevelExpressions(psiFile)

      ProgressManager.checkCanceled()

      topLevelExpressions.flatMap(_.getQNameList.asScala.headOption.map(_.getIdentifierElement)).find(_.getName == name)
    }

    def findIdInDeclarations = {
      ProgressManager.checkCanceled()

      val declarationElements = HaskellPsiUtil.findHaskellDeclarationElements(psiFile)

      ProgressManager.checkCanceled()

      val declarationIdentifiers = declarationElements.flatMap(_.getIdentifierElements).filter(d => d.getName == name || d.getName == "_" + name)

      ProgressManager.checkCanceled()

      declarationIdentifiers.toSeq.sortWith(sortByClassDeclarationFirst).headOption
    }

    if (prioIdInExpression) {
      val expressionIdentifiers = findIdInExpressions
      if (expressionIdentifiers.isEmpty) {
        findIdInDeclarations
      } else {
        expressionIdentifiers
      }
    } else {
      val declarationIdentifiers = findIdInDeclarations
      if (declarationIdentifiers.isEmpty) {
        findIdInExpressions
      } else {
        declarationIdentifiers
      }
    }
  }

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
        }.orElse(HaskellPsiUtil.findHighestDeclarationElement(element).flatMap(_.getIdentifierElements.find(_.getName == name)).
        orElse {
          ProgressManager.checkCanceled()
          None
        }.orElse(HaskellPsiUtil.findQualifiedName(element).map(_.getIdentifierElement)).find(_.getName == name)).orElse {
        HaskellPsiUtil.findTtype(element).flatMap(_.getQNameList.asScala.map(_.getIdentifierElement).find(_.getName == name))
      }
    } yield namedElement

    ProgressManager.checkCanceled()

    namedElement
  }

  private def sortByClassDeclarationFirst(namedElement1: HaskellNamedElement, namedElement2: HaskellNamedElement): Boolean = {
    (HaskellPsiUtil.findDeclarationElement(namedElement1), HaskellPsiUtil.findDeclarationElement(namedElement2)) match {
      case (Some(_: HaskellClassDeclaration), _) => true
      case (_, _) => false
    }
  }

  def findIdentifiersByNameInfo(nameInfo: NameInfo, namedElement: HaskellNamedElement, project: Project): Either[NoInfo, (Option[String], HaskellNamedElement, Option[String])] = {
    ProgressManager.checkCanceled()

    val name = namedElement.getName
    nameInfo match {
      case pni: ProjectNameInfo =>
        val (virtualFile, psiFile) = HaskellFileUtil.findFileInRead(project, pni.filePath)
        ProgressManager.checkCanceled()
        (virtualFile, psiFile) match {
          case (Some(vf), Right(pf)) => findIdentifierByLocation(project, vf, pf, pni.lineNr, pni.columnNr, name).map(r => Right(HaskellPsiUtil.findModuleName(pf), r, None)).getOrElse(Left(NoInfoAvailable(name, "-")))
          case (_, Right(_)) => Left(NoInfoAvailable(name, "-"))
          case (_, Left(noInfo)) => Left(noInfo)
        }
      case lni: LibraryNameInfo => findIdentifiersByLibraryNameInfo(project, lni, name).map({ case (mn, nes) => (Some(mn), nes, lni.packageName) })
      case _ => Left(NoInfoAvailable(name, "-"))
    }
  }
}

case class HaskellNamedElementResolveResult(element: HaskellNamedElement) extends PsiElementResolveResult(element)

case class HaskellFileResolveResult(element: PsiElement) extends PsiElementResolveResult(element)

case class NoResolveResult(noInfo: NoInfo) extends ResolveResult {
  override def getElement: PsiElement = null

  override def isValidResult: Boolean = false
}

