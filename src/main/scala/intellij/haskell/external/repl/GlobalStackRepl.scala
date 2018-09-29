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

package intellij.haskell.external.repl

import com.intellij.openapi.project.Project
import intellij.haskell.external.repl.StackRepl.StackReplOutput

case class GlobalStackRepl(project: Project, replTimeout: Int) extends StackRepl(project, None, Seq("--no-package-hiding"), replTimeout) {

  private[this] var loadedModuleName: Option[String] = None

  @volatile
  var isBusy = false

  def getModuleIdentifiers(moduleName: String): Option[StackReplOutput] = synchronized {
    try {
      isBusy = true
      execute(s":browse! $moduleName")
    } finally {
      isBusy = false
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

  override def restart(forceExit: Boolean): Unit = synchronized {
    if (available && !starting) {
      exit(forceExit)
      loadedModuleName = None
      start()
    }
  }

  private def loadModule(moduleName: String) = {
    if (!loadedModuleName.contains(moduleName)) {
      val output = execute(s":module $moduleName")
      if (output.exists(_.stderrLines.isEmpty)) {
        loadedModuleName = Some(moduleName)
      } else {
        loadedModuleName = None
      }
    }
  }
}
