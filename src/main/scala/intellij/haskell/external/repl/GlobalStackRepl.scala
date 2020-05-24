/*
 * Copyright 2014-2020 Rik van der Kleij
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
import intellij.haskell.util.{HaskellProjectUtil, ScalaFutureUtil}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._

case class GlobalStackRepl(project: Project, replTimeout: Int) extends StackRepl(project, None, Seq("--no-package-hiding", "--no-load"), replTimeout) {

  @volatile
  private[this] var loadedModuleName: Option[String] = None

  def clearLoadedModules(): Unit = {
    loadedModuleName = None
  }

  def getModuleIdentifiers(moduleName: String): Option[StackReplOutput] = {
    val isPrelude = moduleName == HaskellProjectUtil.Prelude
    if (isPrelude) {
      ScalaFutureUtil.waitForValue(project, Future {
        blocking {
          synchronized {
            // To get qualified identifiers for Prelude
            execute(s""":set -XNoImplicitPrelude""")
            execute(s""":unadd Prelude""")
            val result = execute(s":browse! $moduleName")
            execute(s""":set -XImplicitPrelude""")
            execute(s""":add Prelude""")
            result
          }
        }
      }, ":browse in GlobalStackRepl").flatten
    } else {
      ScalaFutureUtil.waitForValue(project, Future {
        blocking {
          synchronized {
            execute(s":browse! $moduleName")
          }
        }
      }, ":browse in GlobalStackRepl").flatten
    }
  }

  def findInfo(moduleName: String, name: String): Option[StackReplOutput] = {
    ScalaFutureUtil.waitForValue(project, Future {
      blocking {
        synchronized {
          loadModule(moduleName)

          if (loadedModuleName.contains(moduleName)) {
            execute(s":info $name")
          } else {
            // No info means NEVER info because it's library
            Some(StackReplOutput())
          }
        }
      }
    }, ":info in GlobalStackRepl").flatten
  }

  override def restart(forceExit: Boolean): Unit = synchronized {
    if (available && !starting) {
      exit(forceExit)
      loadedModuleName = None
      Thread.sleep(1000)
      start()
    }
  }

  private def loadModule(moduleName: String): Unit = {
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
