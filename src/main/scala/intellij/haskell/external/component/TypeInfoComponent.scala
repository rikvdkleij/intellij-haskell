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
import com.intellij.openapi.editor.SelectionModel
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.psi.{PsiElement, PsiFile}
import intellij.haskell.external.repl.StackReplsManager
import intellij.haskell.psi._
import intellij.haskell.util.LineColumnPosition

import scala.collection.JavaConverters._

private[component] object TypeInfoComponent {

  private final val Executor = Executors.newCachedThreadPool()

  private case class Key(psiFile: PsiFile, startLineNr: Int, startColumnNr: Int, endLineNr: Int, endColumnNr: Int, expression: String)

  private case class Result(typeInfo: Either[String, Option[TypeInfo]])

  private final val Cache = CacheBuilder.newBuilder()
    .build(
      new CacheLoader[Key, Result]() {

        override def load(key: Key): Result = {
          findTypeInfo(key)
        }

        override def reload(key: Key, oldResult: Result): ListenableFuture[Result] = {
          val task = ListenableFutureTask.create[Result](() => {
            val newResult = findTypeInfo(key)
            newResult.typeInfo match {
              case Right(ti) if ti.isDefined => newResult
              case _ => oldResult
            }
          })
          Executor.execute(task)
          task
        }

        private def findTypeInfo(key: Key) = {
          if (LoadComponent.isLoaded(key.psiFile)) {
            createTypeInfo(key)
          } else {
            Result(Left("No info available at this moment"))
          }
        }

        private def createTypeInfo(key: Key): Result = {
          val project = key.psiFile.getProject
          val typeInfo = StackReplsManager.getProjectRepl(project).flatMap(_.findTypeInfoFor(key.psiFile, key.startLineNr, key.startColumnNr, key.endLineNr, key.endColumnNr, key.expression)) match {
            case Some(output) => Right(output.stdOutLines.headOption.filterNot(_.trim.isEmpty).map(ti => TypeInfo(ti)))
            case _ => Left("No type info available")
          }
          Result(typeInfo)
        }
      }
    )

  def findTypeInfoForElement(psiElement: PsiElement): Option[TypeInfo] = {
    for {
      qne <- HaskellPsiUtil.findQualifiedNameParent(psiElement)
      to = qne.getTextOffset
      f <- Option(psiElement.getContainingFile)
      sp <- LineColumnPosition.fromOffset(f, to)
      ep <- LineColumnPosition.fromOffset(f, to + qne.getText.length)
      ti <- findTypeInfo(f, sp, ep, qne.getNameWithoutParens)
    } yield ti
  }

  def findTypeInfoForSelection(psiFile: PsiFile, selectionModel: SelectionModel): Option[TypeInfo] = {
    for {
      sp <- LineColumnPosition.fromOffset(psiFile, selectionModel.getSelectionStart)
      ep <- LineColumnPosition.fromOffset(psiFile, selectionModel.getSelectionEnd)
      typeInfo <- findTypeInfo(psiFile, sp, ep, selectionModel.getSelectedText)
    } yield typeInfo
  }

  def invalidate(psiFile: PsiFile): Unit = {
    Cache.asMap().asScala.filter(_._1.psiFile == psiFile).keys.foreach(Cache.invalidate)
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
