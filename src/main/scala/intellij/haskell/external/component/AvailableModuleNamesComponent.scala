/*
 * Copyright 2014-2018 Rik van der Kleij
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

import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.search.FileTypeIndex
import intellij.haskell.HaskellFileType
import intellij.haskell.external.repl.StackRepl.{BenchmarkType, TestSuiteType}
import intellij.haskell.external.repl.StackReplsManager.StackComponentInfo
import intellij.haskell.util.HaskellFileUtil
import intellij.haskell.util.index.HaskellFilePathIndex

import scala.collection.JavaConverters._

// TODO Consider to use cache with expiring
private[component] object AvailableModuleNamesComponent {

  def findAvailableModuleNamesWithIndex(psiFile: PsiFile): Iterable[String] = {
    val stackComponentInfo = HaskellComponentsManager.findStackComponentInfo(psiFile)
    stackComponentInfo match {
      case Some(info) =>
        val libraryModuleNames = findAvailableLibraryModuleNames(info)
        findAvailableProjectModuleNamesWithIndex(info) ++ libraryModuleNames
      case None => Iterable()
    }
  }

  private final val TestStanzaTypes = Seq(TestSuiteType, BenchmarkType)

  def findAvailableProjectModuleNamesWithIndex(stackComponentInfo: StackComponentInfo): Iterable[String] = {
    val module = stackComponentInfo.module
    val project = module.getProject
    findModuleNamesInDirectories(project, module, TestStanzaTypes.contains(stackComponentInfo.stanzaType))
  }

  def findAvailableLibraryModuleNamesWithIndex(module: Module): Iterable[String] = {
    findModuleNamesInDirectories(module.getProject, module, includeTests = false)
  }

  private def findAvailableLibraryModuleNames(stackComponentInfo: StackComponentInfo): Iterable[String] = {
    HaskellComponentsManager.findStackComponentGlobalInfo(stackComponentInfo).map(_.availableLibraryModuleNames).getOrElse(Iterable())
  }

  private def findModuleNamesInDirectories(project: Project, module: Module, includeTests: Boolean): Iterable[String] = {
    for {
      vf <- FileTypeIndex.getFiles(HaskellFileType.Instance, module.getModuleScope(includeTests)).asScala
      hf <- HaskellFileUtil.convertToHaskellFile(project, vf)
      mn <- HaskellFilePathIndex.findModuleName(hf, module.getModuleScope(includeTests))
    } yield mn
  }
}
