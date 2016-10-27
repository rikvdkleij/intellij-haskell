/*
 * Copyright 2016 Rik van der Kleij
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

import com.intellij.lang.ImportOptimizer
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import intellij.haskell.HaskellFile
import intellij.haskell.external.component.StackReplsComponentsManager
import intellij.haskell.psi.HaskellElementCondition
import intellij.haskell.util.{HaskellProjectUtil, LineColumnPosition}

// TODO: Refactor so call to loadHaskellFile is not done if called from HaskellAnnotator
class HaskellImportOptimizer extends ImportOptimizer {

  override def supports(psiFile: PsiFile): Boolean = psiFile.isInstanceOf[HaskellFile] && !HaskellProjectUtil.isLibraryFile(psiFile)

  override def processFile(psiFile: PsiFile): Runnable = {
    new Runnable {
      override def run(): Unit = {
        val problems = StackReplsComponentsManager.loadHaskellFile(psiFile, refreshCache = false).currentFileProblems
        val redundantImports = problems.filter(p => p.plainMessage match {
          case HaskellImportOptimizer.WarningRedundantImport(moduleName) => true
          case _ => false
        })

        val redundantImportModuleOffsets = redundantImports.flatMap(p => LineColumnPosition.getOffset(psiFile, LineColumnPosition(p.lineNr, p.columnNr)))
        val redundantModuleDeclarations = redundantImportModuleOffsets.map(offset => psiFile.findElementAt(offset)).map(e => PsiTreeUtil.findFirstParent(e, HaskellElementCondition.ImportDeclarationCondition))
        redundantModuleDeclarations.foreach { me =>
          me.delete()
        }
      }
    }
  }
}

object HaskellImportOptimizer {
  final val WarningRedundantImport = """[W|w]arning.*The import of [`|‘]([^'’]+)['|’] is redundant.*""".r
}
