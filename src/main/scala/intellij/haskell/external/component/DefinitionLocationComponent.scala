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
import com.intellij.psi.{PsiElement, PsiFile}
import intellij.haskell.external.repl.{StackReplOutput, StackReplsManager}
import intellij.haskell.psi._
import intellij.haskell.util.LineColumnPosition

import scala.collection.JavaConverters._

private[component] object DefinitionLocationComponent {
  private final val Executor = Executors.newCachedThreadPool()

  private final val LocAtPattern = """(.+)\:\(([\d]+),([\d]+)\)-\(([\d]+),([\d]+)\)""".r
  private final val PackageModulePattern = """.+\:([\w\.\-]+)""".r

  private case class Key(startLineNr: Int, startColumnNr: Int, endLineNr: Int, endColumnNr: Int, expression: String, psiFile: PsiFile)

  private case class Result(location: Either[String, Option[LocationInfo]])

  private final val Cache = CacheBuilder.newBuilder()
    .build(
      new CacheLoader[Key, Result]() {

        override def load(key: Key): Result = {
          createDefinitionLocation(key)
        }

        override def reload(key: Key, oldResult: Result): ListenableFuture[Result] = {
          val task = ListenableFutureTask.create[Result](() => {
            val newResult = createDefinitionLocation(key)
            newResult.location match {
              case Right(o) if o.isDefined => newResult
              case _ => oldResult
            }
          })
          Executor.execute(task)
          task
        }

        private def createDefinitionLocation(key: Key): Result = {
          val psiFile = key.psiFile
          val project = psiFile.getProject
          val location = (findLocationInfoFor(key, psiFile, project) match {
            case Some(output) => Right(output)
            case _ => Left("No info available")
          }).right.map(_.stdOutLines.headOption.flatMap(createDefinitionLocationInfo))
          Result(location)
        }

        private def findLocationInfoFor(key: Key, psiFile: PsiFile, project: Project): Option[StackReplOutput] = {
          StackReplsManager.getProjectRepl(project).flatMap(_.findLocationInfoFor(psiFile, key.startLineNr, key.startColumnNr, key.endLineNr, key.endColumnNr, key.expression))
        }

        private def createDefinitionLocationInfo(output: String): Option[LocationInfo] = {
          output match {
            case LocAtPattern(filePath, startLineNr, startColumnNr, endLineNr, endColumnNr) => Some(DefinitionLocationInfo(filePath.trim, startLineNr.toInt, startColumnNr.toInt, endLineNr.toInt, endColumnNr.toInt))
            case PackageModulePattern(moduleName) => Some(ModuleLocationInfo(moduleName))
            case _ => None
          }
        }
      }
    )

  def findDefinitionLocation(psiElement: PsiElement): Option[LocationInfo] = {
    val qualifiedNameElement = HaskellPsiUtil.findQualifiedNameElement(psiElement)
    for {
      qne <- qualifiedNameElement
      textOffset = qne.getTextOffset
      psiFile <- Option(psiElement.getContainingFile)
      sp <- LineColumnPosition.fromOffset(psiFile, textOffset)
      ep <- LineColumnPosition.fromOffset(psiFile, textOffset + qne.getText.length - 1)
      location <- find(psiFile, sp, ep, qne.getName)
    } yield location
  }

  def invalidate(psiFile: PsiFile): Unit = {
    Cache.asMap().asScala.filter(_._1.psiFile == psiFile).keys.foreach(Cache.invalidate)
  }

  private def find(psiFile: PsiFile, startPosition: LineColumnPosition, endPosition: LineColumnPosition, expression: String): Option[LocationInfo] = {
    val key = Key(startPosition.lineNr, startPosition.columnNr, endPosition.lineNr, endPosition.columnNr, expression, psiFile)
    try {
      Cache.get(key).location match {
        case Right(result) => result
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
}

sealed trait LocationInfo

case class DefinitionLocationInfo(filePath: String, startLineNr: Int, startColumnNr: Int, endLineNr: Int, endColumnNr: Int) extends LocationInfo

case class ModuleLocationInfo(moduleName: String) extends LocationInfo
