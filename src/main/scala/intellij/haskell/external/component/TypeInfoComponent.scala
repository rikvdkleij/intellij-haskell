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
import com.intellij.openapi.editor.SelectionModel
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.psi.{PsiElement, PsiFile}
import intellij.haskell.external.repl.StackReplsManager
import intellij.haskell.psi._
import intellij.haskell.util.LineColumnPosition

import scala.collection.JavaConversions._

private[component] object TypeInfoComponent {

  private final val Executor = Executors.newCachedThreadPool()

  private case class Key(psiFile: PsiFile, startLineNr: Int, startColumnNr: Int, endLineNr: Int, endColumnNr: Int, expression: String)

  private case class Result(typeInfo: Either[String, Option[TypeInfo]], var toRefresh: Boolean = false)

  private final val Cache = CacheBuilder.newBuilder()
    .refreshAfterWrite(2, TimeUnit.SECONDS)
    .expireAfterWrite(10, TimeUnit.MINUTES)
    .build(
      new CacheLoader[Key, Result]() {

        override def load(key: Key): Result = {
          createTypeInfo(key)
        }

        override def reload(key: Key, oldResult: Result): ListenableFuture[Result] = {
          val task = ListenableFutureTask.create(new Callable[Result]() {
            def call() = {
              if (oldResult.toRefresh && oldResult.typeInfo.isRight) {
                val newResult = createTypeInfo(key)
                newResult.typeInfo match {
                  case Right(ti) if ti.isDefined => newResult
                  case _ => oldResult
                }
              } else {
                oldResult
              }
            }
          })
          Executor.execute(task)
          task
        }

        private def createTypeInfo(key: Key): Result = {
          val project = key.psiFile.getProject
          val typeInfo = StackReplsManager.getProjectRepl(project).findTypeInfoFor(key.psiFile, key.startLineNr, key.startColumnNr, key.endLineNr, key.endColumnNr, key.expression) match {
            case Some(output) => Right(output.stdOutLines.headOption.filterNot(_.trim.isEmpty).map(ti => TypeInfo(ti)))
            case _ => Left("No type info available")
          }
          Result(typeInfo)
        }
      }
    )

  def findTypeInfoForElement(psiElement: PsiElement): Option[TypeInfo] = {
    for {
      qne <- HaskellPsiUtil.findQualifiedNameElement(psiElement)
      to = qne.getTextOffset
      f <- Option(psiElement.getContainingFile)
      sp <- LineColumnPosition.fromOffset(f, to)
      ep <- LineColumnPosition.fromOffset(f, to + qne.getText.length)
      ti <- findTypeInfo(f, sp, ep, qne.getText)
    } yield ti
  }

  def findTypeInfoForSelection(psiFile: PsiFile, selectionModel: SelectionModel): Option[TypeInfo] = {
    for {
      sp <- LineColumnPosition.fromOffset(psiFile, selectionModel.getSelectionStart)
      ep <- LineColumnPosition.fromOffset(psiFile, selectionModel.getSelectionEnd)
      typeInfo <- findTypeInfo(psiFile, sp, ep, selectionModel.getSelectedText)
    } yield typeInfo
  }

  def markAllToRefresh(psiFile: PsiFile): Unit = {
    Cache.asMap().filter(_._1.psiFile == psiFile).values.foreach(_.toRefresh = true)
  }

  private def findTypeInfo(psiFile: PsiFile, startPosition: LineColumnPosition, endPosition: LineColumnPosition, expression: String): Option[TypeInfo] = {
    val key = Key(psiFile, startPosition.lineNr, startPosition.columnNr, endPosition.lineNr, endPosition.columnNr, expression)
    try {
      Cache.get(key).typeInfo match {
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

case class TypeInfo(typeSignature: String)
