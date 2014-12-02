/*
 * Copyright 2014 Rik van der Kleij
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

package com.powertuple.intellij.haskell.navigate

import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.psi._
import com.intellij.psi.util.PsiTreeUtil
import com.powertuple.intellij.haskell.external._
import com.powertuple.intellij.haskell.psi._
import com.powertuple.intellij.haskell.util.HaskellElementCondition._
import com.powertuple.intellij.haskell.util.{FileUtil, HaskellEditorUtil, LineColumnPosition}
import com.powertuple.intellij.haskell.{HaskellFile, HaskellIcons}

import scala.collection.JavaConversions._

class HaskellReference(namedElement: HaskellNamedElement, textRange: TextRange) extends PsiPolyVariantReferenceBase[HaskellNamedElement](namedElement, textRange) {

  private val file = myElement.getContainingFile
  private lazy val info = GhcModiManager.findTypeInfoFor(file, myElement)

  override def multiResolve(incompleteCode: Boolean): Array[ResolveResult] = {
    val project = file.getProject

    val haskellElement = Option(PsiTreeUtil.findFirstParent(myElement, ModuleDeclarationCondition)).
        orElse(Option(PsiTreeUtil.findFirstParent(myElement, ImportDeclarationCondition)))
    haskellElement match {
      case Some(md: HaskellModuleDeclaration) =>
        Array()
      case Some(id: HaskellImportDeclaration) =>
        (for {
          filePath <- FileUtil.findModuleFilePath(id.getModuleName, project)
          file <- findFile(filePath)
        } yield new PsiElementResolveResult(file.getOriginalElement)).toArray
      case _ =>
        val identifier = ((n: String) => n.split('.').lastOption.getOrElse(n))(myElement.getName)
        val resolveResultsByGhcMod = getIdentifierInfos(file, myElement).map {
          case pii: ProjectIdentifierInfo => resolveReferences(pii, identifier)
          case lii: LibraryIdentifierInfo => resolveDeclarationReferences(lii, identifier)
          case bii: BuiltInIdentifierInfo => resolveReferences(bii, file)
        }.flatten

        displayLabelInfoMessageIfResultsContainsBuiltInDefinition(resolveResultsByGhcMod, project)

        (if (resolveResultsByGhcMod.isEmpty) {
          findLocalNamedElements.filter(_.getName == identifier).map(e => new PsiElementResolveResult(e))
        } else {
          resolveResultsByGhcMod
        }).toArray.distinct
    }
  }

  override def getVariants: Array[AnyRef] = {
    val localNamedElements = findLocalNamedElements
    val declarationNamedElements = findDeclarationElementsInFile(file).filterNot(_.getIdentifierElements.contains(myElement))
    (localNamedElements ++ declarationNamedElements).groupBy(_.getName).values.map(_.head).map(createLookupElements).flatten.toArray
  }


  private def displayLabelInfoMessageIfResultsContainsBuiltInDefinition(resolveResultsByGhcMod: Seq[ResolveResult], project: Project) {
    val firstBuiltInResolveResult = resolveResultsByGhcMod.find(_.isInstanceOf[BuiltInResolveResult])
    firstBuiltInResolveResult match {
      case Some(birs: BuiltInResolveResult) => HaskellEditorUtil.createLabelMessage(s"${birs.typeSignature} is built-in: ${birs.libraryName}:${birs.module}", project)
      case _ => ()
    }
  }

  private def createLookupElements(compositeElement: HaskellCompositeElement): Seq[LookupElementBuilder] = {
    compositeElement match {
      case ne: HaskellNamedElement => Seq(LookupElementBuilder.create(ne.getName).withIcon(HaskellIcons.HaskellSmallBlueLogo))
      case de: HaskellDeclarationElement => de.getIdentifierElements.map(ne => LookupElementBuilder.create(ne.getName).withTypeText(getTypeText(de)).withIcon(de.getPresentation.getIcon(false)))
    }
  }

  private def getTypeText(declarationElement: HaskellDeclarationElement) = {
    val presentableText = declarationElement.getPresentation.getPresentableText
    val typeSignatureDoubleColonIndex = presentableText.indexOf("::")
    if (typeSignatureDoubleColonIndex > 0) {
      presentableText.drop(typeSignatureDoubleColonIndex + 2).trim
    } else {
      presentableText
    }
  }

  private def resolveDeclarationReferences(libraryIdentifierInfo: LibraryIdentifierInfo, identifer: String): Iterable[ResolveResult] = {
    findFile(libraryIdentifierInfo.filePath).map(resolveDeclarationReferencesInFile(_, identifer)).getOrElse(Iterable())
  }

  private def resolveReferences(projectIdentifierInfo: ProjectIdentifierInfo, identifier: String): Iterable[ResolveResult] = {
    findFile(projectIdentifierInfo.filePath) match {
      case Some(_) => createResolveResultsByUsingLineColumnInfo(projectIdentifierInfo, identifier)
      case None => Iterable()
    }
  }

  private implicit object ElementOrdering extends Ordering[HaskellNamedElement] {
    override def compare(x: HaskellNamedElement, y: HaskellNamedElement): Int = {
      x.getTextOffset.compare(y.getTextOffset)
    }
  }

  private def findNamedElementFromDeclaration(psiElement: PsiElement, identifier: String) = {
    Option(PsiTreeUtil.findFirstParent(psiElement, DeclarationElementCondition)).
        map(_.asInstanceOf[HaskellDeclarationElement]).flatMap(declaration => {
      (info, declaration) match {
        case (None, dd: HaskellDataConstructorDeclarationElement) if dd.getSimpleType.getText == identifier =>
          dd.getIdentifierElements.find(_.getName == identifier).headOption
        case (Some(_), dd: HaskellDataConstructorDeclarationElement) if dd.getIdentifierElements.exists(_.getName == identifier) =>
          Some(dd.getIdentifierElements.filter(_.getName == identifier).max)
        case (_, d) => d.getIdentifierElements.find(_.getName == identifier)
      }
    })
  }

  private def createResolveResultsByUsingLineColumnInfo(projectIdentifierInfo: ProjectIdentifierInfo, identifier: String): Iterable[ResolveResult] = {
    (for {
      haskellFile <- findFile(projectIdentifierInfo.filePath)
      startOffset <- LineColumnPosition.getOffset(haskellFile, LineColumnPosition(projectIdentifierInfo.lineNr, projectIdentifierInfo.colNr))
      element <- Option(haskellFile.findElementAt(startOffset))
      namedElement <- {
        element match {
          case ne: HaskellNamedElement => Some(ne)
          case e => PsiTreeUtil.findChildrenOfType(haskellFile, classOf[HaskellNamedElement]).find(_.getTextOffset == startOffset).
              orElse(findNamedElementFromDeclaration(e, identifier))
        }
      }.filterNot(_ == myElement)
    } yield new PsiElementResolveResult(namedElement).asInstanceOf[ResolveResult]).toIterable
  }

  private def resolveReferences(builtInIdentifierInfo: BuiltInIdentifierInfo, psiFile: PsiFile): Iterable[ResolveResult] = {
    Iterable(new BuiltInResolveResult(builtInIdentifierInfo.typeSignature, builtInIdentifierInfo.libraryName, builtInIdentifierInfo.module))
  }

  private class BuiltInResolveResult(val typeSignature: String, val libraryName: String, val module: String) extends ResolveResult {
    override def getElement: PsiElement = null

    override def isValidResult: Boolean = false
  }

  private def findFile(filePath: String): Option[HaskellFile] = {
    val file = Option(LocalFileSystem.getInstance().findFileByPath(filePath))
    file match {
      case Some(f) => Option(PsiManager.getInstance(myElement.getProject).findFile(f)).map(_.asInstanceOf[HaskellFile])
      case None => None
    }
  }

  private def resolveDeclarationReferencesInFile(file: PsiFile, identifier: String): Iterable[ResolveResult] = {
    findNamedElementsEqualToIdentifier(findDeclarationElementsInFile(file).flatMap(_.getIdentifierElements), identifier).map(new PsiElementResolveResult(_))
  }

  private def findLocalNamedElements: Iterable[HaskellNamedElement] = {
    Option(PsiTreeUtil.getParentOfType(myElement, classOf[HaskellExpression])) match {
      case Some(p) => PsiTreeUtil.findChildrenOfType(p, classOf[HaskellNamedElement]).filterNot(_ == myElement).toIterable
      case None => Iterable()
    }
  }

  private def findDeclarationElementsInFile(file: PsiFile): Iterable[HaskellDeclarationElement] = {
    PsiTreeUtil.findChildrenOfType(file, classOf[HaskellDeclarationElement])
  }

  private def findNamedElementsEqualToIdentifier(namedElements: Iterable[HaskellNamedElement], identifier: String): Iterable[HaskellNamedElement] = {
    namedElements.filter(_.getName == identifier)
  }

  private def getIdentifierInfos(psiFile: PsiFile, namedElement: HaskellNamedElement): Seq[IdentifierInfo] = {
    GhcModiManager.findInfoFor(psiFile, namedElement)
  }
}