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

package intellij.haskell.external.repl

import com.intellij.openapi.project.Project

private[repl] class GlobalStackReplProcess(project: Project) extends StackReplProcess(project, Seq("--no-load", "--no-package-hiding")) {
  override def getComponentName: String = "global-stack-repl"

  private[this] var loadedModuleName: Option[String] = None

  def getModuleIdentifiers(moduleName: String): Option[StackReplOutput] = synchronized {
    loadModule(moduleName)

    if (loadedModuleName.contains(moduleName)) {
      execute(s":browse! $moduleName")
    } else {
      None
    }
  }

  def findInfo(moduleName: String, name: String): Option[StackReplOutput] = synchronized {
    loadModule(moduleName)

    if (loadedModuleName.contains(moduleName)) {
      execute(s":info $name")
    } else {
      // No info means never info because it's library
      Some(StackReplOutput())
    }
  }

  def showActiveLanguageFlags(): Option[StackReplOutput] = synchronized {
    execute(":show language")
  }

  private def loadModule(moduleName: String) = {
    if (!loadedModuleName.contains(moduleName)) {
      val output = execute(s":module $moduleName")
      if (output.exists(_.stdErrLines.isEmpty)) {
        loadedModuleName = Some(moduleName)
      } else {
        loadedModuleName = None
      }
    }
  }
}
