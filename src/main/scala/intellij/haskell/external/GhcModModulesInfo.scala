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

package intellij.haskell.external

import java.util.concurrent.{Callable, Executors, TimeUnit}

import com.google.common.cache.{CacheBuilder, CacheLoader}
import com.google.common.util.concurrent.{ListenableFuture, ListenableFutureTask, UncheckedExecutionException}
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.project.Project

object GhcModModulesInfo {

  private case class ModuleList(project: Project)

  private val executor = Executors.newCachedThreadPool()

  private final val ModuleListCache = CacheBuilder.newBuilder()
    .refreshAfterWrite(60, TimeUnit.SECONDS)
    .build(
      new CacheLoader[ModuleList, GhcModOutput]() {
        private def getProcessOutput(moduleList: ModuleList): GhcModOutput = {
          GhcModProcessManager.getGhcModInfoProcess(moduleList.project).execute("list")
        }

        override def load(moduleList: ModuleList): GhcModOutput = {
          getProcessOutput(moduleList)
        }

        override def reload(moduleList: ModuleList, oldValue: GhcModOutput): ListenableFuture[GhcModOutput] = {
          val task = ListenableFutureTask.create(new Callable[GhcModOutput]() {
            def call() = {
              val newValue = getProcessOutput(moduleList)
              if (newValue.outputLines.isEmpty) {
                oldValue
              } else {
                newValue
              }
            }
          })
          executor.execute(task)
          task
        }
      }
    )

  def listAvailableModules(project: Project): Iterable[String] = {
    val processOutput = try {
      ModuleListCache.get(ModuleList(project))
    }
    catch {
      case _: UncheckedExecutionException => GhcModOutput()
      case _: ProcessCanceledException => GhcModOutput()
    }
    processOutput.outputLines
  }
}
