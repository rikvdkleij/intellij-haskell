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
import intellij.haskell.external.CommandLine
import intellij.haskell.external.repl.StackReplsManager
import intellij.haskell.external.repl.process.StackReplOutput
import intellij.haskell.sdk.HaskellSdkType

import scala.collection.JavaConversions._

private[component] object GlobalProjectInfoComponent {

  private case class Key(project: Project)

  private val executor = Executors.newCachedThreadPool()

  private final val Cache = CacheBuilder.newBuilder()
    .build(
      new CacheLoader[Key, GlobalProjectInfo]() {

        override def load(key: Key): GlobalProjectInfo = {
          createGlobalProjectInfo(key)
        }

        override def reload(key: Key, oldValue: GlobalProjectInfo): ListenableFuture[GlobalProjectInfo] = {
          val task = ListenableFutureTask.create(new Callable[GlobalProjectInfo]() {
            def call() = {
              createGlobalProjectInfo(key)
            }
          })
          executor.execute(task)
          task
        }

        private def createGlobalProjectInfo(key: Key): GlobalProjectInfo = {
          val project = key.project
          val allAvailableModuleNames = findTail(StackReplsManager.getProjectRepl(project).findAllAvailableLibraryModules)
          val availableInProdModuleNames = allAvailableModuleNames.filterNot(_.startsWith("Test."))
          GlobalProjectInfo(availableInProdModuleNames, allAvailableModuleNames, isNoImplicitPreludeGlobalActive(project), getLanguageExtensions(project))
        }

        private def findTail(output: StackReplOutput) = {
          val lines = output.stdOutLines
          if (lines.isEmpty) {
            Iterable()
          } else {
            lines.tail.map(m => m.substring(1, m.length - 1))
          }
        }

        private def isNoImplicitPreludeGlobalActive(project: Project): Boolean = {
          val languageFlags = StackReplsManager.getGlobalRepl(project).showActiveLanguageFlags().stdOutLines
          languageFlags.exists(_.contains("-XNoImplicitPrelude"))
        }

        private def getLanguageExtensions(project: Project): Iterable[String] = {
          (for {
            stackPath <- HaskellSdkType.getStackPath(project)
            ghcPath <- findGhcPath(stackPath, project.getBasePath)
          } yield ghcPath) match {
            case None => Iterable()
            case Some(ghcPath) => CommandLine.getProcessOutput(
              project.getBasePath,
              ghcPath,
              Seq("--supported-languages")
            ).getStdoutLines
          }
        }

        private def findGhcPath(stackPath: String, projectBasePath: String) = {
          CommandLine.getProcessOutput(
            projectBasePath,
            stackPath,
            Seq("path", "--compiler-exe")
          ).getStdoutLines.headOption
        }
      }
    )

  def findGlobalProjectInfo(project: Project): GlobalProjectInfo = {
    try {
      Cache.get(Key(project))
    }
    catch {
      case _: UncheckedExecutionException => GlobalProjectInfo()
      case _: ProcessCanceledException => GlobalProjectInfo()
    }
  }

  def invalidate(project: Project): Unit = {
    Cache.invalidate(Key(project))
  }
}

case class GlobalProjectInfo(availableInProdLibraryModuleNames: Iterable[String] = Iterable(),
                             availableInTestLibraryModuleNames: Iterable[String] = Iterable(),
                             noImplicitPreludeActive: Boolean = false,
                             languageExtensions: Iterable[String] = Iterable())
