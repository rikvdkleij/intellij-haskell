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
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.text.StringUtil
import intellij.haskell.HaskellNotificationGroup
import intellij.haskell.external.execution.StackCommandLine

private[component] object LibraryModuleNamesComponent {

  import scala.collection.JavaConverters._

  private case class Key(project: Project, packageName: String)

  private type Result = Option[LibraryModuleNames]

  private final val Cache: LoadingCache[Key, Result] = Scaffeine().build((k: Key) => findAvailableModuleNames(k))

  private def splitLines(s: String, excludeEmptyLines: Boolean) = {
    val converted = StringUtil.convertLineSeparators(s)
    StringUtil.split(converted, "\n", true, excludeEmptyLines).asScala
  }

  import scala.concurrent.duration._

  def preloadLibraryModuleNames(project: Project): Unit = {
    val result = StackCommandLine.run(project, Seq("exec", "--", "ghc-pkg", "dump"), notifyBalloonError = true, timeoutInMillis = 60.seconds.toMillis).map { processOutput =>
      val packageOutputs = processOutput.getStdout.split("(?m)^---\n")
      packageOutputs.map(o => {
        val outputLines = splitLines(o, excludeEmptyLines = true)
        val (packageName, exposedModuleNames, hiddenModuleNames) = findPackageModuleNames(outputLines)
        (packageName, LibraryModuleNames(exposedModuleNames, hiddenModuleNames))
      })
    }.getOrElse(Array())

    result.foreach { case (name, lmn) =>
      name.foreach { n =>
        Cache.put(Key(project, n), Some(lmn))
      }
    }
  }

  def findLibraryModuleNames(project: Project, packageName: String): Option[LibraryModuleNames] = {
    val key = Key(project, packageName)
    Cache.get(key) match {
      case result@Some(_) => result
      case _ => None
    }
  }


  private def findAvailableModuleNames(key: Key): Option[LibraryModuleNames] = {
    // Because preloadLibraryModuleNames should already have done all the work, something is wrong if this method is called
    HaskellNotificationGroup.logErrorBalloonEvent(key.project, s"Package ${key.packageName} is not in library module names cache")
    None
  }

  private final val NameKey = "name: "

  private def findPackageModuleNames(lines: Seq[String]): (Option[String], Iterable[String], Iterable[String]) = {
    val name = lines.find(_.startsWith(NameKey)).map(_.replace(NameKey, ""))
    val exposedModuleNameLines = lines.dropWhile(_ != "exposed-modules:").drop(1)
    val hiddenModuleNameLines = lines.dropWhile(_ != "hidden-modules:").drop(1)

    def findModuleNames(lines: Iterable[String]) = {
      lines.takeWhile(_.startsWith(" ")).mkString(" ").trim.split("""\s+""")
    }

    val exposedModulenames = findModuleNames(exposedModuleNameLines)
    val hiddenModulenames = findModuleNames(hiddenModuleNameLines)
    (name, exposedModulenames.toIterable, hiddenModulenames.toIterable)
  }

  def invalidate(project: Project): Unit = {
    val keys = Cache.asMap().keys.filter(_.project == project)
    keys.foreach(Cache.invalidate)
  }
}


case class LibraryModuleNames(exposed: Iterable[String], hidden: Iterable[String])