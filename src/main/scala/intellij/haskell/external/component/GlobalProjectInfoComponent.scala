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

package intellij.haskell.external.component

import java.util.concurrent.{Callable, Executors}

import com.google.common.cache.{CacheBuilder, CacheLoader}
import com.google.common.util.concurrent.{ListenableFuture, ListenableFutureTask, UncheckedExecutionException}
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.project.Project
import intellij.haskell.external.commandLine.{CommandLine, StackCommandLine}
import intellij.haskell.external.repl.StackReplsManager

import scala.collection.JavaConversions._

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
          val task = ListenableFutureTask.create(new Callable[Option[GlobalProjectInfo]]() {
            def call() = {
              createGlobalProjectInfo(key)
            }
          })
          executor.execute(task)
          task
        }

        private def createGlobalProjectInfo(key: Key): Option[GlobalProjectInfo] = {
          val project = key.project
          StackReplsManager.getProjectRepl(project).findAllAvailableLibraryModules.flatMap { allModuleNames =>
            val prodModuleNames = allModuleNames.filterNot(_.startsWith("Test."))
            isNoImplicitPreludeGlobalActive(project).map(active => GlobalProjectInfo(prodModuleNames, allModuleNames, active, getLanguageExtensions(project)))
          }
        }

        private def isNoImplicitPreludeGlobalActive(project: Project): Option[Boolean] = {
          val languageFlags = StackReplsManager.getGlobalRepl(project).showActiveLanguageFlags().map(_.stdOutLines)
          languageFlags.map(_.exists(_.contains("-XNoImplicitPrelude")))
        }

        private def getLanguageExtensions(project: Project): Iterable[String] = {
          findGhcPath(project).map(ghcPath => {
            CommandLine.runCommand(
              project.getBasePath,
              ghcPath,
              Seq("--supported-languages")
            ).getStdoutLines.toIterable
          }).getOrElse(Iterable())
        }

        private def findGhcPath(project: Project) = {
          StackCommandLine.runCommand(Seq("path", "--compiler-exe"), project).getStdoutLines.headOption
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
    Cache.invalidate(Key(project))
  }
}

case class GlobalProjectInfo(availableProductionLibraryModuleNames: Iterable[String] = Iterable(),
                             allAvailableLibraryModuleNames: Iterable[String] = Iterable(),
                             noImplicitPreludeActive: Boolean = false,
                             languageExtensions: Iterable[String] = Iterable())
