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

import java.util.concurrent.{Callable, Executors}

import com.google.common.cache.{CacheBuilder, CacheLoader}
import com.google.common.util.concurrent.{ListenableFuture, ListenableFutureTask, UncheckedExecutionException}
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.psi.{PsiElement, PsiFile}
import intellij.haskell.external.repl.StackReplsManager
import intellij.haskell.psi._
import intellij.haskell.util.LineColumnPosition

import scala.collection.JavaConversions._

private[component] object DefinitionLocationComponent {
  private final val Executor = Executors.newCachedThreadPool()

  private final val LocAtPattern = """(.+)\:\(([\d]+),([\d]+)\)-\(([\d]+),([\d]+)\)""".r

  private case class Key(startLineNr: Int, startColumnNr: Int, endLineNr: Int, endColumnNr: Int, expression: String, psiFile: PsiFile)

  private final val Cache = CacheBuilder.newBuilder()
    .build(
      new CacheLoader[Key, Option[DefinitionLocation]]() {

        override def load(key: Key): Option[DefinitionLocation] = {
          createDefinitionLocation(key)
        }

        override def reload(key: Key, oldInfo: Option[DefinitionLocation]): ListenableFuture[Option[DefinitionLocation]] = {
          val task = ListenableFutureTask.create(new Callable[Option[DefinitionLocation]]() {
            def call() = {
              createDefinitionLocation(key)
            }
          })
          Executor.execute(task)
          task
        }

        private def createDefinitionLocation(key: Key): Option[DefinitionLocation] = {
          val psiFile = key.psiFile
          val project = psiFile.getProject
          val output = StackReplsManager.getProjectRepl(project).findLocationInfoFor(psiFile, key.startLineNr, key.startColumnNr, key.endLineNr, key.endColumnNr + 1, key.expression)
          output.stdOutLines.headOption.flatMap(l => createDefinitionLocationInfo(l))
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
      ep <- LineColumnPosition.fromOffset(psiFile, textOffset + qne.getText.length - 1)
      typeInfo <- find(psiFile, sp, ep, qne.getName)
    } yield typeInfo
  }

  def invalidate(psiFile: PsiFile): Unit = {
    val keys = Cache.asMap().filter(_._1.psiFile == psiFile).keys
    keys.foreach(k => Cache.invalidate(k))
  }

  private def find(psiFile: PsiFile, startPosition: LineColumnPosition, endPosition: LineColumnPosition, expression: String): Option[DefinitionLocation] = {
    val key = Key(startPosition.lineNr, startPosition.columnNr, endPosition.lineNr, endPosition.columnNr, expression, psiFile)
    try {
      Cache.get(key)
    }
    catch {
      case _: UncheckedExecutionException => None
      case _: ProcessCanceledException => None
    }
  }

  private def createDefinitionLocationInfo(output: String): Option[DefinitionLocation] = {
    output match {
      case LocAtPattern(filePath, startLineNr, startColumnNr, endLineNr, endColumnNr) => Some(DefinitionLocation(filePath.trim, startLineNr.toInt, startColumnNr.toInt, endLineNr.toInt, endColumnNr.toInt))
      case _ => None
    }
  }
}

case class DefinitionLocation(filePath: String, startLineNr: Int, startColumnNr: Int, endLineNr: Int, endColumnNr: Int)
