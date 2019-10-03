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
import com.intellij.psi.PsiFile
import com.intellij.psi.TokenType.WHITE_SPACE
import com.intellij.psi.util.PsiTreeUtil
import intellij.haskell.HaskellFile
import intellij.haskell.psi.HaskellPsiUtil
import intellij.haskell.psi.HaskellTypes._
import intellij.haskell.util.{HaskellFileUtil, HaskellProjectUtil, ScalaUtil}

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

    warnings.foreach(_.getDescription match {
      case HaskellImportOptimizer.WarningRedundantImport(mn) => removeRedundantImport(psiFile, mn)
      case HaskellImportOptimizer.WarningRedundant2Import(idNames, mn) => removeRedundantImportIds(psiFile, mn, idNames.split(',').toSeq.map(_.trim))
      case _ => ()
    })
    true
  }

  def removeRedundantImport(psiFile: PsiFile, moduleName: String): Unit = {
    HaskellPsiUtil.findImportDeclarations(psiFile).find(_.getModuleName.contains(moduleName)).foreach { importDeclaration =>
      val newline = Option(PsiTreeUtil.findSiblingForward(importDeclaration, HS_NEWLINE, true, null))
      WriteCommandAction.runWriteCommandAction(psiFile.getProject, ScalaUtil.computable {
        importDeclaration.delete()
        newline.foreach(_.delete())
      })
    }
  }

  import intellij.haskell.psi.HaskellTypes._

  def removeRedundantImportIds(psiFile: PsiFile, moduleName: String, idNames: Seq[String]): Unit = {
    HaskellPsiUtil.findImportDeclarations(psiFile).find(_.getModuleName.contains(moduleName)).foreach { importDeclaration =>
      val prefix = Option(importDeclaration.getImportQualifiedAs).map(_.getQualifier.getName).orElse(importDeclaration.getModuleName)
      val idsToRemove = importDeclaration.getImportSpec.getImportIdsSpec.getImportIdList.asScala.flatMap(_.getQNameList.asScala).filter(qn => idNames.exists(idn => idn == qn.getName || prefix.exists(p => idn == p + "." + qn.getName)))
      idsToRemove.foreach { iid =>
        val commaToRemove = Option(PsiTreeUtil.findSiblingBackward(iid, HS_COMMA, true, null)).orElse(Option(PsiTreeUtil.findSiblingForward(iid, HS_COMMA, true, null)))
        val whiteSpaceRemove = Option(PsiTreeUtil.findSiblingBackward(iid, WHITE_SPACE, true, null)).orElse(Option(PsiTreeUtil.findSiblingForward(iid, WHITE_SPACE, true, null)))
        WriteCommandAction.runWriteCommandAction(psiFile.getProject, ScalaUtil.computable {
          whiteSpaceRemove.foreach(_.delete())
          commaToRemove.foreach(_.delete())
          iid.delete()
        })
      }
    }
  }
}