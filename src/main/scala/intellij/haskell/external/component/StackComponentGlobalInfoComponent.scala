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

import com.github.blemale.scaffeine.{LoadingCache, Scaffeine}
import com.intellij.openapi.project.Project
import intellij.haskell.external.repl.StackRepl.StackReplOutput
import intellij.haskell.external.repl.StackReplsManager
import intellij.haskell.external.repl.StackReplsManager.StackComponentInfo

private[component] object StackComponentGlobalInfoComponent {

  private case class Key(stackComponentInfo: StackComponentInfo)

  private type Result = Either[NoInfo, StackComponentGlobalInfo]

  private final val Cache: LoadingCache[Key, Result] = Scaffeine().build((k: Key) => load(k))

  def findStackComponentGlobalInfo(stackComponentInfo: StackComponentInfo): Option[StackComponentGlobalInfo] = {
    val key = Key(stackComponentInfo)
    Cache.get(key) match {
      case Right(result) => Some(result)
      case Left(_) =>
        Cache.invalidate(key)
        None
    }
  }

  private def load(key: Key): Result = {
    if (LoadComponent.isBusy(key.stackComponentInfo.module.getProject, key.stackComponentInfo)) {
      Left(ReplIsBusy)
    } else {
      createStackInfo(key)
    }
  }

  private def createStackInfo(key: Key): Result = {
    val project = key.stackComponentInfo.module.getProject
    val stackComponentInfo = key.stackComponentInfo
    findAvailableModuleNames(project, stackComponentInfo) match {
      case Left(noInfo) => Left(noInfo)
      case Right(mn) => isNoImplicitPreludeActive(project, stackComponentInfo) match {
        case Left(noInfo) => Left(noInfo)
        case Right(noImplicitPrelude) => Right(StackComponentGlobalInfo(stackComponentInfo, mn, noImplicitPrelude))
      }
    }
  }

  private def findAvailableModuleNames(project: Project, componentInfo: StackComponentInfo): Either[NoInfo, Iterable[String]] = {
    StackReplsManager.getProjectRepl(project, componentInfo).flatMap(_.findAvailableLibraryModuleNames(project)) match {
      case Some(o) => Right(getModuleNames(o))
      case None => Left(ReplNotAvailable)
    }
  }

  private def isNoImplicitPreludeActive(project: Project, stackTargetBuildInfo: StackComponentInfo): Either[NoInfo, Boolean] = {
    StackReplsManager.getProjectRepl(project, stackTargetBuildInfo).flatMap(_.showActiveLanguageFlags) match {
      case Some(o) => Right(o.stdoutLines.mkString(" ").contains("-XNoImplicitPrelude"))
      case None => Left(ReplNotAvailable)
    }
  }

  private def getModuleNames(output: StackReplOutput) = {
    val lines = output.stdoutLines
    if (lines.isEmpty) {
      Iterable()
    } else {
      lines.tail.map(m => m.substring(1, m.length - 1))
    }
  }

  def invalidate(project: Project): Unit = {
    val keys = Cache.asMap().keys.filter(_.stackComponentInfo.module.getProject == project)
    keys.foreach(Cache.invalidate)
  }
}

case class StackComponentGlobalInfo(stackComponentInfo: StackComponentInfo, availableLibraryModuleNames: Iterable[String], noImplicitPreludeActive: Boolean)
