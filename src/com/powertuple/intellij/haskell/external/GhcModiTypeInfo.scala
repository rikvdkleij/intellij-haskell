package com.powertuple.intellij.haskell.external

import com.intellij.psi.{PsiElement, PsiFile}
import com.powertuple.intellij.haskell.util.{FileUtil, LineColumnPosition, ProjectUtil}
import com.powertuple.intellij.haskell.{HaskellFile, HaskellLanguage, HaskellNotificationGroup}

import scala.util.{Failure, Success, Try}

private[external] object GhcModiTypeInfo {
  private final val GhcModiTypeInfoPattern = """([\d]+) ([\d]+) ([\d]+) ([\d]+) "(.+)"""".r

  def findInfoFor(ghcModi: GhcModi, psiFile: PsiFile, psiElement: PsiElement): Option[TypeInfo] = {

    // Skip library files because ghc-mod(i) will not support them.
    if (!ProjectUtil.isProjectFile(psiFile) || (psiFile.getLanguage != HaskellLanguage.INSTANCE)) {
      return None;
    }

    FileUtil.saveFile(psiFile)

    val startPositionExpression = findStartOfExpression(psiElement, psiFile) match {
      case Some(lcp) => Some(lcp)
      case None => HaskellNotificationGroup.notifyError(s"Could not find start position for ${psiElement.getText}"); None
    }

    for {
      spe <- startPositionExpression
      typeInfos <- findGhcModiInfos(psiFile, spe)
      typeInfo <- typeInfos.find(ty => ty.startLine == spe.lineNr && ty.startColumn == spe.colunmNr)
    } yield typeInfo
  }

  private def findGhcModiInfos(psiFile: PsiFile, startPositionExpression: LineColumnPosition): Option[Seq[TypeInfo]] = {
    val ghcModi = GhcModiManager.getInstance(psiFile.getProject).getGhcModi
    val vFile = psiFile.getVirtualFile
    val cmd = s"type ${vFile.getPath} ${startPositionExpression.lineNr} ${startPositionExpression.colunmNr}"
    val ghcModiOutput = ghcModi.execute(cmd)

    ghcModiOutputToTypeInfo(ghcModiOutput.outputLines) match {
      case Success(typeInfos) => Some(typeInfos)
      case Failure(error) => HaskellNotificationGroup.notifyError(s"Could not determine type with $cmd. Error: $error.getMessage"); None
    }
  }

  private def findStartOfExpression(psiElement: PsiElement, psiFile: PsiFile): Option[LineColumnPosition] = {

    def findTopExpressionElement(element: PsiElement): PsiElement = {
      if (element.getParent.isInstanceOf[HaskellFile]) {
        element
      } else {
        findTopExpressionElement(element.getParent)
      }
    }
    LineColumnPosition.fromOffset(psiFile, findTopExpressionElement(psiElement).getTextRange.getStartOffset)
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
