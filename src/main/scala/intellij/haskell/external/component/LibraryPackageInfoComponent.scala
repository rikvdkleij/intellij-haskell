/*
 * Copyright 2014-2019 Rik van der Kleij
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
import intellij.haskell.external.execution.CommandLine
import intellij.haskell.util.ScalaUtil
import intellij.haskell.util.StringUtil.removePackageQualifier

import scala.jdk.CollectionConverters._

private[component] object LibraryPackageInfoComponent {

  private case class Key(project: Project, packageName: String)

  private type Result = Option[PackageInfo]

  private final val Cache: LoadingCache[Key, Result] = Scaffeine().build((k: Key) => findPackageInfo(k))

  private def splitLines(s: String, excludeEmptyLines: Boolean) = {
    val converted = StringUtil.convertLineSeparators(s)
    StringUtil.split(converted, "\n", true, excludeEmptyLines).asScala.toSeq
  }

  import scala.concurrent.duration._

  def preloadLibraryPackageInfos(project: Project): Unit = {
    val projectPackageNames = HaskellComponentsManager.findProjectModulePackageNames(project).map(_._2)
    val globalProjectInfo = HaskellComponentsManager.getGlobalProjectInfo(project)

    val result = globalProjectInfo.map(info => CommandLine.run(project, info.ghcPkgPath,
      Seq("dump",
        s"--package-db=${info.packageDbPaths.globalPackageDbPath}",
        s"--package-db=${info.packageDbPaths.snapshotPackageDbPath}",
        s"--package-db=${info.packageDbPaths.localPackageDbPath}"), notifyBalloonError = true, timeoutInMillis = 60.seconds.toMillis)).map { processOutput =>

      val packageOutputs = processOutput.getStdout.split("(?m)^---\n")
      packageOutputs.map(o => {
        val outputLines = splitLines(o, excludeEmptyLines = true)
        findPackageInfo(outputLines)
      })
    }

    result match {
      case Some(r) => r.foreach {
        case d@Some(packageInfo) => if (!projectPackageNames.contains(packageInfo.packageName) && packageInfo.packageName != "rts") Cache.put(Key(project, packageInfo.packageName), d)
        case None => HaskellNotificationGroup.logInfoBalloonEvent(project, s"Could not retrieve all package information via `ghc-pkg dump`")
      }
      case None => HaskellNotificationGroup.logErrorBalloonEvent(project, "Executing `ghc-pkg dump` failed")
    }
  }

  def findLibraryPackageInfo(project: Project, packageName: String): Result = {
    val key = Key(project, packageName)
    Cache.get(key) match {
      case result@Some(_) => result
      case _ => None
    }
  }

  def libraryPackageInfos(project: Project): Iterable[PackageInfo] = {
    Cache.asMap().values.flatten
  }

  private def findPackageInfo(key: Key): Result = {
    // Because preloadLibraryModuleNames should already have done all the work, something is wrong if this method is called
    HaskellNotificationGroup.logErrorBalloonEvent(key.project, s"Package ${key.packageName} is not in library module names cache")
    None
  }

  private final val PackageNameVersionPattern = """([\w\-]+)-([\d\.]+)(?:\-.*)?""".r

  def toPackageNameversion(depends: String): Option[PackageId] = {
    depends match {
      case PackageNameVersionPattern(name, version) => Some(PackageId(name, version))
      case _ => None
    }
  }

  private def findPackageInfo(lines: Seq[String]): Option[PackageInfo] = {
    val packageInfoMap = ScalaUtil.linesToMap(lines)

    for {
      name <- packageInfoMap.get("name")
      version <- packageInfoMap.get("version")
      id <- packageInfoMap.get("id")
      exposedModuleNames = packageInfoMap.get("exposed-modules").map(splitLine).getOrElse(Seq())
      hiddenModuleNames = packageInfoMap.get("hidden-modules").map(splitLine).getOrElse(Seq())
      dependsPackageNames = packageInfoMap.get("depends").map(splitLine).getOrElse(Seq()).flatMap(toPackageNameversion)
    } yield PackageInfo(name, version, id, exposedModuleNames, hiddenModuleNames, dependsPackageNames)
  }

  private def splitLine(s: String): Seq[String] = {
    s.replaceAll("""\s+""", ",").split(",").map(_.trim).filterNot(_ == "from").map(removePackageQualifier).toSeq
  }

  def invalidate(project: Project): Unit = {
    val keys = Cache.asMap().keys.filter(_.project == project)
    keys.foreach(Cache.invalidate)
  }
}


case class PackageInfo(packageName: String, version: String, id: String, exposedModuleNames: Seq[String], hiddenModuleNames: Seq[String], dependsOnPackageIds: Seq[PackageId])

case class PackageId(name: String, version: String)