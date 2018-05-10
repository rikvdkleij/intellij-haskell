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

import com.github.blemale.scaffeine.{LoadingCache, Scaffeine}
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import intellij.haskell.external.repl.StackRepl.StackReplOutput
import intellij.haskell.external.repl.StackReplsManager
import intellij.haskell.psi._
import intellij.haskell.util.LineColumnPosition

object DefinitionLocationComponent {
  private final val LocAtPattern = """(.+)\:\(([\d]+),([\d]+)\)-\(([\d]+),([\d]+)\)""".r
  private final val PackageModulePattern = """.+\:([\w\.\-]+)""".r

  private case class Key(startLineNr: Int, startColumnNr: Int, endLineNr: Int, endColumnNr: Int, name: String, psiFile: PsiFile)

  type DefinitionLocationResult = Either[NoInfo, LocationInfo]

  private final val Cache: LoadingCache[Key, DefinitionLocationResult] = Scaffeine().build((k: Key) => findDefinitionLocation(k))

  def findDefinitionLocation(namedElement: HaskellNamedElement): Option[DefinitionLocationResult] = {
    (for {
      qne <- HaskellPsiUtil.findQualifiedNameParent(namedElement).map(_.getIdentifierElement)
      textOffset = qne.getTextOffset
      psiFile <- Option(namedElement.getContainingFile)
      sp <- LineColumnPosition.fromOffset(psiFile, textOffset)
      ep <- LineColumnPosition.fromOffset(psiFile, textOffset + qne.getText.length)
    } yield find(psiFile, sp, ep, qne.getName)) match {
      case r@Some(_) => r
      case None => None
    }
  }

  def invalidate(psiFile: PsiFile): Unit = {
    Cache.asMap().filter(_._1.psiFile == psiFile).keys.foreach(Cache.invalidate)
  }

  def invalidateAll(project: Project): Unit = {
    Cache.asMap().map(_._1.psiFile).filter(_.getProject == project).foreach(invalidate)
  }

  private def findDefinitionLocation(key: Key): DefinitionLocationResult = {
    if (LoadComponent.isBusy(key.psiFile)) {
      Left(ReplIsBusy)
    } else {
      val psiFile = key.psiFile
      val project = psiFile.getProject

      if (key.name.headOption.exists(_.isUpper)) {
        createLocationInfoWithEndColumnExcluded(project, psiFile, key, withoutLastColumn = true)
      } else {
        createLocationInfoWithEndColumnExcluded(project, psiFile, key, withoutLastColumn = false)
      }
    }
  }

  private def createLocationInfoWithEndColumnExcluded(project: Project, psiFile: PsiFile, key: Key, withoutLastColumn: Boolean): DefinitionLocationResult = {
    findLocationInfoFor(key, psiFile, project, withoutLastColumn) match {
      case Some(o) => o.stdoutLines.headOption.map(createLocationInfo) match {
        case Some(r) => r
        case None => Left(NoInfoAvailable)
      }
      case None => Left(ReplNotAvailable)
    }
  }

  private def findLocationInfoFor(key: Key, psiFile: PsiFile, project: Project, withoutLastColumn: Boolean): Option[StackReplOutput] = {
    val endColumnNr = if (withoutLastColumn) key.endColumnNr - 1 else key.endColumnNr
    StackReplsManager.getProjectRepl(psiFile).flatMap(_.findLocationInfoFor(psiFile, key.startLineNr, key.startColumnNr, key.endLineNr, endColumnNr, key.name))
  }

  private def createLocationInfo(output: String): DefinitionLocationResult = {
    output match {
      case LocAtPattern(filePath, startLineNr, startColumnNr, endLineNr, endColumnNr) => Right(DefinitionLocationInfo(filePath.trim, startLineNr.toInt, startColumnNr.toInt, endLineNr.toInt, endColumnNr.toInt))
      case PackageModulePattern(moduleName) => Right(ModuleLocationInfo(moduleName))
      case _ => Left(NoInfoAvailable)
    }
  }

  private def find(psiFile: PsiFile, startPosition: LineColumnPosition, endPosition: LineColumnPosition, expression: String): DefinitionLocationResult = {
    val key = Key(startPosition.lineNr, startPosition.columnNr, endPosition.lineNr, endPosition.columnNr, expression, psiFile)
    val result = Cache.get(key)
    result match {
      case Right(_) => result
      case Left(ReplNotAvailable) =>
        Cache.invalidate(key)
        result
      case Left(NoInfoAvailable) =>
        result
      case Left(ReplIsBusy) =>
        Cache.invalidate(key)
        result
    }
  }
}


sealed trait LocationInfo

case class DefinitionLocationInfo(filePath: String, startLineNr: Int, startColumnNr: Int, endLineNr: Int, endColumnNr: Int) extends LocationInfo

case class ModuleLocationInfo(moduleName: String) extends LocationInfo
