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

package com.powertuple.intellij.haskell.actions

import com.intellij.openapi.actionSystem.{AnAction, AnActionEvent, CommonDataKeys}
import com.intellij.openapi.editor.Editor
import com.intellij.psi.util.PsiUtilBase
import com.intellij.psi.{PsiElement, PsiFile}
import com.powertuple.intellij.haskell.external.GhcModiManager
import com.powertuple.intellij.haskell.util.{ProjectUtil, FileUtil, LineColumnPosition}
import com.powertuple.intellij.haskell.{HaskellFile, HaskellLanguage, HaskellNotificationGroup}

import scala.util.{Failure, Success, Try}

class ShowTypeAction extends AnAction {

  override def update(e: AnActionEvent) {
    HaskellActionUtil.enableAndShowIfInHaskellFile(e)
  }

  def actionPerformed(e: AnActionEvent) {
    val context = e.getDataContext
    val editor = CommonDataKeys.EDITOR.getData(context)

    if (editor == null) return

    val psiFile = PsiUtilBase.getPsiFileInEditor(editor, CommonDataKeys.PROJECT.getData(context))

    // Skip library files
    if (!ProjectUtil.isProjectFile(psiFile)) {
      return;
    }

    if (psiFile.getLanguage != HaskellLanguage.INSTANCE) return
    FileUtil.saveFile(psiFile)

    val startPositionExpression = findStartOfExpression(editor, psiFile) match {
      case Some(lcp) => lcp
      case None => HaskellNotificationGroup.notifyError("Could not find start position of expression"); return
    }

    val ghcModi = GhcModiManager.getInstance(psiFile.getProject).getGhcMod
    val vFile = psiFile.getVirtualFile
    val cmd = s"type ${vFile.getPath} ${startPositionExpression.lineNr} ${startPositionExpression.colunmNr}"
    val ghcModiOutput = ghcModi.execute(cmd)

    val typeInfo = ghcModiOutputToTypeInfo(ghcModiOutput.outputLines) match {
      case Success(typeInfos) => typeInfos.find(ty => ty.startLine == startPositionExpression.lineNr && ty.startColumn == startPositionExpression.colunmNr)
      case Failure(error) => HaskellNotificationGroup.notifyError(s"Could not determine type with $cmd. Error: $error.getMessage"); None
    }

    typeInfo match {
      case Some(ti) => HaskellActionUtil.showHint(editor, ti.typeSignature)
      case None => HaskellActionUtil.showHint(editor, "Could not determine type of expression")
    }
  }

  private def findStartOfExpression(editor: Editor, psiFile: PsiFile): Option[LineColumnPosition] = {
    val offset = editor.getCaretModel.getOffset
    val element = psiFile.findElementAt(offset)

    def findTopExpressionElement(element: PsiElement): PsiElement = {
      if (element.getParent.isInstanceOf[HaskellFile]) {
        element
      } else {
        findTopExpressionElement(element.getParent)
      }
    }
    LineColumnPosition.fromOffset(psiFile, findTopExpressionElement(element).getTextRange.getStartOffset)
  }

  private[actions] def ghcModiOutputToTypeInfo(ghcModiOutput: Seq[String]): Try[Seq[TypeInfo]] = Try {
    val ghcModiTypeInfoPattern = """([\d]+) ([\d]+) ([\d]+) ([\d]+) "(.+)"""".r
    for (outputLine <- ghcModiOutput) yield {
      outputLine match {
        case ghcModiTypeInfoPattern(startLn, startCol, endLine, endCol, typeSignature) =>
          TypeInfo(startLn.toInt, startCol.toInt, endLine.toInt, endCol.toInt, typeSignature)
      }
    }
  }
}

case class TypeInfo(startLine: Int, startColumn: Int, endLine: Int, endColumn: Int, typeSignature: String)