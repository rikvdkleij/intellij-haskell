/*
 * Copyright 2014 Rik van der Kleij

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

package com.powertuple.intellij.haskell.psi

import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.psi._
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.{PsiTreeUtil, PsiUtilCore}
import com.powertuple.intellij.haskell.external.GhciModManager
import com.powertuple.intellij.haskell.util.{FileUtil, HaskellFileIndex, LineColumnPosition, ProjectUtil}
import com.powertuple.intellij.haskell.{HaskellFile, HaskellIcons, HaskellNotificationGroup}

import scala.util.{Failure, Success, Try}

class HaskellVarReference(element: HaskellVar, textRange: TextRange) extends PsiReferenceBase[HaskellVar](element, textRange) {

  override def resolve: PsiElement = {
    val psiFile = myElement.getContainingFile

    // Skip library files
    if (!ProjectUtil.isProjectFile(psiFile)) {
      return null;
    }

    FileUtil.saveAllFiles()

    val expression = myElement.getText.substring(textRange.getStartOffset, textRange.getEndOffset)

    (for {
      expressionInfo <- getExpressionInfo(psiFile, expression)
      haskellFile <- Option(PsiManager.getInstance(myElement.getProject).
          findFile(LocalFileSystem.getInstance().findFileByPath(expressionInfo.filePath)).asInstanceOf[HaskellFile])
      typeSignature <- findTypeSignaturesFor(haskellFile, expression)
    } yield typeSignature).orElse(for {
      expressionInfo <- getLocalExpressionInfo(getExpressionInfo(psiFile, expression))
      haskellFile <- Option(PsiManager.getInstance(myElement.getProject).
          findFile(LocalFileSystem.getInstance().findFileByPath(expressionInfo.filePath)).asInstanceOf[HaskellFile])
      startOffset <- LineColumnPosition.getOffset(haskellFile, LineColumnPosition(expressionInfo.lineNr, expressionInfo.colNr))
    } yield PsiUtilCore.getElementAtOffset(haskellFile, startOffset).getParent.asInstanceOf[HaskellVar]).orNull
  }

  /**
   * Did not find solution to take scope into account, so currently it returns all vars of file :-(
   */
  override def getVariants: Array[AnyRef] = {
    val haskellFile = myElement.getContainingFile.asInstanceOf[HaskellFile]
    val vars = PsiTreeUtil.getChildrenOfType(haskellFile, classOf[HaskellVar]).filter(v => v != null && v.getName != null && !v.getName.isEmpty).groupBy(_.getName).map(_._2.head).toArray
    vars.map(v => LookupElementBuilder.create(v).withIcon(HaskellIcons.HASKELL_SMALL_LOGO).withTypeText(v.getName))
  }

  private def getLocalExpressionInfo(expressionInfo: Option[ExpressionInfo]) = {
    expressionInfo match {
      case Some(lei: LocalExpressionInfo) => Some(lei)
      case _ => None
    }
  }

  private def findTypeSignaturesFor(haskellFile: HaskellFile, expression: String) = {
    Option(PsiTreeUtil.getChildrenOfType(haskellFile, classOf[HaskellStartTypeSignature])) match {
      case Some(typeSignatures) => typeSignatures.map(ts => ts.getFirstChild).find(v => v.getText == expression)
      case _ => None
    }
  }

  private def getOutputLine(outputLines: Seq[String]): Option[String] = {
    outputLines match {
      case Seq(ol) => Some(ol)
      case _ => None
    }
  }

  private def getExpressionInfo(psiFile: PsiFile, expression: String): Option[ExpressionInfo] = {
    val ghcModi = GhciModManager.getGhcMod(psiFile.getProject)
    val cmd = s"info ${psiFile.getVirtualFile.getPath} $expression"
    val ghcModiOutput = ghcModi.execute(cmd)

    for {
      outputLine <- getOutputLine(ghcModiOutput.outputLines)
      expressionInfo <- expressionInfoFrom(outputLine)
    } yield expressionInfo
  }

  private def expressionInfoFrom(outputLine: String): Option[ExpressionInfo] = {
    if (outputLine == "Cannot show info") {
      None
    } else {
      ghcModiOutputToExpressionInfo(outputLine) match {
        case Success(ei) => Some(ei)
        case Failure(error) => if (error.getMessage == null) {
          throw error
        } else HaskellNotificationGroup.notifyInfo(error.getMessage);
          None
      }
    }
  }

  private def ghcModiOutputToExpressionInfo(ghcModiOutput: String): Try[ExpressionInfo] = Try {
    val GhcModiInfoPattern = """(.+)-- Defined at (.+):([\d]+):([\d]+)""".r
    val GhcModiInfoLibraryPathPattern = """(.+)-- Defined in ‘(.+):(.*)’.*""".r
    val GhcModiInfoLibraryPattern = """(.+)-- Defined in ‘(.+)’""".r

    ghcModiOutput match {
      case GhcModiInfoPattern(typeSignature, filePath, lineNr, colNr) => LocalExpressionInfo(typeSignature.trim, filePath, lineNr.toInt, colNr.toInt)
      case GhcModiInfoLibraryPathPattern(typeSignature, libraryName, filePath) => LibraryExpressionInfo(typeSignature.trim, findLibraryFilePath(filePath))
      case GhcModiInfoLibraryPattern(typeSignature, modulePath) => LibraryExpressionInfo(typeSignature, findLibraryFilePath(modulePath))
      case _ => throw new Exception(s"Unknown pattern for ghc-modi info output: $ghcModiOutput")
    }
  }

  private def findLibraryFilePath(modulePath: String) = {
    val (fileName, dirs) = modulePath.split('.').toList.reverse match {
      case fn :: d => (fn, d)
      case _ => throw new Exception(s"Could not determine file and directory for $modulePath")
    }

    val files = HaskellFileIndex.getFilesByName(myElement.getProject, fileName, GlobalSearchScope.allScope(myElement.getProject))
    val file = files.find(hf => checkPath(hf.getContainingDirectory, dirs)).getOrElse(throw new Exception(s"Could not find file path for $modulePath"))
    file.getVirtualFile.getPath
  }

  private def checkPath(dir: PsiDirectory, dirNames: List[String]): Boolean = {
    dirNames match {
      case h :: t if dir.getName == h => checkPath(dir.getParentDirectory, t)
      case h :: t => false
      case _ => true
    }
  }

  abstract class ExpressionInfo {
    def typeSignature: String

    def filePath: String
  }

  case class LocalExpressionInfo(typeSignature: String, filePath: String, lineNr: Int, colNr: Int) extends ExpressionInfo

  case class LibraryExpressionInfo(typeSignature: String, filePath: String) extends ExpressionInfo

}