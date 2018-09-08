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

import com.github.blemale.scaffeine.{LoadingCache, Scaffeine}
import com.intellij.execution.process.ProcessOutput
import com.intellij.openapi.project.Project
import intellij.haskell.external.execution.StackCommandLine

private[component] object LibraryModuleNamesComponent {

  private case class Key(project: Project, packageName: String)

  private type Result = Option[LibraryModuleNames]

  private final val Cache: LoadingCache[Key, Result] = Scaffeine().build((k: Key) => findAvailableModuleNames(k))

  def findLibraryModuleNames(project: Project, packageName: String): Option[LibraryModuleNames] = {
    val key = Key(project, packageName)
    Cache.get(key) match {
      case result@Some(_) => result
      case _ =>
        Cache.invalidate(key)
        None
    }
  }

  import scala.collection.JavaConverters._

  private def findAvailableModuleNames(key: Key): Option[LibraryModuleNames] = {
    StackCommandLine.run(key.project, Seq("exec", "--", "ghc-pkg", "describe", key.packageName)).flatMap { processOutput =>
      val (axposedModuleNames, hiddenModuleNames) = findModuleNames(processOutput)
      Some(LibraryModuleNames(axposedModuleNames, hiddenModuleNames))
    }
  }

  private def findModuleNames(processOutput: ProcessOutput) = {
    val lines = processOutput.getStdoutLines.asScala
    val exposedModuleNameLines = lines.dropWhile(_ != "exposed-modules:").drop(1)
    val hiddenModuleNameLines = lines.dropWhile(_ != "hidden-modules:").drop(1)

    def findModuleNames(lines: Iterable[String]) = {
      lines.takeWhile(_.startsWith(" ")).mkString(" ").trim.split("""\s+""")
    }

    val exposedModulenames = findModuleNames(exposedModuleNameLines)
    val hiddenModulenames = findModuleNames(hiddenModuleNameLines)
    (exposedModulenames.toIterable, hiddenModulenames.toIterable)
  }

  def invalidate(project: Project): Unit = {
    val keys = Cache.asMap().keys.filter(_.project == project)
    keys.foreach(Cache.invalidate)
  }
}


case class LibraryModuleNames(exposed: Iterable[String], hidden: Iterable[String])