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

import com.github.blemale.scaffeine.{LoadingCache, Scaffeine}
import com.intellij.openapi.project.Project
import intellij.haskell.external.component.HaskellComponentsManager.StackComponentInfo
import intellij.haskell.util.HaskellProjectUtil

import scala.concurrent.duration._
import scala.concurrent.{Await, Future, blocking}

private[component] object StackComponentGlobalInfoComponent {

  import scala.concurrent.ExecutionContext.Implicits.global

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
    findAvailableLibraryModuleNames(project, stackComponentInfo)
  }

  private def findAvailableLibraryModuleNames(project: Project, componentInfo: StackComponentInfo): Result = {
    val projectPackageNames = HaskellProjectUtil.findProjectPackageNames(project)
    val buildDependsLibraryPackages = componentInfo.buildDepends.filterNot(projectPackageNames.contains)

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
    val keys = Cache.asMap().keys.filter(_.stackComponentInfo.module.getProject == project)
    keys.foreach(Cache.invalidate)
  }
}

case class StackComponentGlobalInfo(stackComponentInfo: StackComponentInfo, packageInfos: Seq[PackageInfo])
