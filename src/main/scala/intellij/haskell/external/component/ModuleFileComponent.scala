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

import java.util.concurrent.Executors

import com.google.common.cache.{CacheBuilder, CacheLoader}
import com.google.common.util.concurrent.{ListenableFuture, ListenableFutureTask, UncheckedExecutionException}
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.GlobalSearchScope
import intellij.haskell.util.index.HaskellFileIndex
import intellij.haskell.util.{HaskellEditorUtil, HaskellProjectUtil}
import intellij.haskell.{HaskellFile, HaskellNotificationGroup}

import scala.collection.JavaConverters._

private[component] object ModuleFileComponent {

  private final val Executor = Executors.newCachedThreadPool()

  private case class Key(project: Project, moduleName: String)

  private case class Result(files: Iterable[HaskellFile])

  private final val Cache = CacheBuilder.newBuilder()
    .build(
      new CacheLoader[Key, Result]() {

        override def load(key: Key): Result = {
          findFiles(key)
        }

        override def reload(key: Key, oldResult: Result): ListenableFuture[Result] = {
          val task = ListenableFutureTask.create[Result](() => {
            val newResult = findFiles(key)
            newResult.files match {
              case f if f.nonEmpty => newResult
              case _ => oldResult
            }
          })
          Executor.execute(task)
          task
        }

        private def findFiles(key: Key): Result = {
          Result(findFilesForModule(key.project, key.moduleName))
        }
      })

  def invalidate(project: Project): Unit = {
    Cache.asMap().asScala.filter(_._1.project == project).keys.foreach(Cache.invalidate)
  }

  def findHaskellFiles(project: Project, moduleName: String): Iterable[HaskellFile] = {
    if (AvailableModuleNamesComponent.findProjectProductionModuleNames(project).exists(_ == moduleName)) {
      findFilesForModule(project, moduleName)
    } else {
      val key = Key(project, moduleName)
      try {
        Cache.get(key).files
      }
      catch {
        case _: UncheckedExecutionException => None
        case _: ProcessCanceledException => None
      }
    }
  }

  private def findFilesForModule(project: Project, moduleName: String): Iterable[HaskellFile] = {
    for {
      fp <- findFilePathsForModule(moduleName, project)
      f <- HaskellProjectUtil.findFile(fp, project)
    } yield f
  }

  private def findFilePathsForModule(moduleName: String, project: Project): Iterable[String] = {
    getFileNameAndDirNamesForModule(project, moduleName).map(names => {
      val (fileName, dirNames) = names
      val filePaths = for {
        file <- HaskellFileIndex.findFilesByName(project, fileName, GlobalSearchScope.allScope(project))
        if checkDirNames(file.getParent, dirNames)
      } yield file.getPath

      if (filePaths.isEmpty) {
        // Test dependencies are supported since Stack 1.2.1, see https://github.com/commercialhaskell/stack/issues/1919
        HaskellEditorUtil.showStatusBarInfoMessage(s"Could not find source code for `$moduleName`. Please use `Download Haskell library sources` in `Tools` from menu.", project)
        filePaths
      } else {
        filePaths
      }
    }).getOrElse(Iterable())
  }

  private def getFileNameAndDirNamesForModule(project: Project, module: String) = {
    module.split('.').toList.reverse match {
      case n :: d => Some(n, d)
      case _ => HaskellNotificationGroup.logWarningEvent(project, s"Could not determine directory names for $module"); None
    }
  }

  private def checkDirNames(dir: VirtualFile, dirNames: List[String]): Boolean = {
    dirNames match {
      case dirName :: parentDirName =>
        if (dir.getName == dirName)
          checkDirNames(dir.getParent, parentDirName)
        else
          false
      case _ => true
    }
  }
}

