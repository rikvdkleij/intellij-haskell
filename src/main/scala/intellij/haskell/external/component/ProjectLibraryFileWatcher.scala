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

import java.util
import java.util.concurrent.ConcurrentHashMap

import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.util.ReadTask.Continuation
import com.intellij.openapi.progress.util.{ProgressIndicatorUtils, ReadTask}
import com.intellij.openapi.project.{DumbService, Project}
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.{VFileContentChangeEvent, VFileEvent}
import intellij.haskell.external.repl.StackReplsManager.StackComponentInfo
import intellij.haskell.util.HaskellFileUtil
import intellij.haskell.util.index.HaskellFileNameIndex

import scala.collection.JavaConverters._
import scala.collection.concurrent

object ProjectLibraryFileWatcher {
  val changedLibrariesByPackageName: concurrent.Map[String, StackComponentInfo] = new ConcurrentHashMap[String, StackComponentInfo]().asScala
}

class ProjectLibraryFileWatcher(project: Project) extends BulkFileListener {

  override def before(events: util.List[_ <: VFileEvent]): Unit = {}

  override def after(events: util.List[_ <: VFileEvent]): Unit = {
    val readTask = new ReadTask {

      override def runBackgroundProcess(indicator: ProgressIndicator): Continuation = {
        DumbService.getInstance(project).runReadActionInSmartMode(() => {
          performInReadAction(indicator)
        })
      }

      override def computeInReadAction(indicator: ProgressIndicator): Unit = {
        if (!project.isDisposed) {
          indicator.checkCanceled()
          val watchFiles = HaskellFileNameIndex.findProjectProductionFiles(project)
          indicator.checkCanceled()
          for {
            virtualFile <- watchFiles.find(vf => events.asScala.exists(e => e.isInstanceOf[VFileContentChangeEvent] && e.getPath == vf.getPath))
            haskellFile <- HaskellFileUtil.convertToHaskellFile(project, virtualFile)
            info <- HaskellComponentsManager.findStackComponentInfo(haskellFile)
          } yield ProjectLibraryFileWatcher.changedLibrariesByPackageName.put(info.packageName, info)
        }
      }

      override def onCanceled(indicator: ProgressIndicator): Unit = {
        ProgressIndicatorUtils.scheduleWithWriteActionPriority(this)
      }
    }
    ProgressIndicatorUtils.scheduleWithWriteActionPriority(readTask)
  }

}
