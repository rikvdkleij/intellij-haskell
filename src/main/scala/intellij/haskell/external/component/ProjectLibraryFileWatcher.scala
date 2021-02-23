/*
 * Copyright 2014-2020 Rik van der Kleij
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

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.{VFileContentChangeEvent, VFileEvent}
import intellij.haskell.external.repl.StackRepl.LibType
import intellij.haskell.util.{HaskellFileUtil, HaskellProjectUtil}

import scala.jdk.CollectionConverters._

class ProjectLibraryFileWatcher(project: Project) extends BulkFileListener {

  override def before(events: util.List[_ <: VFileEvent]): Unit = {}

  override def after(events: util.List[_ <: VFileEvent]): Unit = {
    if (!project.isDisposed) {
      val componentTargets = (for {
        virtualFile <- events.asScala.filter(e => e.isInstanceOf[VFileContentChangeEvent] && HaskellFileUtil.isHaskellFile(e.getFile) && HaskellProjectUtil.isSourceFile(project, e.getFile)).map(_.getFile)
        componentTarget <- HaskellComponentsManager.findComponentTarget(project, HaskellFileUtil.getAbsolutePath(virtualFile))
        if componentTarget.stanzaType == LibType
      } yield componentTarget).toSet

      if (componentTargets.nonEmpty) {
        ProjectLibraryBuilder.addBuild(project, componentTargets)
      }
    }
  }
}
