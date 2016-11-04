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

package intellij.haskell.external.component

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.util.Computable
import com.intellij.psi.PsiFile
import intellij.haskell.psi.HaskellPsiUtil
import intellij.haskell.util.{HaskellFileIndex, HaskellProjectUtil}

import scala.collection.JavaConversions._

private[component] object AvailableModuleNamesComponent {

  def findAvailableModuleNames(psiFile: PsiFile): Iterable[String] = {
    if (HaskellProjectUtil.isProjectTestFile(psiFile)) {
      val libraryTestModuleNames = GlobalProjectInfoComponent.findGlobalProjectInfo(psiFile.getProject).map(_.allAvailableLibraryModuleNames).getOrElse(Stream())
      val testModuleNames = ApplicationManager.getApplication.runReadAction {
        new Computable[Iterable[String]] {
          override def compute(): Iterable[String] = {
            HaskellFileIndex.findProjectTestPsiFiles(psiFile.getProject).flatMap(HaskellPsiUtil.findModuleName)
          }
        }
      }
      testModuleNames ++ libraryTestModuleNames
    } else {
      val libraryModuleNames = GlobalProjectInfoComponent.findGlobalProjectInfo(psiFile.getProject).map(_.availableProductionLibraryModuleNames).getOrElse(Stream())
      val prodModuleNames = ApplicationManager.getApplication.runReadAction {
        new Computable[Iterable[String]] {
          override def compute(): Iterable[String] = {
            HaskellFileIndex.findProjectProductionPsiFiles(psiFile.getProject).flatMap(HaskellPsiUtil.findModuleName)
          }
        }
      }
      prodModuleNames ++ libraryModuleNames
    }
  }
}
