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
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.SelectionModel
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.util.Computable
import com.intellij.psi.{PsiElement, PsiFile}
import intellij.haskell.external.repl.StackReplsManager
import intellij.haskell.psi._
import intellij.haskell.util.LineColumnPosition

import scala.collection.JavaConverters._

private[component] object TypeInfoComponent {

  private final val Executor = Executors.newCachedThreadPool()

  private case class Key(psiFile: PsiFile, startLineNr: Int, startColumnNr: Int, endLineNr: Int, endColumnNr: Int, expression: String, forceGetInfo: Boolean)

  private case class Result(typeInfo: Either[NoTypeInfo, Option[TypeInfo]])

  private final val Cache = CacheBuilder.newBuilder()
    .build(
      new CacheLoader[Key, Result]() {

        override def load(key: Key): Result = {
          if (!key.forceGetInfo && LoadComponent.isBusy(key.psiFile)) {
            Result(Left(ReplIsLoading))
          } else {
            findTypeInfo(key)
          }
        }

        override def reload(key: Key, oldResult: Result): ListenableFuture[Result] = {
          val task = ListenableFutureTask.create[Result](() => {
            findTypeInfo(key)
          })
          Executor.execute(task)
          task
        }

        private def findTypeInfo(key: Key) = {
          val typeInfo = StackReplsManager.getProjectRepl(key.psiFile).flatMap(_.findTypeInfoFor(key.psiFile, key.startLineNr, key.startColumnNr, key.endLineNr, key.endColumnNr, key.expression)) match {
            case Some(output) => Right(output.stdoutLines.headOption.filterNot(_.trim.isEmpty).map(ti => TypeInfo(ti)))
            case _ => Left(ReplNotAvailable)
          }
          Result(typeInfo)
        }
      }
    )

  def findTypeInfoForElement(psiElement: PsiElement, forceGetInfo: Boolean): Option[TypeInfo] = {
    if (psiElement.isValid) {
      ApplicationManager.getApplication.runReadAction(new Computable[Option[Key]] {
        override def compute(): Option[Key] = {
          for {
            qne <- HaskellPsiUtil.findQualifiedNameParent(psiElement)
            to = qne.getTextOffset
            pf <- Option(psiElement.getContainingFile)
            sp <- LineColumnPosition.fromOffset(pf, to)
            ep <- LineColumnPosition.fromOffset(pf, to + qne.getText.length)
          } yield Key(pf, sp.lineNr, sp.columnNr, ep.lineNr, ep.columnNr, qne.getName, forceGetInfo)
        }
      }).flatMap(key => {
        val otherKey = key.copy(forceGetInfo = !forceGetInfo)
        Option(Cache.getIfPresent(otherKey)).flatMap(_.typeInfo.toOption) match {
          case Some(r) => r
          case None => findTypeInfo(key)
        }
      })
    } else {
      None
    }
  }

  def findTypeInfoForSelection(psiFile: PsiFile, selectionModel: SelectionModel): Option[TypeInfo] = {
    for {
      sp <- LineColumnPosition.fromOffset(psiFile, selectionModel.getSelectionStart)
      ep <- LineColumnPosition.fromOffset(psiFile, selectionModel.getSelectionEnd)
      typeInfo <- findTypeInfo(Key(psiFile, sp.lineNr, sp.columnNr, ep.lineNr, ep.columnNr, selectionModel.getSelectedText, forceGetInfo = true))
    } yield typeInfo
  }

  def invalidate(psiFile: PsiFile): Unit = {
    Cache.asMap().asScala.filter(_._1.psiFile == psiFile).keys.foreach(Cache.invalidate)
  }

  private def findTypeInfo(key: Key): Option[TypeInfo] = {
    try {
      Cache.get(key).typeInfo match {
        case Right(result) => result
        case Left(ReplNotAvailable) =>
          Cache.invalidate(key)
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


  private sealed trait NoTypeInfo

  private case object ReplNotAvailable extends NoTypeInfo

  private case object ReplIsLoading extends NoTypeInfo

}

case class TypeInfo(typeSignature: String)
