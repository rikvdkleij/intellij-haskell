/*
 * Copyright 2015 Rik van der Kleij
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

package com.powertuple.intellij.haskell.external

import java.util.concurrent.{Callable, Executors, TimeUnit}

import com.google.common.cache.{CacheBuilder, CacheLoader}
import com.google.common.util.concurrent.{ListenableFuture, ListenableFutureTask, UncheckedExecutionException}
import com.intellij.execution.process.ProcessOutput
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.project.Project
import com.powertuple.intellij.haskell.settings.{HaskellSettingsState, HaskellSettings}

import scala.collection.JavaConversions._

object GhcMod {

  private case class ModuleInfo(projectBasePath: String, moduleName: String)

  private case class ModuleList(projectBasePath: String)

  private val executor = Executors.newCachedThreadPool()

  private final val BrowseInfoCache = CacheBuilder.newBuilder()
      .refreshAfterWrite(10, TimeUnit.SECONDS)
      .build(
        new CacheLoader[ModuleInfo, ProcessOutput]() {
          private def getProcessOutput(moduleInfo: ModuleInfo): Option[ProcessOutput] = {
            val ghcModPath = getGhcModPath
            ghcModPath.map { p =>
              ExternalProcess.getProcessOutput(
                moduleInfo.projectBasePath,
                p,
                Seq("browse", "-d", "-q", "-o") ++ Seq(moduleInfo.moduleName),
                4900
              )
            }
          }

          override def load(moduleInfo: ModuleInfo): ProcessOutput = {
            getProcessOutput(moduleInfo).getOrElse(new ProcessOutput())
          }

          override def reload(moduleInfo: ModuleInfo, oldValue: ProcessOutput): ListenableFuture[ProcessOutput] = {
            val task = ListenableFutureTask.create(new Callable[ProcessOutput]() {
              def call() = {
                val newValue = getProcessOutput(moduleInfo)
                newValue match {
                  case None => oldValue
                  case Some(v) => if (v.getStdoutLines.isEmpty) {
                    oldValue
                  } else {
                    v
                  }
                }
              }
            })
            executor.execute(task)
            task
          }
        }
      )

  private final val ModuleListCache = CacheBuilder.newBuilder()
      .refreshAfterWrite(60, TimeUnit.SECONDS)
      .build(
        new CacheLoader[ModuleList, ProcessOutput]() {
          private def getProcessOutput(moduleList: ModuleList): ProcessOutput = {
            val ghcModPath = getGhcModPath
            ghcModPath.map { p =>
              ExternalProcess.getProcessOutput(
                moduleList.projectBasePath,
                p,
                Seq("list")
              )
            }.getOrElse(new ProcessOutput())
          }

          override def load(moduleList: ModuleList): ProcessOutput = {
            getProcessOutput(moduleList)
          }

          override def reload(moduleList: ModuleList, oldValue: ProcessOutput): ListenableFuture[ProcessOutput] = {
            val task = ListenableFutureTask.create(new Callable[ProcessOutput]() {
              def call() = {
                val newValue = getProcessOutput(moduleList)
                if (newValue.getStdoutLines.isEmpty) {
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

  def browseInfo(project: Project, moduleName: String, removeParensFromOperator: Boolean): Iterable[BrowseInfo] = {
    val processOutput = try {
      BrowseInfoCache.get(ModuleInfo(project.getBasePath, moduleName))
    }
    catch {
      case _: UncheckedExecutionException => new ProcessOutput
      case _: ProcessCanceledException => new ProcessOutput
    }
    processOutput.getStdoutLines.map(createBrowseInfo(_, removeParensFromOperator)).flatten
  }

  def listAvailableModules(project: Project): Iterable[String] = {
    val processOutput = try {
      ModuleListCache.get(ModuleList(project.getBasePath))
    }
    catch {
      case _: UncheckedExecutionException => new ProcessOutput
      case _: ProcessCanceledException => new ProcessOutput
    }
    processOutput.getStdoutLines
  }

  def listLanguageExtensions(project: Project): Iterable[String] = {
    val ghcModPath = getGhcModPath
    ghcModPath.map { p =>
      val output = ExternalProcess.getProcessOutput(
        project.getBasePath,
        p,
        Seq("lang")
      )
      output.getStdoutLines.toIterable
    }.getOrElse(Iterable())
  }

  private def getGhcModPath = {
    HaskellSettingsState.getGhcModPath
  }

  private def createBrowseInfo(info: String, removeParensFromOperator: Boolean): Option[BrowseInfo] = {
    info.split("::") match {
      case Array(qn, d) =>
        val (m, n) = getModuleAndName(qn, removeParensFromOperator)
        Some(BrowseInfo(n, m, Some(d)))
      case Array(qn) =>
        val (m, n) = getModuleAndName(qn, removeParensFromOperator)
        Some(BrowseInfo(n, m, None))
      case _ => None
    }
  }

  private def getModuleAndName(qualifiedName: String, removeParensFromOperator: Boolean): (String, String) = {
    val indexOfOperator = qualifiedName.lastIndexOf(".(") + 1
    if (indexOfOperator > 1) {
      val (m, o) = trimPair(qualifiedName.splitAt(indexOfOperator))
      (m.substring(0, m.length - 1), if (removeParensFromOperator) o.substring(1, o.length - 1) else o)
    } else {
      val indexOfId = qualifiedName.lastIndexOf('.') + 1
      val (m, id) = trimPair(qualifiedName.splitAt(indexOfId))
      (m.substring(0, m.length - 1), id)
    }
  }

  private def trimPair(t: (String, String)) = {
    t match {
      case (t0, t1) => (t0.trim, t1.trim)
    }
  }
}

case class BrowseInfo(name: String, moduleName: String, declaration: Option[String])
