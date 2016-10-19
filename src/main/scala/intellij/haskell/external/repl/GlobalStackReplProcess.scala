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

private[external] class GlobalStackReplProcess(project: Project) extends StackReplProcess(project, Seq("--no-package-hiding")) {
  override def getComponentName: String = "global-stack-repl"

  def getModuleIdentifiers(moduleName: String): Option[StackReplOutput] = synchronized {
    execute(s":module $moduleName")
    execute(s":browse! $moduleName")
  }

  def findNameInfo(moduleName: String, name: String): Option[StackReplOutput] = synchronized {
    val output = execute(s":module $moduleName")
    if (output.exists(_.stdErrLines.isEmpty)) {
      execute(s":info $name")
    } else {
      // Now no info means never info because it's library
      Some(StackReplOutput())
    }
  }

  def showActiveLanguageFlags(): Option[StackReplOutput] = synchronized {
    execute(":show language")
  }
}
