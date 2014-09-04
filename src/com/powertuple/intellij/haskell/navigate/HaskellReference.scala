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
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.psi._
import com.intellij.psi.util.PsiTreeUtil
import com.powertuple.intellij.haskell.external._
import com.powertuple.intellij.haskell.psi._
import com.powertuple.intellij.haskell.util.FileUtil
import com.powertuple.intellij.haskell.{HaskellFile, HaskellIcons, HaskellNotificationGroup}

import scala.collection.JavaConversions._

class HaskellReference(element: HaskellNamedElement, textRange: TextRange) extends PsiPolyVariantReferenceBase[HaskellNamedElement](element, textRange) {

  override def multiResolve(incompleteCode: Boolean): Array[ResolveResult] = {
    val psiFile = myElement.getContainingFile

    FileUtil.saveAllFiles()

    val expression = myElement.getText.substring(textRange.getStartOffset, textRange.getEndOffset)
    findResolveResultByDeclaration(psiFile.asInstanceOf[HaskellFile], expression).toArray

    // else {
    //    val bla = getExpressionInfos(psiFile, expression).map {
    //        case pei: ProjectExpressionInfo =>
    //          for {
    //            haskellFile <- findFile(pei.filePath)
    //            startOffset <- LineColumnPosition.getOffset(haskellFile, LineColumnPosition(pei.lineNr, pei.colNr))
    //          } yield new PsiElementResolveResult(PsiUtilCore.getElementAtOffset(haskellFile, startOffset))
    //        case lei: LibraryExpressionInfo =>
    //          findFile(lei.filePath).flatMap(findResolveResultByDeclaration(_, expression))
    //        case bie: BuiltInExpressionInfo =>
    //          if (bie.declarationDefinition) {
    //            HaskellEditorUtil.createLabelMessage(s"${bie.typeSignature} is built-in: ${bie.libraryName}:${bie.module}", psiFile.getProject)
    //          }
    //          None
    //      }.flatten.toArray
    //      bla
    //    }
  }

  override def getVariants: Array[AnyRef] = {
    val haskellFile = myElement.getContainingFile.asInstanceOf[HaskellFile]
    val declarations = PsiTreeUtil.findChildrenOfType(haskellFile, classOf[HaskellDeclarationElement])

    declarations.map {
      case d: HaskellDataDeclaration => Seq(createLookupElement(d)) ++ d.getConstrs.getConstrList.map(e => createLookupElement(e))
      case d => Seq(createLookupElement(d))
    }.flatten.toArray
  }

  private def findFile(filePath: String): Option[HaskellFile] = {
    Option(PsiManager.getInstance(myElement.getProject).findFile(LocalFileSystem.getInstance().findFileByPath(filePath)).asInstanceOf[HaskellFile])
  }

  private def createLookupElement(declarationElement: HaskellDeclarationElement) = {
    LookupElementBuilder.create(declarationElement.getIdentifierElement.getName).withTypeText(declarationElement.getText).withIcon(HaskellIcons.HaskellSmallLogo)
  }

  private def createLookupElement(constr: HaskellConstr) = {
    LookupElementBuilder.create(constr.getIdentifierElement.getName).withTypeText(constr.getText).withIcon(HaskellIcons.HaskellSmallLogo)
  }

  private def findResolveResultByDeclaration(haskellFile: HaskellFile, expression: String): Iterable[PsiElementResolveResult] = {
    val re = Option(PsiTreeUtil.findChildrenOfType(haskellFile, classOf[HaskellDeclarationElement])) match {
      case Some(declarationElements) => declarationElements.flatMap(de => findNamedElements(de, expression)).map(de => new PsiElementResolveResult(de))
      case _ => HaskellNotificationGroup.notifyError(s"Could not find declaration for `$expression` in file `${haskellFile.getVirtualFile.getPath}`"); Iterable()
    }
    re
  }

  private def findNamedElements(declarationElement: HaskellDeclarationElement, expression: String): Seq[HaskellNamedElement] = {
    declarationElement match {
      case dd: HaskellDataDeclaration => Option(dd.getSimpletype.getIdentifierElement).filter(_.getName == expression).toSeq ++ Option(dd.getConstrs).map(_.getConstrList.map(_.getIdentifierElement).filter(_.getName == expression)).getOrElse(Seq())
      case _@de => Seq(de).map(_.getIdentifierElement).filter(_.getName == expression)
    }
  }

  private def getExpressionInfos(psiFile: PsiFile, expression: String): Seq[ExpressionInfo] = {
    GhcModiManager.findInfoFor(psiFile, expression)
  }
}