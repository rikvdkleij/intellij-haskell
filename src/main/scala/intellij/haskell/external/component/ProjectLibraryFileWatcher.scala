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

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.util.ReadTask.Continuation
import com.intellij.openapi.progress.util.{ProgressIndicatorUtils, ReadTask}
import com.intellij.openapi.project.{DumbService, Project}
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.{VFileContentChangeEvent, VFileEvent}
import intellij.haskell.external.component.ProjectLibraryFileWatcher.{Build, Building}
import intellij.haskell.external.execution.StackCommandLine
import intellij.haskell.external.repl.StackRepl.LibType
import intellij.haskell.external.repl.StackReplsManager.StackComponentInfo
import intellij.haskell.util.HaskellFileUtil
import intellij.haskell.util.index.HaskellFileNameIndex

import scala.collection.JavaConverters._
import scala.collection.concurrent

object ProjectLibraryFileWatcher {
  val builtLibraries: concurrent.Map[String, StackComponentInfo] = new ConcurrentHashMap[String, StackComponentInfo]().asScala

  trait BuildStatus

  case object Building extends BuildStatus

  case object Build extends BuildStatus

  private val buildStatus = new ConcurrentHashMap[String, BuildStatus]().asScala
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
            componentInfo <- HaskellComponentsManager.findStackComponentInfo(haskellFile)
          } yield
            if (componentInfo.stanzaType == LibType) {
              this.synchronized {
                if (ProjectLibraryFileWatcher.buildStatus.get(componentInfo.packageName).contains(Building)) {
                  ProjectLibraryFileWatcher.buildStatus.put(componentInfo.packageName, Build)
                } else if (ProjectLibraryFileWatcher.buildStatus.get(componentInfo.packageName).isEmpty) {
                  ProjectLibraryFileWatcher.buildStatus.put(componentInfo.packageName, Building)
                  ApplicationManager.getApplication.executeOnPooledThread(new Runnable() {

                    override def run(): Unit = {
                      val output = StackCommandLine.build(project, componentInfo.target, logBuildResult = false, fast = true)
                      if (output.exists(_.getExitCode == 0)) {
                        ProjectLibraryFileWatcher.builtLibraries.put(componentInfo.packageName, componentInfo)
                      }
                      val status = ProjectLibraryFileWatcher.buildStatus.get(componentInfo.packageName)
                      if (status.contains(Build)) {
                        ProjectLibraryFileWatcher.buildStatus.put(componentInfo.packageName, Building)
                        run()
                      } else {
                        ProjectLibraryFileWatcher.buildStatus.remove(componentInfo.packageName)
                      }
                    }
                  })
                }
              }
            }
        }
      }

      override def onCanceled(indicator: ProgressIndicator): Unit = {
        ProgressIndicatorUtils.scheduleWithWriteActionPriority(this)
      }
    }

    ProgressIndicatorUtils.scheduleWithWriteActionPriority(readTask)
  }

}
