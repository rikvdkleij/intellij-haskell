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

package intellij.haskell.external.repl.process

import com.intellij.openapi.project.Project

class GlobalStackReplProcess(project: Project) extends StackReplProcess(project, Seq("--no-build", "--no-package-hiding")) {
  override def getComponentName: String = "global-stack-repl"

  def getModuleIdentifiers(moduleName: String): StackReplOutput = synchronized {
    execute(":module " + moduleName)
    execute(":browse! " + moduleName)
  }

  def findNameInfo(moduleName: String, name: String): StackReplOutput = synchronized {
    val output = execute(s":module $moduleName")
    if (output.stdErrLines.isEmpty) {
      execute(s":info $name")
    } else {
      StackReplOutput()
    }
  }

  def showActiveLanguageFlags(): StackReplOutput = {
    execute(":show language")
  }
}
