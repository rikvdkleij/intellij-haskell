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

package intellij.haskell.editor

import com.intellij.codeInsight.daemon.impl.DaemonCodeAnalyzerImpl
import com.intellij.lang.ImportOptimizer
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.psi.TokenType.WHITE_SPACE
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.{PsiElement, PsiFile}
import intellij.haskell.HaskellFile
import intellij.haskell.psi.HaskellPsiUtil
import intellij.haskell.psi.HaskellTypes._
import intellij.haskell.util.{HaskellFileUtil, HaskellProjectUtil, LineColumnPosition, ScalaUtil}

import scala.jdk.CollectionConverters._
import scala.util.matching.Regex

class HaskellImportOptimizer extends ImportOptimizer {

  override def supports(psiFile: PsiFile): Boolean = psiFile.isInstanceOf[HaskellFile] && HaskellProjectUtil.isSourceFile(psiFile)

  override def processFile(psiFile: PsiFile): Runnable = {
    () => HaskellImportOptimizer.removeRedundantImports(psiFile)
  }
}

object HaskellImportOptimizer {
  final val WarningRedundantImport: Regex = """.*The (?:qualified )?import of [`|‘]([^'’]+)['|’] is redundant.*""".r
  final val WarningRedundant2Import: Regex = """.*The (?:qualified )?import of [`|‘]([^'’]+)['|’] from module [`|‘]([^'’]+)['|’] is redundant.*""".r

  def removeRedundantImports(psiFile: PsiFile): Boolean = {
    val document = HaskellFileUtil.findDocument(psiFile)
    val warnings = document.map(d => DaemonCodeAnalyzerImpl.getHighlights(d, HighlightSeverity.WARNING, psiFile.getProject)).map(_.asScala).getOrElse(Seq())

    warnings.foreach(w => w.getDescription match {
      case HaskellImportOptimizer.WarningRedundantImport(mn) => removeRedundantImport(psiFile, mn, getLineNr(psiFile, w.getStartOffset))
      case HaskellImportOptimizer.WarningRedundant2Import(idNames, mn) => removeRedundantImportIds(psiFile, mn, idNames.split(',').toSeq.map(_.trim), getLineNr(psiFile, w.getStartOffset))
      case _ => ()
    })
    true
  }

  private def getLineNr(psiFile: PsiFile, element: PsiElement) = {
    val offset = element.getTextRange.getStartOffset
    LineColumnPosition.fromOffset(psiFile.getVirtualFile, offset).map(_.lineNr)
  }

  private def getLineNr(psiFile: PsiFile, offset: Int) = {
    LineColumnPosition.fromOffset(psiFile.getVirtualFile, offset).map(_.lineNr)
  }

  def removeRedundantImport(psiFile: PsiFile, moduleName: String, lineNr: Option[Int]): Unit = {
    HaskellPsiUtil.findImportDeclarations(psiFile).find(d => d.getModuleName.contains(moduleName) && getLineNr(psiFile, d) == lineNr).foreach { importDeclaration =>

      val spaces = Option(PsiTreeUtil.findSiblingForward(importDeclaration, WHITE_SPACE, true, null))
      val newline = spaces.flatMap(s => Option(PsiTreeUtil.findSiblingForward(s, HS_NEWLINE, true, null)))
      WriteCommandAction.runWriteCommandAction(psiFile.getProject, ScalaUtil.computable {
        spaces.foreach(_.delete())
        newline.foreach(_.delete())
        importDeclaration.delete()
      })
    }
  }

  import intellij.haskell.psi.HaskellTypes._

  def removeRedundantImportIds(psiFile: PsiFile, moduleName: String, idNames: Seq[String], lineNr: Option[Int]): Unit = {
    HaskellPsiUtil.findImportDeclarations(psiFile).find(d => d.getModuleName.contains(moduleName) && getLineNr(psiFile, d) == lineNr).foreach { importDeclaration =>
      val prefix = Option(importDeclaration.getImportQualifiedAs).map(_.getQualifier.getName).orElse(importDeclaration.getModuleName)
      val idsToRemove = importDeclaration.getImportSpec.getImportIdsSpec.getImportIdList.asScala.filter(qn => idNames.exists(idn => idn == qn.getText || prefix.exists(p => idn == p + "." + qn.getText)))
      idsToRemove.foreach { iid =>
        val commaToRemove = Option(PsiTreeUtil.findSiblingForward(iid, HS_COMMA, true, null)).orElse(Option(PsiTreeUtil.findSiblingBackward(iid, HS_COMMA, true, null)))
        val whiteSpaceRemove = Option(PsiTreeUtil.findSiblingBackward(iid, WHITE_SPACE, true, null)).orElse(Option(PsiTreeUtil.findSiblingForward(iid, WHITE_SPACE, true, null)))
        val newline = whiteSpaceRemove.flatMap(s => Option(PsiTreeUtil.findSiblingForward(s, HS_NEWLINE, true, null)))
        WriteCommandAction.runWriteCommandAction(psiFile.getProject, ScalaUtil.computable {
          whiteSpaceRemove.foreach(_.delete())
          newline.foreach(_.delete())
          commaToRemove.foreach(_.delete())
          iid.delete()
        })
      }
    }
  }
}