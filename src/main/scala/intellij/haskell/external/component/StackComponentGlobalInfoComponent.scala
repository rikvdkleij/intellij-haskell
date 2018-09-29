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

import java.util.concurrent.Executors

import com.github.blemale.scaffeine.{LoadingCache, Scaffeine}
import com.intellij.openapi.project.Project
import intellij.haskell.external.component.HaskellComponentsManager.StackComponentInfo
import intellij.haskell.module.HaskellModuleBuilder

import scala.concurrent.{Await, ExecutionContext, ExecutionContextExecutorService, Future}

private[component] object StackComponentGlobalInfoComponent {

  private case class Key(stackComponentInfo: StackComponentInfo)

  private type Result = Option[StackComponentGlobalInfo]

  private final val Cache: LoadingCache[Key, Result] = Scaffeine().build((k: Key) => createStackInfo(k))

  def findStackComponentGlobalInfo(stackComponentInfo: StackComponentInfo): Option[StackComponentGlobalInfo] = {
    val key = Key(stackComponentInfo)
    Cache.get(key) match {
      case result@Some(_) => result
      case _ =>
        Cache.invalidate(key)
        None
    }
  }

  private def createStackInfo(key: Key): Result = {
    val project = key.stackComponentInfo.module.getProject
    val stackComponentInfo = key.stackComponentInfo
    findAvailableModuleNames(project, stackComponentInfo)
  }

  private val ExecutorService = Executors.newCachedThreadPool()
  implicit val ExecContext: ExecutionContextExecutorService = ExecutionContext.fromExecutorService(ExecutorService)

  import scala.concurrent.duration._

  private def findAvailableModuleNames(project: Project, componentInfo: StackComponentInfo): Result = {
    val allDependencies = HaskellModuleBuilder.getDependencies(project, componentInfo.module, componentInfo.target, None)
    val packageNameLibraryModuleNamesFutures = allDependencies.grouped(5).map { dependencies =>
      Future {
        dependencies.flatMap { d =>
          val name = d.name
          if (project.isDisposed) {
            None
          } else {
            LibraryModuleNamesComponent.findLibraryModuleNames(project, name).map((name, _))
          }
        }
      }
    }

    val packageNameLibraryModuleNames = Await.result(Future.sequence(packageNameLibraryModuleNamesFutures), 60.second).flatten.toIterable

    val availableDependencyPackages = HaskellModuleBuilder.getDependencies(project, componentInfo.module, componentInfo.target, Some(1)).map(_.name).toSeq
    val availableLibraryModuleNames = packageNameLibraryModuleNames.filter { case (n, _) => availableDependencyPackages.contains(n) }.map(_._2)
    if (packageNameLibraryModuleNames.isEmpty) {
      None
    } else {
      Some(StackComponentGlobalInfo(componentInfo, availableLibraryModuleNames, packageNameLibraryModuleNames.map(_._2)))
    }
  }

  def invalidate(project: Project): Unit = {
    val keys = Cache.asMap().keys.filter(_.stackComponentInfo.module.getProject == project)
    keys.foreach(Cache.invalidate)
  }
}

case class StackComponentGlobalInfo(stackComponentInfo: StackComponentInfo, availableLibraryModuleNames: Iterable[LibraryModuleNames], allLibraryModuleNames: Iterable[LibraryModuleNames])
