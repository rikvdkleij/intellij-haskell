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
import com.intellij.openapi.util.io.FileUtil
import com.intellij.psi.PsiFile
import intellij.haskell.HaskellNotificationGroup
import intellij.haskell.external.repl.StackReplsManager
import intellij.haskell.external.repl.StackReplsManager.StackComponentInfo
import intellij.haskell.runconfig.console.HaskellConsoleView
import intellij.haskell.util.HaskellFileUtil

import scala.collection.JavaConverters._

private[component] object HaskellProjectFileInfoComponent {

  private case class Key(psiFile: PsiFile)

  private val executor = Executors.newCachedThreadPool()

  private final val Cache = CacheBuilder.newBuilder()
    .build(
      new CacheLoader[Key, Option[HaskellProjectFileInfo]]() {

        override def load(key: Key): Option[HaskellProjectFileInfo] = {
          createHaskellFileInfo(key)
        }

        override def reload(key: Key, oldValue: Option[HaskellProjectFileInfo]): ListenableFuture[Option[HaskellProjectFileInfo]] = {
          val task = ListenableFutureTask.create[Option[HaskellProjectFileInfo]](() => {
            createHaskellFileInfo(key)
          })
          executor.execute(task)
          task
        }

        private def createHaskellFileInfo(key: Key): Option[HaskellProjectFileInfo] = {
          val psiFile = key.psiFile
          val project = psiFile.getProject

          StackReplsManager.getReplsManager(project).map(_.stackComponentInfos).flatMap(stackComponentInfos => {
            HaskellConsoleView.findConsoleInfo(psiFile).flatMap(consoleInfo => stackComponentInfos.find(_.target == consoleInfo.stackTarget)) match {
              case Some(componentInfo) => Some(HaskellProjectFileInfo(componentInfo))
              case None => getStackComponentInfo(psiFile, stackComponentInfos).map(buildInfo => HaskellProjectFileInfo(buildInfo))
            }
          })
        }

        private def getStackComponentInfo(psiFile: PsiFile, stackTargetBuildInfos: Iterable[StackComponentInfo]): Option[StackComponentInfo] = {
          val filePath = HaskellFileUtil.getAbsolutePath(psiFile)
          stackTargetBuildInfos.find(_.mainIs.exists(mi => mi == filePath)) match {
            case info@Some(_) => info
            case None =>
              val infos = stackTargetBuildInfos.filter(_.sourceDirs.exists(sd => FileUtil.isAncestor(sd, filePath, true))).toSeq
              if (infos.size > 1) {
                HaskellNotificationGroup.logWarningBalloonEvent(psiFile.getProject, s"Ambiguous Stack target: ${psiFile.getName} belongs to the source dir of more than one Stack target/Cabal stanza. The first one of ${infos.map(_.target)} is chosen.")
              }
              infos.headOption
              match {
                case info@Some(_) => info
                case None =>
                  HaskellNotificationGroup.logErrorBalloonEvent(psiFile.getProject, s"Can not determine Stack target for file ${psiFile.getName} because no accompanying `hs-source-dirs` or `main-is` can be found in Cabal file(s)")
                  None
              }
          }
        }
      }
    )

  def findHaskellProjectFileInfo(psiFile: PsiFile): Option[HaskellProjectFileInfo] = {
    try {
      val key = Key(psiFile)
      Cache.get(key) match {
        case result@Some(_) => result
        case _ =>
          Cache.invalidate(key)
          None
      }
    }
    catch {
      case _: UncheckedExecutionException => None
      case _: ProcessCanceledException => None
    }
  }

  def invalidate(project: Project): Unit = {
    val keys = Cache.asMap().keySet().asScala.filter(_.psiFile.getProject == project)
    keys.foreach(Cache.invalidate)
  }

  def invalidate(psiFile: PsiFile): Unit = {
    Cache.invalidate(Key(psiFile))
  }
}

case class HaskellProjectFileInfo(stackComponentInfo: StackComponentInfo)

