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
import intellij.haskell.util.StringUtil.escapeString
import intellij.haskell.util.{HaskellProjectUtil, StringUtil}

import scala.collection.JavaConverters._

private[component] object NameInfoComponent {

  private final val ProjectInfoPattern = """(.+)-- Defined at (.+):([\d]+):([\d]+)""".r
  private final val LibraryModuleInfoPattern = """(.+)-- Defined in [`‘]([\w\.\-]+):([\w\.\-]+)['’]""".r
  private final val ModuleInfoPattern = """(.+)-- Defined in [`‘]([\w\.\-]+)['’]""".r
  private final val InfixInfoPattern = """(infix.+)""".r

  private final val Executor = Executors.newCachedThreadPool()

  private case class Key(psiFile: PsiFile, name: String)

  private case class ModuleAndNameKey(project: Project, moduleName: String, name: String)

  private case class Result(nameInfos: Option[Iterable[NameInfo]])

  private final val Cache = CacheBuilder.newBuilder()
    .build(
      new CacheLoader[Key, Result] {

        override def load(key: Key): Result = {
          Result(findNameInfos(key))
        }

        override def reload(key: Key, oldResult: Result): ListenableFuture[Result] = {
          val task = ListenableFutureTask.create[Result](() => {
            findNameInfos(key) match {
              case newResult@Some(nis) if nis.nonEmpty => Result(newResult)
              case _ => oldResult
            }
          })
          Executor.execute(task)
          task
        }

        private def findNameInfos(key: Key): Option[Iterable[NameInfo]] = {
          val project = key.psiFile.getProject
          HaskellProjectUtil.isLibraryFile(key.psiFile).flatMap(isLibraryFile => {
            val output = if (isLibraryFile) {
              val moduleName = HaskellPsiUtil.findModuleName(key.psiFile, runInRead = true)
              moduleName.flatMap(mn => StackReplsManager.getGlobalRepl(project).flatMap(_.findInfo(mn, key.name)))
            } else {
              findInfoForProjectIdentifier(key, project)
            }
            createNameInfos(project, output)
          })
        }

        private def findInfoForProjectIdentifier(key: Key, project: Project): Option[StackReplOutput] = {
          if (LoadComponent.isLoaded(key.psiFile, forceLoad = true)) {
            StackReplsManager.getProjectRepl(project).flatMap(_.findInfo(key.psiFile, key.name))
          } else {
            Some(StackReplOutput())
          }
        }
      }
    )

  private final val ModuleAndNameCache = CacheBuilder.newBuilder()
    .build(
      new CacheLoader[ModuleAndNameKey, Result] {

        override def load(key: ModuleAndNameKey): Result = {
          Result(findNameInfos(key))
        }

        override def reload(key: ModuleAndNameKey, oldResult: Result): ListenableFuture[Result] = {
          val task = ListenableFutureTask.create[Result](() => {
            findNameInfos(key) match {
              case newResult@Some(nis) if nis.nonEmpty => Result(newResult)
              case _ => oldResult
            }
          })
          Executor.execute(task)
          task
        }

        private def findNameInfos(key: ModuleAndNameKey): Option[Iterable[NameInfo]] = {
          val output = StackReplsManager.getGlobalRepl(key.project).flatMap(_.findInfo(key.moduleName, key.name))
          createNameInfos(key.project, output)
        }
      })

  def findNameInfo(psiElement: PsiElement): Iterable[NameInfo] = {
    val key = for {
      qne <- HaskellPsiUtil.findQualifiedNameParent(psiElement)
      pf <- Option(qne.getContainingFile).map(_.getOriginalFile)
    } yield Key(pf, qne.getNameWithoutParens.replaceAll("""\s+""", ""))

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

  def findNameInfoByModuleAndName(project: Project, moduleName: String, name: String): Iterable[NameInfo] = {
    try {
      val key = ModuleAndNameKey(project, moduleName, name)
      ModuleAndNameCache.get(key).nameInfos match {
        case Some(nis) => nis
        case _ =>
          Cache.invalidate(key)
          Iterable()
      }
    }
    catch {
      case _: UncheckedExecutionException => None
      case _: ProcessCanceledException => None
    }
  }

  def invalidate(psiFile: PsiFile): Unit = {
    Cache.asMap().asScala.filter(_._1.psiFile == psiFile).keys.foreach(Cache.invalidate)
  }

  def invalidateAll(project: Project): Unit = {
    Cache.asMap().asScala.map(_._1.psiFile).filter(_.getProject == project).foreach(invalidate)
    ModuleAndNameCache.asMap().asScala.filter(_._1.project == project).keys.foreach(ModuleAndNameCache.invalidate)
  }

  private def createNameInfo(outputLine: String, project: Project): Option[NameInfo] = {
    val result = outputLine match {
      case ProjectInfoPattern(declaration, filePath, lineNr, colNr) => Some(ProjectNameInfo(declaration, filePath, lineNr.toInt, colNr.toInt))
      case LibraryModuleInfoPattern(declaration, libraryName, moduleName) =>
        if (libraryName == "ghc-prim" || libraryName == "integer-gmp") {
          Some(BuiltInNameInfo(declaration, libraryName, "GHC.Base"))
        }
        else {
          Some(LibraryNameInfo(declaration, moduleName))
        }
      case ModuleInfoPattern(declaration, moduleName) => Some(LibraryNameInfo(declaration, moduleName))
      case InfixInfoPattern(declaration) => Some(InfixInfo(declaration))
      case _ => None
    }
    result
  }

  private def createNameInfos(project: Project, output: Option[StackReplOutput]): Option[Iterable[NameInfo]] = {
    output.map(_.stdOutLines.flatMap(l => createNameInfo(l, project)))
  }
}

sealed trait NameInfo {

  def declaration: String

  def shortenedDeclaration: String = StringUtil.shortenHaskellDeclaration(declaration)

  def escapedDeclaration: String = escapeString(declaration).replaceAll("""\s+""", " ")
}

case class ProjectNameInfo(declaration: String, filePath: String, lineNr: Int, columnNr: Int) extends NameInfo

case class LibraryNameInfo(declaration: String, moduleName: String) extends NameInfo

case class BuiltInNameInfo(declaration: String, libraryName: String, moduleName: String) extends NameInfo

case class InfixInfo(declaration: String) extends NameInfo