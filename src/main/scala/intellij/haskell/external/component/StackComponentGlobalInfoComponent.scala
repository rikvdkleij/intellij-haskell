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
import com.intellij.psi.PsiFile
import intellij.haskell.external.repl.StackReplsManager.StackComponentInfo
import intellij.haskell.external.repl.{StackReplOutput, StackReplsManager}

import scala.collection.JavaConverters._

private[component] object StackComponentGlobalInfoComponent {

  private case class Key(project: Project, stackComponentInfo: StackComponentInfo)

  private val executor = Executors.newCachedThreadPool()

  private type Result = Either[NoStackComponentGlobalInfo, StackComponentGlobalInfo]

  private final val Cache = CacheBuilder.newBuilder()
    .build(
      new CacheLoader[Key, Result]() {

        override def load(key: Key): Result = {
          if (LoadComponent.isLoading(key.project)) {
            Left(ReplNotAvailable)
          } else {
            createStackInfo(key)
          }
        }

        override def reload(key: Key, oldValue: Result): ListenableFuture[Result] = {
          val task = ListenableFutureTask.create[Result](() => {
            createStackInfo(key)
          })
          executor.execute(task)
          task
        }

        private def createStackInfo(key: Key): Result = {
          val project = key.project
          val stackComponentInfo = key.stackComponentInfo
          (for {
            moduleNames <- findAvailableModuleNames(project, stackComponentInfo)
            noImplicitPrelude <- isNoImplicitPreludeActive(project, stackComponentInfo)
          } yield StackComponentGlobalInfo(stackComponentInfo, moduleNames, noImplicitPrelude)).map(r => Right(r)).getOrElse(Left(NoStackComponentGlobalInfoAvailable))
        }

        private def findAvailableModuleNames(project: Project, componentInfo: StackComponentInfo): Option[Iterable[String]] = {
          StackReplsManager.getProjectRepl(project, componentInfo).flatMap(_.findAvailableLibraryModuleNames).map(findModuleNames)
        }

        private def isNoImplicitPreludeActive(project: Project, stackTargetBuildInfo: StackComponentInfo): Option[Boolean] = {
          StackReplsManager.getProjectRepl(project, stackTargetBuildInfo).flatMap(_.showActiveLanguageFlags).map(_.stdOutLines).map(_.exists(_.contains("-XNoImplicitPrelude")))
        }

        private def findModuleNames(output: StackReplOutput) = {
          val lines = output.stdOutLines
          if (lines.isEmpty) {
            Iterable()
          } else {
            lines.tail.map(m => m.substring(1, m.length - 1))
          }
        }
      }
    )

  def findStackComponentGlobalInfo(psiFile: PsiFile): Option[StackComponentGlobalInfo] = {
    HaskellComponentsManager.findStackComponentInfo(psiFile).flatMap(info => {
      try {
        val key = Key(psiFile.getProject, info)
        Cache.get(key) match {
          case Right(result) => Some(result)
          case Left(NoStackComponentGlobalInfoAvailable) =>
            Cache.invalidate(key)
            None
          case Left(ReplNotAvailable) =>
            Cache.refresh(key)
            None
        }
      }
      catch {
        case _: UncheckedExecutionException => None
        case _: ProcessCanceledException => None
      }
    })
  }

  def invalidate(project: Project): Unit = {
    val keys = Cache.asMap().keySet().asScala.filter(_.project == project)
    keys.foreach(Cache.invalidate)
  }

  private sealed trait NoStackComponentGlobalInfo

  private case object NoStackComponentGlobalInfoAvailable extends NoStackComponentGlobalInfo

  private case object ReplNotAvailable extends NoStackComponentGlobalInfo

}

case class StackComponentGlobalInfo(stackComponentInfo: StackComponentInfo, availableLibraryModuleNames: Iterable[String], noImplicitPreludeActive: Boolean)
