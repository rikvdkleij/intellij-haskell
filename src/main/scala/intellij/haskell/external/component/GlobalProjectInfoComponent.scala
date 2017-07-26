/*
 * Copyright 2014-2017 Rik van der Kleij
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

import java.util.concurrent.Executors

import com.google.common.cache.{CacheBuilder, CacheLoader}
import com.google.common.util.concurrent.{ListenableFuture, ListenableFutureTask, UncheckedExecutionException}
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.project.Project
import intellij.haskell.external.execution.{CommandLine, StackCommandLine}

import scala.collection.JavaConverters._

private[component] object GlobalProjectInfoComponent {

  private case class Key(project: Project)

  private val executor = Executors.newCachedThreadPool()

  private final val Cache = CacheBuilder.newBuilder()
    .build(
      new CacheLoader[Key, Option[GlobalProjectInfo]]() {

        override def load(key: Key): Option[GlobalProjectInfo] = {
          createGlobalProjectInfo(key)
        }

        override def reload(key: Key, oldValue: Option[GlobalProjectInfo]): ListenableFuture[Option[GlobalProjectInfo]] = {
          val task = ListenableFutureTask.create[Option[GlobalProjectInfo]](() => {
            createGlobalProjectInfo(key)
          })
          executor.execute(task)
          task
        }

        private def createGlobalProjectInfo(key: Key): Option[GlobalProjectInfo] = {
          val project = key.project
          val extensions = getSupportedLanguageExtensions(project)
          val packageNames = getAvailablePackages(project)
          extensions.map(exts => GlobalProjectInfo(exts, packageNames))
        }

        def getSupportedLanguageExtensions(project: Project): Option[Iterable[String]] = {
          findGhcPath(project).flatMap(ghcPath => {
            CommandLine.runProgram(
              Some(project),
              project.getBasePath,
              ghcPath,
              Seq("--supported-languages"),
              notifyBalloonError = true
            ).map(_.getStdoutLines.asScala)
          })
        }

        def getAvailablePackages(project: Project): Iterable[String] = {
          CabalConfigComponent.getAvailablePackageNames(project)
        }

        private def findGhcPath(project: Project) = {
          StackCommandLine.runCommand(project, Seq("path", "--compiler-exe")).flatMap(_.getStdoutLines.asScala.headOption)
        }
      }
    )

  def findGlobalProjectInfo(project: Project): Option[GlobalProjectInfo] = {
    try {
      val key = Key(project)
      Cache.get(key) match {
        case result@Some(_) => result
        case _ =>
          Cache.invalidate(key)
          None
      }
    }
    catch {
      case _: UncheckedExecutionException => None
      case _: ProcessCanceledException => None
    }
  }

  def invalidate(project: Project): Unit = {
    val keys = Cache.asMap().keySet().asScala.filter(_.project == project)
    keys.foreach(Cache.invalidate)
  }
}

case class GlobalProjectInfo(supportedLanguageExtensions: Iterable[String], availablePackageNames: Iterable[String])
