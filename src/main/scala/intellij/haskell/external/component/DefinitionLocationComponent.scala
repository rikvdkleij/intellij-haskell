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

import java.util.concurrent.{Callable, Executors, TimeUnit}

import com.google.common.cache.{CacheBuilder, CacheLoader}
import com.google.common.util.concurrent.{ListenableFuture, ListenableFutureTask, UncheckedExecutionException}
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.project.Project
import com.intellij.psi.{PsiElement, PsiFile}
import intellij.haskell.external.repl.{StackReplOutput, StackReplsManager}
import intellij.haskell.psi._
import intellij.haskell.util.LineColumnPosition

private[component] object DefinitionLocationComponent {
  private final val Executor = Executors.newCachedThreadPool()

  private final val LocAtPattern = """(.+)\:\(([\d]+),([\d]+)\)-\(([\d]+),([\d]+)\)""".r

  private case class Key(startLineNr: Int, startColumnNr: Int, endLineNr: Int, endColumnNr: Int, expression: String, psiFile: PsiFile)

  private type Result = Either[String, Option[DefinitionLocation]]

  private final val Cache = CacheBuilder.newBuilder()
    .refreshAfterWrite(5, TimeUnit.SECONDS)
    .expireAfterWrite(10, TimeUnit.MINUTES)
    .build(
      new CacheLoader[Key, Result]() {

        override def load(key: Key): Result = {
          createDefinitionLocation(key)
        }

        override def reload(key: Key, oldInfo: Result): ListenableFuture[Result] = {
          val task = ListenableFutureTask.create(new Callable[Result]() {
            def call() = {
              val newInfo = createDefinitionLocation(key)
              newInfo match {
                case Right(o) if o.isDefined => newInfo
                case _ => oldInfo
              }
            }
          })
          Executor.execute(task)
          task
        }

        // See https://github.com/commercialhaskell/intero/issues/260
        // and https://github.com/commercialhaskell/intero/issues/182
        private def createDefinitionLocation(key: Key): Result = {
          val psiFile = key.psiFile
          val project = psiFile.getProject
          (findLocationInfoFor(key, psiFile, project, endColumnExcluded = false) match {
            case Some(output1) =>
              val headOption = output1.stdOutLines.headOption
              if (headOption.contains("Couldn't resolve to any modules.") || headOption.contains("No matching export in any local modules.")) {
                findLocationInfoFor(key, psiFile, project, endColumnExcluded = true) match {
                  case Some(output2) => Right(output2)
                  case _ => Left("No info available")
                }
              } else {
                Right(output1)
              }
            case _ => Left("No info available")
          }).right.map(_.stdOutLines.headOption.flatMap(l => createDefinitionLocationInfo(l)))
        }

        private def findLocationInfoFor(key: Key, psiFile: PsiFile, project: Project, endColumnExcluded: Boolean): Option[StackReplOutput] = {
          val endColumnNr = if (endColumnExcluded) key.endColumnNr else key.endColumnNr - 1
          StackReplsManager.getProjectRepl(project).findLocationInfoFor(psiFile, key.startLineNr, key.startColumnNr, key.endLineNr, endColumnNr, key.expression)
        }

        // TODO: Extend with `package:module name` pattern
        private def createDefinitionLocationInfo(output: String): Option[DefinitionLocation] = {
          output match {
            case LocAtPattern(filePath, startLineNr, startColumnNr, endLineNr, endColumnNr) => Some(DefinitionLocation(filePath.trim, startLineNr.toInt, startColumnNr.toInt, endLineNr.toInt, endColumnNr.toInt))
            case _ => None
          }
        }
      }
    )

  def findDefinitionLocation(psiElement: PsiElement): Option[DefinitionLocation] = {
    val qualifiedNameElement = HaskellPsiUtil.findQualifiedNameElement(psiElement)
    for {
      qne <- qualifiedNameElement
      textOffset = qne.getTextOffset
      psiFile <- Option(psiElement.getContainingFile)
      sp <- LineColumnPosition.fromOffset(psiFile, textOffset)
      ep <- LineColumnPosition.fromOffset(psiFile, textOffset + qne.getText.length)
      location <- find(psiFile, sp, ep, qne.getName)
    } yield location
  }

  private def find(psiFile: PsiFile, startPosition: LineColumnPosition, endPosition: LineColumnPosition, expression: String): Option[DefinitionLocation] = {
    val key = Key(startPosition.lineNr, startPosition.columnNr, endPosition.lineNr, endPosition.columnNr, expression, psiFile)
    try {
      Cache.get(key) match {
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

case class DefinitionLocation(filePath: String, startLineNr: Int, startColumnNr: Int, endLineNr: Int, endColumnNr: Int)
