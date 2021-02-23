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

import java.nio.file.Paths

import com.github.blemale.scaffeine.{AsyncLoadingCache, Scaffeine}
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtil
import com.intellij.psi.PsiFile
import intellij.haskell.HaskellNotificationGroup
import intellij.haskell.external.component.HaskellComponentsManager.ComponentTarget
import intellij.haskell.external.repl.StackRepl.LibType
import intellij.haskell.external.repl.StackReplsManager
import intellij.haskell.runconfig.console.HaskellConsoleView
import intellij.haskell.util.{HaskellFileUtil, HaskellProjectUtil, ScalaFutureUtil, ScalaUtil}

private[component] object HaskellModuleInfoComponent {

  private case class InternalComponentTarget(target: Option[ComponentTarget], message: Option[String])

  private case class Key(project: Project, filePath: String)

  private final val Cache: AsyncLoadingCache[Key, Option[InternalComponentTarget]] = Scaffeine().buildAsync((k: Key) => createInternalComponentTarget(k))

  def findComponentTarget(project: Project, filePath: String): Option[ComponentTarget] = {
    val key = Key(project, filePath)
    ScalaFutureUtil.waitForValue(project, Cache.get(key), s"getting component target for file: $filePath").flatten match {
      case Some(internalInfo) =>
        internalInfo.message match {
          case Some(m) => HaskellNotificationGroup.warningEvent(project, m)
          case None => None
        }
        internalInfo.target match {
          case Some(target) => Some(target)
          case _ => None
        }
      case _ =>
        Cache.synchronous().invalidate(key)
        None
    }
  }

  def findComponentTarget(psiFile: PsiFile): Option[ComponentTarget] = {
    val project = psiFile.getProject

    HaskellConsoleView.findConsoleInfo(psiFile) match {
      case Some(consoleInfo) =>
        val targets = StackReplsManager.getReplsManager(project).map(_.componentTargets)
        targets.flatMap(_.find(_.target == consoleInfo.stackTarget))
      case None =>
        val filePath = HaskellFileUtil.getAbsolutePath(psiFile)
        filePath.flatMap(k => findComponentTarget(project, k))
    }
  }

  def invalidate(project: Project): Unit = {
    val keys = Cache.synchronous().asMap().keys.filter(_.project == project)
    keys.foreach(Cache.synchronous().invalidate)
  }

  def invalidate(psiFile: PsiFile): Unit = {
    HaskellFileUtil.getAbsolutePath(psiFile).foreach(fp => Cache.synchronous().invalidate(Key(psiFile.getProject, fp)))
  }

  private def createInternalComponentTarget(key: Key): Option[InternalComponentTarget] = {
    val project = key.project
    val filePath = key.filePath

    StackReplsManager.getReplsManager(project).map(_.componentTargets).map(stackComponentInfos => {
      val target = findComponentTarget(filePath, stackComponentInfos)
      InternalComponentTarget(target._1, target._2)
    })
  }

  // Messages are not displayed directly here because it can cause deadlock
  // For some reason a notify can trigger ProjectLibraryFileWatcher which also asks for HaskellProjectFileInfo
  private def findComponentTarget(filePath: String, componentTargets: Iterable[ComponentTarget]): (Option[ComponentTarget], Option[String]) = {
    componentTargets.find(_.mainIs.exists(filePath.contains)) match {
      case info@Some(_) => (info, None)
      case None =>
        val sourceDirsByInfo = componentTargets.map(target => (target, target.sourceDirs.filter(sd => FileUtil.isAncestor(sd, filePath, true)))).filterNot({ case (_, dir) => dir.isEmpty })
        val (target, message) = if (sourceDirsByInfo.size > 1) {
          val sourceDirByInfo = sourceDirsByInfo.map({ case (target, dirs) => (target, dirs.maxBy(sd => Paths.get(sd).getNameCount)) })
          val mostSpecificSourceDirByInfo = ScalaUtil.maxsBy(sourceDirByInfo)({ case (_, dir) => Paths.get(dir).getNameCount })

          val target = mostSpecificSourceDirByInfo.find(_._1.stanzaType == LibType)

          val message = if (mostSpecificSourceDirByInfo.size > 1) {
            Some(s"Ambiguous Stack target for file `$filePath`. It can belong to the source dir of more than one Stack target/Cabal stanza: `${mostSpecificSourceDirByInfo.map(_._1.target)}`  |  `${target.map(_._1.target).getOrElse(mostSpecificSourceDirByInfo.map(_._1.target).headOption.getOrElse("-"))}` is chosen.")
          } else {
            None
          }
          (target.map(_._1).orElse(mostSpecificSourceDirByInfo.headOption.map(_._1)), message)
        } else {
          (sourceDirsByInfo.headOption.map(_._1), None)
        }
        target match {
          case Some(_) => (target, message)
          case None =>
            componentTargets.find(info => info.stanzaType == LibType && FileUtil.isAncestor(HaskellProjectUtil.getModuleDir(info.module).getAbsolutePath, filePath, true)) match {
              case Some(target) => (Some(target), None)
              case None => val message = Some(s"Could not determine Stack target for file `$filePath` because no accompanying `hs-source-dirs` or `main-is` can be found in Cabal file(s)")
                (None, message)
            }
        }
    }
  }
}