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

package intellij.haskell.external

import java.util.concurrent.{Callable, Executors, TimeUnit}

import com.google.common.cache.{CacheBuilder, CacheLoader}
import com.google.common.util.concurrent.{ListenableFuture, ListenableFutureTask, UncheckedExecutionException}
import com.intellij.openapi.editor.SelectionModel
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.project.Project
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.{PsiElement, PsiFile}
import intellij.haskell.HaskellNotificationGroup
import intellij.haskell.psi._
import intellij.haskell.util.{FileUtil, HaskellElementCondition, LineColumnPosition}
import intellij.haskell.util.HaskellEditorUtil.escapeString

import scala.util.{Failure, Success, Try}

object GhcModTypeInfo {
  private final val GhcModTypeInfoPattern = """([\d]+) ([\d]+) ([\d]+) ([\d]+) "(.+)"""".r

  private final val Executor = Executors.newCachedThreadPool()

  private case class ElementTypeInfo(filePath: String, lineNr: Int, columnNr: Int, project: Project)

  private final val TypeInfoCache = CacheBuilder.newBuilder()
      .refreshAfterWrite(1, TimeUnit.SECONDS)
      .build(
        new CacheLoader[ElementTypeInfo, GhcModOutput]() {
          private def findTypeInfoFor(elementTypeInfo: ElementTypeInfo): GhcModOutput = {
            val cmd = s"type ${elementTypeInfo.filePath} ${elementTypeInfo.lineNr} ${elementTypeInfo.columnNr}"
            GhcModProcessManager.getGhcModProcess(elementTypeInfo.project).execute(cmd)
          }

          override def load(elementTypeInfo: ElementTypeInfo): GhcModOutput = {
            findTypeInfoFor(elementTypeInfo)
          }

          override def reload(elementTypeInfo: ElementTypeInfo, oldInfo: GhcModOutput): ListenableFuture[GhcModOutput] = {
            val task = ListenableFutureTask.create(new Callable[GhcModOutput]() {
              def call() = {
                val newInfo = findTypeInfoFor(elementTypeInfo)
                if (newInfo.outputLines.isEmpty) {
                  oldInfo
                } else {
                  newInfo
                }
              }
            })
            Executor.execute(task)
            task
          }
        }
      )

  def findTypeInfoFor(psiFile: PsiFile, psiElement: PsiElement): Option[TypeInfo] = {
    val textOffset = psiElement match {
      case e: HaskellQVarConOpElement => e.getTextOffset
      case e => Option(PsiTreeUtil.findFirstParent(e, HaskellElementCondition.QVarConOpElementCondition)).map(_.getTextOffset).getOrElse(e.getTextOffset)
    }

    for {
      spe <- LineColumnPosition.fromOffset(psiFile, textOffset)
      typeInfos <- findGhcModiTypeInfos(psiFile, spe)
      typeInfo <- typeInfos.find(ty => ty.startLine == spe.lineNr && ty.startColumn == spe.columnNr)
    } yield typeInfo
  }

  def findTypeInfoForSelection(psiFile: PsiFile, selectionModel: SelectionModel): Option[TypeInfo] = {
    for {
      ss <- LineColumnPosition.fromOffset(psiFile, selectionModel.getSelectionStart)
      se <- LineColumnPosition.fromOffset(psiFile, selectionModel.getSelectionEnd)
      typeInfos <- findGhcModiTypeInfos(psiFile, ss)
      typeInfo <- typeInfos.find(ty => ty.startLine == ss.lineNr && ty.startColumn == ss.columnNr && ty.endLine == se.lineNr && ty.endColumn == se.columnNr)
    } yield typeInfo
  }

  private def findGhcModiTypeInfos(psiFile: PsiFile, startPositionExpression: LineColumnPosition): Option[Iterable[TypeInfo]] = {
    val filePath = FileUtil.getFilePath(psiFile)
    val ghcModiOutput = try {
      val key = ElementTypeInfo(filePath, startPositionExpression.lineNr, startPositionExpression.columnNr, psiFile.getProject)
      val output = TypeInfoCache.get(key)
      if (output.outputLines.isEmpty) {
        TypeInfoCache.refresh(key)
        TypeInfoCache.get(key)
      } else {
        output
      }
    }
    catch {
      case _: UncheckedExecutionException => GhcModOutput()
      case _: ProcessCanceledException => GhcModOutput()
    }
    ghcModiOutputToTypeInfo(ghcModiOutput.outputLines) match {
      case Success(typeInfos) => Some(typeInfos)
      case Failure(error) => HaskellNotificationGroup.notifyError(s"Could not determine type. Error: $error.getMessage"); None
    }
  }

  private[external] def ghcModiOutputToTypeInfo(ghcModiOutput: Iterable[String]): Try[Iterable[TypeInfo]] = Try {
    for (outputLine <- ghcModiOutput) yield {
      outputLine match {
        case GhcModTypeInfoPattern(startLn, startCol, endLine, endCol, typeSignature) =>
          TypeInfo(startLn.toInt, startCol.toInt, endLine.toInt, endCol.toInt, shortenTypeSignature(typeSignature))
      }
    }
  }

  private def shortenTypeSignature(typeSignature: String) = {
    if (typeSignature.length > 80) {
      escapeString(typeSignature).split("->").mkString("->\n")
    } else {
      escapeString(typeSignature)
    }
  }
}

case class TypeInfo(startLine: Int, startColumn: Int, endLine: Int, endColumn: Int, typeSignature: String)
