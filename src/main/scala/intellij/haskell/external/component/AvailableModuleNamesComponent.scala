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
import com.intellij.psi.search.FileTypeIndex
import intellij.haskell.HaskellFileType
import intellij.haskell.external.component.HaskellComponentsManager.StackComponentInfo
import intellij.haskell.external.repl.StackRepl.{BenchmarkType, TestSuiteType}
import intellij.haskell.psi.HaskellPsiUtil
import intellij.haskell.util.HaskellFileUtil

import scala.collection.JavaConverters._

private[component] object AvailableModuleNamesComponent {

  private final val TestStanzaTypes = Seq(TestSuiteType, BenchmarkType)

  def findAvailableModuleNamesWithIndex(stackComponentInfo: StackComponentInfo): Iterable[String] = {
    findAvailableProjectModuleNamesWithIndex(stackComponentInfo) ++ findAvailableLibraryModuleNames(stackComponentInfo)
  }

  def findAvailableLibraryModuleNamesWithIndex(module: Module): Iterable[String] = {
    findModuleNamesInDirectories(module, includeTests = false)
  }

  private def findAvailableProjectModuleNamesWithIndex(stackComponentInfo: StackComponentInfo): Iterable[String] = {
    findModuleNamesInDirectories(stackComponentInfo.module, TestStanzaTypes.contains(stackComponentInfo.stanzaType))
  }

  private def findAvailableLibraryModuleNames(stackComponentInfo: StackComponentInfo): Iterable[String] = {
    HaskellComponentsManager.findStackComponentGlobalInfo(stackComponentInfo).map(_.availableLibraryModuleNames).getOrElse(Iterable())
  }

  private def findModuleNamesInDirectories(module: Module, includeTests: Boolean): Iterable[String] = {
    for {
      vf <- FileTypeIndex.getFiles(HaskellFileType.Instance, module.getModuleScope(includeTests)).asScala
      hf <- HaskellFileUtil.convertToHaskellFileInReadAction(module.getProject, vf).toOption.flatten
      mn <- HaskellPsiUtil.findModuleName(hf)
    } yield mn
  }
}
