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
import com.intellij.psi.PsiFile
import intellij.haskell.external.repl.{StackReplOutput, StackReplsManager}
import intellij.haskell.psi._
import intellij.haskell.util.LineColumnPosition

import scala.collection.JavaConverters._

private[component] object DefinitionLocationComponent {
  private final val Executor = Executors.newCachedThreadPool()

  private final val LocAtPattern = """(.+)\:\(([\d]+),([\d]+)\)-\(([\d]+),([\d]+)\)""".r
  private final val PackageModulePattern = """.+\:([\w\.\-]+)""".r

  private case class Key(startLineNr: Int, startColumnNr: Int, endLineNr: Int, endColumnNr: Int, expression: String, psiFile: PsiFile)

  type DefinitionLocationResult = Either[NoLocationInfo, LocationInfo]

  private final val Cache = CacheBuilder.newBuilder()
    .build(
      new CacheLoader[Key, DefinitionLocationResult]() {

        override def load(key: Key): DefinitionLocationResult = {
          findDefinitionLocation(key)
        }

        override def reload(key: Key, oldResult: DefinitionLocationResult): ListenableFuture[DefinitionLocationResult] = {
          val task = ListenableFutureTask.create[DefinitionLocationResult](() => {
            val newResult = findDefinitionLocation(key)
            newResult match {
              case Right(_) => newResult
              case _ => oldResult
            }
          })
          Executor.execute(task)
          task
        }

        private def findDefinitionLocation(key: Key): DefinitionLocationResult = {
          if (LoadComponent.isLoaded(key.psiFile)) {
            createDefinitionLocation(key)
          } else {
            Left(CancelledLocationInfo())
          }
        }

        // See https://github.com/commercialhaskell/intero/issues/260
        // and https://github.com/commercialhaskell/intero/issues/182
        private def createDefinitionLocation(key: Key): DefinitionLocationResult = {
          val psiFile = key.psiFile
          val project = psiFile.getProject

          def createLocationInfoWithEndColumnExcluded: DefinitionLocationResult = {
            findLocationInfoFor(key, psiFile, project, endColumnExcluded = true) match {
              case Some(o) => o.stdOutLines.headOption.map(createLocationInfo) match {
                case Some(r) => r
                case None => Left(NoAvailableLocationInfo())
              }
              case None => Left(NoAvailableLocationInfo())
            }
          }

          val location = if (key.expression.trim.length == 1) {
            createLocationInfoWithEndColumnExcluded
          } else {
            findLocationInfoFor(key, psiFile, project, endColumnExcluded = false) match {
              case Some(output) =>
                output.stdOutLines.headOption match {
                  case Some(infoLine) =>
                    createLocationInfo(infoLine) match {
                      case info@Right(locationInfo: DefinitionLocationInfo) =>
                        val locatedNamedElement = LineColumnPosition.getOffset(psiFile, LineColumnPosition(locationInfo.startLineNr, locationInfo.startColumnNr)).flatMap(offset => Option(psiFile.findElementAt(offset)).flatMap(HaskellPsiUtil.findNamedElement))
                        if (locatedNamedElement.exists(ne => ne.getName != key.expression)) {
                          createLocationInfoWithEndColumnExcluded
                        } else {
                          info
                        }
                      case info => info match {
                        case Left(NoAvailableLocationInfo()) => createLocationInfoWithEndColumnExcluded
                        case Left(CancelledLocationInfo()) => info
                        case Right(_) => info
                      }
                    }
                  case None => createLocationInfoWithEndColumnExcluded
                }
              case None => Left(NoAvailableLocationInfo())
            }
          }
          location
        }

        private def findLocationInfoFor(key: Key, psiFile: PsiFile, project: Project, endColumnExcluded: Boolean): Option[StackReplOutput] = {
          val endColumnNr = if (endColumnExcluded) key.endColumnNr else key.endColumnNr - 1
          StackReplsManager.getProjectRepl(project).flatMap(_.findLocationInfoFor(psiFile, key.startLineNr, key.startColumnNr, key.endLineNr, endColumnNr, key.expression))
        }

        private def createLocationInfo(output: String): DefinitionLocationResult = {
          output match {
            case LocAtPattern(filePath, startLineNr, startColumnNr, endLineNr, endColumnNr) => Right(DefinitionLocationInfo(filePath.trim, startLineNr.toInt, startColumnNr.toInt, endLineNr.toInt, endColumnNr.toInt))
            case PackageModulePattern(moduleName) => Right(ModuleLocationInfo(moduleName))
            case _ => Left(NoAvailableLocationInfo())
          }
        }
      }
    )

  def findDefinitionLocation(namedElement: HaskellNamedElement): DefinitionLocationResult = {
    (for {
      qne <- HaskellPsiUtil.findQualifiedNameParent(namedElement)
      textOffset = qne.getTextOffset
      psiFile <- Option(namedElement.getContainingFile)
      sp <- LineColumnPosition.fromOffset(psiFile, textOffset)
      ep <- LineColumnPosition.fromOffset(psiFile, textOffset + qne.getText.length)
    } yield find(psiFile, sp, ep, qne.getNameWithoutParens)) match {
      case Some(r) => r
      case None => Left(NoAvailableLocationInfo())
    }
  }

  def invalidate(psiFile: PsiFile): Unit = {
    Cache.asMap().asScala.filter(_._1.psiFile == psiFile).keys.foreach(Cache.invalidate)
  }

  private def find(psiFile: PsiFile, startPosition: LineColumnPosition, endPosition: LineColumnPosition, expression: String): DefinitionLocationResult = {
    val key = Key(startPosition.lineNr, startPosition.columnNr, endPosition.lineNr, endPosition.columnNr, expression, psiFile)
    try {
      val result = Cache.get(key)
      result match {
        case Right(_) => result
        case _ =>
          Cache.invalidate(key)
          result
      }
    }
    catch {
      case _: UncheckedExecutionException => Left(NoAvailableLocationInfo())
      case _: ProcessCanceledException => Left(NoAvailableLocationInfo())
    }
  }
}

sealed trait NoLocationInfo

case class NoAvailableLocationInfo() extends NoLocationInfo

case class CancelledLocationInfo() extends NoLocationInfo

sealed trait LocationInfo

case class DefinitionLocationInfo(filePath: String, startLineNr: Int, startColumnNr: Int, endLineNr: Int, endColumnNr: Int) extends LocationInfo

case class ModuleLocationInfo(moduleName: String) extends LocationInfo
