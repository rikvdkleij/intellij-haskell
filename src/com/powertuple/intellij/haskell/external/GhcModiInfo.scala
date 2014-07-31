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

package com.powertuple.intellij.haskell.external

import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.{PsiDirectory, PsiFile}
import com.powertuple.intellij.haskell.HaskellNotificationGroup
import com.powertuple.intellij.haskell.util.HaskellFileIndex

import scala.util.{Failure, Success, Try}

private[external] object GhcModiInfo {

  val GhcModiInfoPattern = """(.+)-- Defined at (.+):([\d]+):([\d]+)""".r
  val GhcModiInfoLibraryPathPattern = """(.+)-- Defined in ‘(.+):(.*)’.*""".r
  val GhcModiInfoLibraryPattern = """(.+)-- Defined in ‘(.+)’""".r

  def findInfoFor(ghcModi: GhcModi, psiFile: PsiFile, expression: String): Option[ExpressionInfo] = {
    val cmd = s"info ${psiFile.getVirtualFile.getPath} $expression"
    val ghcModiOutput = ghcModi.execute(cmd)

    for {
      outputLine <- getSingleLine(ghcModiOutput.outputLines)
      expressionInfo <- expressionInfoFrom(outputLine, psiFile.getProject)
    } yield expressionInfo
  }

  private def getSingleLine(outputLines: Seq[String]) = {
    outputLines match {
      case Seq(ol) => Some(ol)
      case _ => None
    }
  }

  private def expressionInfoFrom(outputLine: String, project: Project): Option[ExpressionInfo] = {
    if (outputLine == "Cannot show info") {
      None
    } else {
      ghcModiOutputToExpressionInfo(outputLine, project) match {
        case Success(ei) => Some(ei)
        case Failure(error) => if (error.getMessage == null) {
          throw error
        } else {
          HaskellNotificationGroup.notifyInfo(error.getMessage)
          None
        }
      }
    }
  }

  private def ghcModiOutputToExpressionInfo(outputLine: String, project: Project): Try[ExpressionInfo] = Try {
    outputLine match {
      case GhcModiInfoPattern(typeSignature, filePath, lineNr, colNr) => ProjectExpressionInfo(typeSignature.trim, filePath, lineNr.toInt, colNr.toInt)
      case GhcModiInfoLibraryPathPattern(typeSignature, libraryName, module) => LibraryExpressionInfo(typeSignature.trim, findLibraryFilePath(module, project), module)
      case GhcModiInfoLibraryPattern(typeSignature, module) => LibraryExpressionInfo(typeSignature, findLibraryFilePath(module, project), module)
      case _ => throw new Exception(s"Unknown pattern for ghc-modi info output: $outputLine")
    }
  }

  private def findLibraryFilePath(module: String, project: Project) = {
    val (fileName, dirs) = module.split('.').toList.reverse match {
      case fn :: d => (fn, d)
      case _ => throw new Exception(s"Could not determine file and directory for $module")
    }

    val files = HaskellFileIndex.getFilesByName(project, fileName, GlobalSearchScope.allScope(project))
    val file = files.find(hf => checkPath(hf.getContainingDirectory, dirs)).getOrElse(throw new Exception(s"Could not find file path for $module"))
    file.getVirtualFile.getPath
  }

  private def checkPath(dir: PsiDirectory, dirNames: List[String]): Boolean = {
    dirNames match {
      case h :: t if dir.getName == h => checkPath(dir.getParentDirectory, t)
      case h :: t => false
      case _ => true
    }
  }
}

abstract class ExpressionInfo {
  def typeSignature: String

  def filePath: String
}

case class ProjectExpressionInfo(typeSignature: String, filePath: String, lineNr: Int, colNr: Int) extends ExpressionInfo

case class LibraryExpressionInfo(typeSignature: String, filePath: String, module: String) extends ExpressionInfo
