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
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Computable
import com.intellij.psi.{PsiElement, PsiFile}
import intellij.haskell.external.repl.{StackReplOutput, StackReplsManager}
import intellij.haskell.psi._
import intellij.haskell.util.StringUtil.escapeString
import intellij.haskell.util.{HaskellProjectUtil, StringUtil}

import scala.collection.JavaConversions._

private[component] object NameInfoComponent {

  private final val ProjectInfoPattern = """(.+)-- Defined at (.+):([\d]+):([\d]+)""".r
  private final val LibraryModuleInfoPattern = """(.+)-- Defined in [`‘]([\w\.\-]+):([\w\.\-]+)['’]""".r
  private final val ModuleInfoPattern = """(.+)-- Defined in [`‘]([\w\.\-]+)['’]""".r

  private final val Executor = Executors.newCachedThreadPool()

  private case class Key(psiFile: PsiFile, name: String)

  private case class Result(nameInfos: Option[Iterable[NameInfo]], var toRefresh: Boolean = false)

  private final val Cache = CacheBuilder.newBuilder()
    .refreshAfterWrite(2, TimeUnit.SECONDS)
    .expireAfterWrite(30, TimeUnit.MINUTES)
    .build(
      new CacheLoader[Key, Result] {

        override def load(key: Key): Result = {
          Result(findNameInfos(key, key.psiFile.getProject))
        }

        override def reload(key: Key, oldResult: Result): ListenableFuture[Result] = {
          val task = ListenableFutureTask.create(new Callable[Result]() {
            def call() = {
              if (oldResult.toRefresh && oldResult.nameInfos.isDefined) {
                // Only elements of project file can be refreshed
                findNameInfosInProjectFile(key, key.psiFile.getProject) match {
                  case newResult@Some(nis) if nis.nonEmpty => Result(newResult)
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

        private def findNameInfosInProjectFile(key: Key, project: Project): Option[Iterable[NameInfo]] = {
          val output = callProjectReplForInfo(key, project)
          createNameInfos(project, output)
        }

        private def findNameInfos(key: Key, project: Project): Option[Iterable[NameInfo]] = {
          val output = if (HaskellProjectUtil.isLibraryFile(key.psiFile)) {
            val moduleName = findModuleName(key.psiFile)
            moduleName.flatMap(mn => StackReplsManager.getGlobalRepl(project).findInfo(mn, key.name))
          } else {
            callProjectReplForInfo(key, project)
          }
          createNameInfos(project, output)
        }

        private def callProjectReplForInfo(key: Key, project: Project): Option[StackReplOutput] = {
          StackReplsManager.getProjectRepl(project).findInfo(key.psiFile, key.name)
        }

        private def createNameInfos(project: Project, output: Option[StackReplOutput]): Option[Iterable[NameInfo]] = {
          output.map(_.stdOutLines.flatMap(l => createNameInfo(l, project)))
        }

        private def findModuleName(psiFile: PsiFile) = {
          ApplicationManager.getApplication.runReadAction {
            new Computable[Option[String]] {
              override def compute(): Option[String] = {
                HaskellPsiUtil.findModuleName(psiFile)
              }
            }
          }
        }

        private def createNameInfo(outputLine: String, project: Project): Option[NameInfo] = {
          outputLine match {
            case ProjectInfoPattern(declaration, filePath, lineNr, colNr) => Some(ProjectNameInfo(declaration, filePath, lineNr.toInt, colNr.toInt))
            case LibraryModuleInfoPattern(declaration, libraryName, moduleName) =>
              if (libraryName == "ghc-prim" || libraryName == "integer-gmp") {
                Some(BuiltInNameInfo(declaration, libraryName, "GHC.Base"))
              }
              else {
                Some(LibraryNameInfo(declaration, moduleName))
              }
            case ModuleInfoPattern(declaration, moduleName) => Some(LibraryNameInfo(declaration, moduleName))
            case _ => None
          }
        }
      }
    )

  def findNameInfo(psiElement: PsiElement): Iterable[NameInfo] = {
    val key = for {
      qne <- HaskellPsiUtil.findQualifiedNameElement(psiElement)
      pf <- Option(qne.getContainingFile).map(_.getOriginalFile)
    } yield Key(pf, qne.getName.replaceAll("""\s+""", ""))

    (try {
      key.map(k =>
        Cache.get(k).nameInfos match {
          case Some(nis) => nis
          case _ =>
            Cache.invalidate(k)
            Iterable()
        })
    }
    catch {
      case _: UncheckedExecutionException => None
      case _: ProcessCanceledException => None
    }).getOrElse(Iterable())
  }

  // TODO: Make distinction in type between project file and library file
  def markAllToRefresh(psiFile: PsiFile): Unit = {
    Cache.asMap().filter(_._1.psiFile == psiFile).values.foreach(_.toRefresh = true)
  }
}

sealed trait NameInfo {

  def declaration: String

  def shortenedDeclaration = StringUtil.shortenHaskellDeclaration(declaration)

  def escapedDeclaration = escapeString(declaration).replaceAll("""\s+""", " ")
}

case class ProjectNameInfo(declaration: String, filePath: String, lineNr: Int, columnNr: Int) extends NameInfo

case class LibraryNameInfo(declaration: String, moduleName: String) extends NameInfo

case class BuiltInNameInfo(declaration: String, libraryName: String, moduleName: String) extends NameInfo
