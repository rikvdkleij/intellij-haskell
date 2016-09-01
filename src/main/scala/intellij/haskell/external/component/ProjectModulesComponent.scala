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
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Computable
import com.intellij.psi.PsiFile
import intellij.haskell.psi.HaskellPsiUtil
import intellij.haskell.util.{HaskellFileIndex, HaskellProjectUtil}

private[component] object ProjectModulesComponent {

  private case class Key(project: Project)

  private val executor = Executors.newCachedThreadPool()

  private final val Cache = CacheBuilder.newBuilder()
    .build(
      new CacheLoader[Key, ProjectModules]() {

        override def load(key: Key): ProjectModules = {
          createProjectModules(key)
        }

        override def reload(key: Key, oldValue: ProjectModules): ListenableFuture[ProjectModules] = {
          val task = ListenableFutureTask.create(new Callable[ProjectModules]() {
            def call() = {
              createProjectModules(key)
            }
          })
          executor.execute(task)
          task
        }

        private def createProjectModules(key: Key): ProjectModules = {
          val project = key.project
          val moduleNamesWithFile = findProjectModules(project)
          val testAndProdModuleNamesWithFile = moduleNamesWithFile.partition(mnf => HaskellProjectUtil.isProjectTestFile(mnf._2))
          ProjectModules(moduleNamesWithFile.map(_._1), testAndProdModuleNamesWithFile._2.map(_._1), testAndProdModuleNamesWithFile._1.map(_._1))
        }

        private def findProjectModules(project: Project) = {
          ApplicationManager.getApplication.runReadAction {
            new Computable[Iterable[(String, PsiFile)]] {
              override def compute(): Iterable[(String, PsiFile)] = {
                HaskellFileIndex.findProjectHaskellFiles(project).flatMap(f => HaskellPsiUtil.findModuleName(f).map(mn => (mn, f)))
              }
            }
          }
        }
      }
    )

  def findAvailableModules(project: Project): ProjectModules = {
    try {
      Cache.get(Key(project))
    }
    catch {
      case _: UncheckedExecutionException => ProjectModules()
      case _: ProcessCanceledException => ProjectModules()
    }
  }

  def refresh(project: Project): Unit = {
    Cache.refresh(Key(project))
  }

  def invalidate(project: Project): Unit = {
    Cache.invalidate(Key(project))
  }
}

case class ProjectModules(allModuleNames: Iterable[String] = Iterable(), prodModuleNames: Iterable[String] = Iterable(), testModuleNames: Iterable[String] = Iterable())
