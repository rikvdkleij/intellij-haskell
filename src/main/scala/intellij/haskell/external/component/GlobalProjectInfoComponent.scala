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

import java.nio.file.Paths

import com.github.blemale.scaffeine.{LoadingCache, Scaffeine}
import com.intellij.openapi.project.Project
import intellij.haskell.external.execution.{CommandLine, StackCommandLine}
import intellij.haskell.util.{GhcVersion, ScalaUtil}

import scala.collection.JavaConverters._

private[component] object GlobalProjectInfoComponent {

  private case class Key(project: Project)

  private final val Cache: LoadingCache[Key, Option[GlobalProjectInfo]] = Scaffeine().build((k: Key) => createGlobalProjectInfo(k))

  def findGlobalProjectInfo(project: Project): Option[GlobalProjectInfo] = {
    val key = Key(project)
    Cache.get(key) match {
      case result@Some(_) => result
      case _ =>
        Cache.invalidate(key)
        None
    }
  }

  def getSupportedLanguageExtensions(project: Project, ghcPath: String): Seq[String] = {
    CommandLine.run(
      project,
      ghcPath,
      Seq("--supported-languages"),
      notifyBalloonError = true
    ).getStdoutLines.asScala
  }

  def getAvailableStackagesPackages(project: Project): Iterable[String] = {
    CabalConfigComponent.getAvailablePackageNames(project)
  }

  def invalidate(project: Project): Unit = {
    val keys = Cache.asMap().keys.filter(_.project == project)
    keys.foreach(Cache.invalidate)
  }

  private def createGlobalProjectInfo(key: Key): Option[GlobalProjectInfo] = {
    val project = key.project
    for {
      pathLines <- findPathLines(project)
      pathInfoMap = ScalaUtil.linesToMap(pathLines)
      binPaths <- findBinPaths(pathInfoMap)
      packageDbPaths <- findPackageDbPaths(pathInfoMap)
      ghcPath = Paths.get(binPaths.compilerBinPath, "ghc").toString
      ghcPkgPath = Paths.get(binPaths.compilerBinPath, "ghc-pkg").toString
      interoPath = Paths.get(binPaths.localBinPath, "intero").toString
      extensions = getSupportedLanguageExtensions(project, ghcPath)
      stackagePackageNames = getAvailableStackagesPackages(project)
      ghcVersion = findGhcVersion(project, ghcPath)
      localDocRoot <- pathInfoMap.get("local-doc-root")
    } yield GlobalProjectInfo(ghcVersion, ghcPath, ghcPkgPath, interoPath, localDocRoot, packageDbPaths, binPaths, extensions, stackagePackageNames)
  }

  private def findPathLines(project: Project) = {
    StackCommandLine.run(project, Seq("path")).map(_.getStdoutLines.asScala.toSeq)
  }

  private def findGhcVersion(project: Project, ghcPath: String): GhcVersion = {
    val output = CommandLine.run(project, ghcPath, Seq("--numeric-version"))
    GhcVersion.parse(output.getStdout.trim)
  }

  private def findBinPaths(pathInfoMap: Map[String, String]): Option[ProjectBinPaths] = {
    for {
      compilerBinPath <- pathInfoMap.get("compiler-bin")
      localBinPath <- pathInfoMap.get("local-install-root").map(p => Paths.get(p, "bin").toString)
    } yield ProjectBinPaths(compilerBinPath, localBinPath)
  }

  private def findPackageDbPaths(pathInfoMap: Map[String, String]): Option[PackageDbPaths] = {
    for {
      globalPackageDbPath <- pathInfoMap.get("global-pkg-db")
      snapshotPackageDbPath <- pathInfoMap.get("snapshot-pkg-db")
      localPackageDbPath <- pathInfoMap.get("local-pkg-db")
    } yield PackageDbPaths(globalPackageDbPath, snapshotPackageDbPath, localPackageDbPath)
  }
}


case class GlobalProjectInfo(ghcVersion: GhcVersion, ghcPath: String, ghcPkgPath: String, interoPath: String, localDocRoot: String,  packageDbPaths: PackageDbPaths, projectBinPaths: ProjectBinPaths, supportedLanguageExtensions: Iterable[String], availableStackagePackageNames: Iterable[String])

case class PackageDbPaths(globalPackageDbPath: String, snapshotPackageDbPath: String, localPackageDbPath: String)

case class ProjectBinPaths(compilerBinPath: String, localBinPath: String)
