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

import java.util.concurrent.TimeUnit

import com.github.blemale.scaffeine.{AsyncLoadingCache, Scaffeine}
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.search.GlobalSearchScope
import intellij.haskell.external.repl.StackRepl.StackReplOutput
import intellij.haskell.external.repl.StackReplsManager
import intellij.haskell.psi._
import intellij.haskell.util.LineColumnPosition
import intellij.haskell.util.index.HaskellFilePathIndex

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, TimeoutException}

private[component] object DefinitionLocationComponent {
  private final val LocAtPattern = """(.+)\:\(([\d]+),([\d]+)\)-\(([\d]+),([\d]+)\)""".r
  private final val PackageModulePattern = """.+\:([\w\.\-]+)""".r

  private case class Key(startLineNr: Int, startColumnNr: Int, endLineNr: Int, endColumnNr: Int, name: String, psiFile: PsiFile, moduleName: Option[String])

  type DefinitionLocationResult = Either[NoInfo, LocationInfo]

  private final val Cache: AsyncLoadingCache[Key, DefinitionLocationResult] = Scaffeine().buildAsync((k: Key) => findDefinitionLocation(k))

  def findDefinitionLocation(namedElement: HaskellNamedElement, waitIfBusy: Boolean): DefinitionLocationResult = {
    (for {
      qne <- HaskellPsiUtil.findQualifiedNameParent(namedElement).map(_.getIdentifierElement)
      textOffset = qne.getTextOffset
      psiFile <- Option(namedElement.getContainingFile)
      sp <- LineColumnPosition.fromOffset(psiFile, textOffset)
      ep <- LineColumnPosition.fromOffset(psiFile, textOffset + qne.getText.length)
    } yield find(psiFile, sp, ep, qne.getName, waitIfBusy)) match {
      case Some(r) => r
      case None => Left(NoInfoAvailable)
    }
  }

  def invalidate(psiFile: PsiFile): Unit = {
    val keys = Cache.synchronous().asMap().filter(_._1.psiFile == psiFile).keys
    Cache.synchronous().invalidateAll(keys)
  }

  def invalidateAll(project: Project): Unit = {
    val files = Cache.synchronous().asMap().map(_._1.psiFile).filter(_.getProject == project)
    files.foreach(invalidate)
  }

  private def findDefinitionLocation(key: Key): DefinitionLocationResult = {
    if (LoadComponent.isBusy(key.psiFile)) {
      Left(ReplIsBusy)
    } else {
      val psiFile = key.psiFile
      val project = psiFile.getProject

      if (key.name.headOption.exists(_.isUpper)) {
        createLocationInfo(project, psiFile, key, withoutLastColumn = true)
      } else {
        createLocationInfo(project, psiFile, key, withoutLastColumn = false)
      }
    }
  }

  private def createLocationInfo(project: Project, psiFile: PsiFile, key: Key, withoutLastColumn: Boolean): DefinitionLocationResult = {
    findLocationInfo(key, psiFile, project, withoutLastColumn) match {
      case Some(o) => o.stdoutLines.headOption.map(createLocationInfo) match {
        case Some(r) => r
        case None => Left(NoInfoAvailable)
      }
      case None => Left(ReplNotAvailable)
    }
  }

  private def findLocationInfo(key: Key, psiFile: PsiFile, project: Project, withoutLastColumn: Boolean): Option[StackReplOutput] = {
    val endColumnNr = if (withoutLastColumn) key.endColumnNr - 1 else key.endColumnNr
    StackReplsManager.getProjectRepl(psiFile).flatMap(_.findLocationInfo(key.moduleName, psiFile, key.startLineNr, key.startColumnNr, key.endLineNr, endColumnNr, key.name))
  }

  private def createLocationInfo(output: String): DefinitionLocationResult = {
    output match {
      case LocAtPattern(filePath, startLineNr, startColumnNr, endLineNr, endColumnNr) => Right(DefinitionLocationInfo(filePath.trim, startLineNr.toInt, startColumnNr.toInt, endLineNr.toInt, endColumnNr.toInt))
      case PackageModulePattern(moduleName) => Right(ModuleLocationInfo(moduleName))
      case _ => Left(NoInfoAvailable)
    }
  }

  private final val Timeout = Duration.create(100, TimeUnit.MILLISECONDS)

  private def find(psiFile: PsiFile, startPosition: LineColumnPosition, endPosition: LineColumnPosition, expression: String, waitIfBusy: Boolean): DefinitionLocationResult = {
    val project = psiFile.getProject
    val moduleName = HaskellFilePathIndex.findModuleName(psiFile, GlobalSearchScope.projectScope(project))
    val key = Key(startPosition.lineNr, startPosition.columnNr, endPosition.lineNr, endPosition.columnNr, expression, psiFile, moduleName)
    val result = try {
      Await.result(Cache.get(key), Timeout)
    } catch {
      case _: TimeoutException => Left(ReplIsBusy)
    }
    result match {
      case Right(_) => result
      case Left(ReplNotAvailable) =>
        Cache.synchronous().invalidate(key)
        result
      case Left(NoInfoAvailable) =>
        result
      case Left(ReplIsBusy) =>
        if (waitIfBusy && !project.isDisposed) {
          Thread.sleep(100)
          find(psiFile, startPosition, endPosition, expression, waitIfBusy)
        } else {
          Cache.synchronous().invalidate(key)
          result
        }
    }
  }
}


sealed trait LocationInfo

case class DefinitionLocationInfo(filePath: String, startLineNr: Int, startColumnNr: Int, endLineNr: Int, endColumnNr: Int) extends LocationInfo

case class ModuleLocationInfo(moduleName: String) extends LocationInfo
