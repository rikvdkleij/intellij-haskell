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

import com.github.blemale.scaffeine.{AsyncLoadingCache, Scaffeine}
import com.intellij.openapi.project.Project
import intellij.haskell.external.component.HaskellComponentsManager.StackComponentInfo
import intellij.haskell.util.{HaskellProjectUtil, ScalaFutureUtil}

import scala.concurrent.duration._
import scala.concurrent.{Await, Future, blocking}

private[component] object StackComponentGlobalInfoComponent {

  import scala.concurrent.ExecutionContext.Implicits.global

  private case class Key(stackComponentInfo: StackComponentInfo)

  private type Result = Option[StackComponentGlobalInfo]

  private final val Cache: AsyncLoadingCache[Key, Result] = Scaffeine().buildAsync((k: Key) => createStackInfo(k))

  def findStackComponentGlobalInfo(stackComponentInfo: StackComponentInfo): Option[StackComponentGlobalInfo] = {
    val key = Key(stackComponentInfo)
    ScalaFutureUtil.waitForValue(stackComponentInfo.module.getProject, Cache.get(key), "Getting global info").flatten match {
      case result@Some(_) => result
      case _ =>
        Cache.synchronous.invalidate(key)
        None
    }
  }

  private def createStackInfo(key: Key): Result = {
    val project = key.stackComponentInfo.module.getProject
    val stackComponentInfo = key.stackComponentInfo
    findAvailableLibraryModuleNames(project, stackComponentInfo)
  }

  private def findAvailableLibraryModuleNames(project: Project, componentInfo: StackComponentInfo): Result = {
    val projectPackageNames = HaskellProjectUtil.findProjectPackageNames(project)
    val buildDependsLibraryPackages = componentInfo.buildDepends.filterNot(projectPackageNames.contains) ++ Seq("ghc-prim")

    val libraryModuleNamesFutures = buildDependsLibraryPackages.grouped(5).map { packageNames =>
      Future {
        blocking {
          packageNames.flatMap { packageName =>
            if (project.isDisposed) {
              None
            } else {
              LibraryPackageInfoComponent.findLibraryPackageInfo(project, packageName)
            }
          }
        }
      }
    }

    val libraryModuleNames = Await.result(Future.sequence(libraryModuleNamesFutures), 60.second).flatten.toSeq

    Some(StackComponentGlobalInfo(componentInfo, libraryModuleNames))
  }

  def invalidate(project: Project): Unit = {
    val keys = Cache.synchronous.asMap().keys.filter(_.stackComponentInfo.module.getProject == project)
    keys.foreach(Cache.synchronous.invalidate)
  }
}

case class StackComponentGlobalInfo(stackComponentInfo: StackComponentInfo, packageInfos: Seq[PackageInfo])
