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

package com.powertuple.intellij.haskell.external

import com.intellij.openapi.editor.SelectionModel
import com.intellij.psi.{PsiElement, PsiFile}
import com.powertuple.intellij.haskell.HaskellNotificationGroup
import com.powertuple.intellij.haskell.util.{FileUtil, LineColumnPosition}

import scala.util.{Failure, Success, Try}

private[external] object GhcModiTypeInfo {
  private final val GhcModiTypeInfoPattern = """([\d]+) ([\d]+) ([\d]+) ([\d]+) "(.+)"""".r

  def findInfoFor(ghcModi: GhcModi, psiFile: PsiFile, psiElement: PsiElement): Option[TypeInfo] = {
    FileUtil.saveFile(psiFile)

    val startPositionExpression = LineColumnPosition.fromOffset(psiFile, psiElement.getTextOffset) match {
      case Some(lcp) => Some(lcp)
      case None => HaskellNotificationGroup.notifyError(s"Could not find start position for ${psiElement.getText}"); None
    }

    for {
      spe <- startPositionExpression
      typeInfos <- findGhcModiInfos(psiFile, spe)
      typeInfo <- typeInfos.find(ty => ty.startLine == spe.lineNr && ty.startColumn == spe.columnNr)
    } yield typeInfo
  }

  def findInfoForSelection(ghcModi: GhcModi, psiFile: PsiFile, selectionModel: SelectionModel): Option[TypeInfo] = {
    FileUtil.saveFile(psiFile)

    val selectionStart = LineColumnPosition.fromOffset(psiFile, selectionModel.getSelectionStart) match {
      case Some(lcp) => Some(lcp)
      case None => HaskellNotificationGroup.notifyError(s"Could not find start of selection"); None
    }

    val selectionEnd = LineColumnPosition.fromOffset(psiFile, selectionModel.getSelectionEnd) match {
      case Some(lcp) => Some(lcp)
      case None => HaskellNotificationGroup.notifyError(s"Could not find end of selection"); None
    }

    for {
      ss <- selectionStart
      se <- selectionEnd
      typeInfos <- findGhcModiInfos(psiFile, ss)
      typeInfo <- typeInfos.find(ty => ty.startLine == ss.lineNr && ty.startColumn == ss.columnNr && ty.endLine == se.lineNr && ty.endColumn == se.columnNr)
    } yield typeInfo
  }

  private def findGhcModiInfos(psiFile: PsiFile, startPositionExpression: LineColumnPosition): Option[Seq[TypeInfo]] = {
    val ghcModi = GhcModiManager.getInstance(psiFile.getProject).getGhcModi
    val vFile = psiFile.getVirtualFile
    val cmd = s"type ${vFile.getPath} ${startPositionExpression.lineNr} ${startPositionExpression.columnNr}"
    val ghcModiOutput = ghcModi.execute(cmd)

    ghcModiOutputToTypeInfo(ghcModiOutput.outputLines) match {
      case Success(typeInfos) => Some(typeInfos)
      case Failure(error) => HaskellNotificationGroup.notifyError(s"Could not determine type with $cmd. Error: $error.getMessage"); None
    }
  }

  private[external] def ghcModiOutputToTypeInfo(ghcModiOutput: Seq[String]): Try[Seq[TypeInfo]] = Try {
    for (outputLine <- ghcModiOutput) yield {
      outputLine match {
        case GhcModiTypeInfoPattern(startLn, startCol, endLine, endCol, typeSignature) =>
          TypeInfo(startLn.toInt, startCol.toInt, endLine.toInt, endCol.toInt, typeSignature)
      }
    }
  }
}

case class TypeInfo(startLine: Int, startColumn: Int, endLine: Int, endColumn: Int, typeSignature: String)
