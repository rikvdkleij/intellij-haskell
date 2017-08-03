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
import com.intellij.psi.PsiFile
import intellij.haskell.external.repl.{StackReplOutput, StackReplsManager}
import intellij.haskell.psi._
import intellij.haskell.util.LineColumnPosition

import scala.collection.JavaConverters._

object DefinitionLocationComponent {
  private final val Executor = Executors.newCachedThreadPool()

  private final val LocAtPattern = """(.+)\:\(([\d]+),([\d]+)\)-\(([\d]+),([\d]+)\)""".r
  private final val PackageModulePattern = """.+\:([\w\.\-]+)""".r

  private case class Key(startLineNr: Int, startColumnNr: Int, endLineNr: Int, endColumnNr: Int, expression: String, psiFile: PsiFile)

  private type DefinitionLocationResult = Either[NoLocationInfo, LocationInfo]

  private final val Cache = CacheBuilder.newBuilder()
    .build(
      new CacheLoader[Key, DefinitionLocationResult]() {

        override def load(key: Key): DefinitionLocationResult = {
          if (LoadComponent.isBusy(key.psiFile)) {
            Left(ReplIsLoading)
          } else {
            findDefinitionLocation(key)
          }
        }

        override def reload(key: Key, oldResult: DefinitionLocationResult): ListenableFuture[DefinitionLocationResult] = {
          val task = ListenableFutureTask.create[DefinitionLocationResult](() => {
            findDefinitionLocation(key)
          })
          Executor.execute(task)
          task
        }

        private def findDefinitionLocation(key: Key): DefinitionLocationResult = {
          val psiFile = key.psiFile
          val project = psiFile.getProject

          createLocationInfoWithEndColumnExcluded(project, psiFile, key)
        }

        private def createLocationInfoWithEndColumnExcluded(project: Project, psiFile: PsiFile, key: Key): DefinitionLocationResult = {
          findLocationInfoFor(key, psiFile, project, endColumnExcluded = true) match {
            case Some(o) => o.stdOutLines.headOption.map(createLocationInfo) match {
              case Some(r) => r
              case None => Left(NoLocationInfoAvailable)
            }
            case None => Left(ReplNotAvailable)
          }
        }

        private def findLocationInfoFor(key: Key, psiFile: PsiFile, project: Project, endColumnExcluded: Boolean): Option[StackReplOutput] = {
          val endColumnNr = if (endColumnExcluded) key.endColumnNr else key.endColumnNr - 1
          StackReplsManager.getProjectRepl(psiFile).flatMap(_.findLocationInfoFor(psiFile, key.startLineNr, key.startColumnNr, key.endLineNr, endColumnNr, key.expression))
        }

        private def createLocationInfo(output: String): DefinitionLocationResult = {
          output match {
            case LocAtPattern(filePath, startLineNr, startColumnNr, endLineNr, endColumnNr) => Right(DefinitionLocationInfo(filePath.trim, startLineNr.toInt, startColumnNr.toInt, endLineNr.toInt, endColumnNr.toInt))
            case PackageModulePattern(moduleName) => Right(ModuleLocationInfo(moduleName))
            case _ => Left(NoLocationInfoAvailable)
          }
        }
      }
    )

  def findDefinitionLocation(namedElement: HaskellNamedElement): Option[LocationInfo] = {
    (for {
      qne <- HaskellPsiUtil.findQualifiedNameParent(namedElement).map(_.getIdentifierElement)
      textOffset = qne.getTextOffset
      psiFile <- Option(namedElement.getContainingFile)
      sp <- LineColumnPosition.fromOffset(psiFile, textOffset)
      ep <- LineColumnPosition.fromOffset(psiFile, textOffset + qne.getText.length)
    } yield find(psiFile, sp, ep, qne.getName)) match {
      case Some(r) => r
      case None => None
    }
  }

  def invalidate(psiFile: PsiFile): Unit = {
    Cache.asMap().asScala.filter(_._1.psiFile == psiFile).keys.foreach(Cache.invalidate)
  }

  def invalidateAll(project: Project): Unit = {
    Cache.asMap().asScala.map(_._1.psiFile).filter(_.getProject == project).foreach(invalidate)
  }

  private def find(psiFile: PsiFile, startPosition: LineColumnPosition, endPosition: LineColumnPosition, expression: String): Option[LocationInfo] = {
    val key = Key(startPosition.lineNr, startPosition.columnNr, endPosition.lineNr, endPosition.columnNr, expression, psiFile)
    try {
      val result = Cache.get(key)
      result match {
        case Right(r) => Some(r)
        case Left(ReplNotAvailable) =>
          Cache.invalidate(key)
          None
        case Left(NoLocationInfoAvailable) =>
          None
        case Left(ReplIsLoading) =>
          Cache.refresh(key)
          None
      }
    }
    catch {
      case _: UncheckedExecutionException => None
      case _: ProcessCanceledException => None
    }
  }

  private sealed trait NoLocationInfo

  private case object NoLocationInfoAvailable extends NoLocationInfo

  private case object ReplIsLoading extends NoLocationInfo

  private case object ReplNotAvailable extends NoLocationInfo

}


sealed trait LocationInfo

case class DefinitionLocationInfo(filePath: String, startLineNr: Int, startColumnNr: Int, endLineNr: Int, endColumnNr: Int) extends LocationInfo

case class ModuleLocationInfo(moduleName: String) extends LocationInfo
