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
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Computable
import com.intellij.psi.PsiFile
import com.intellij.psi.search.GlobalSearchScopesCore
import intellij.haskell.HaskellFile
import intellij.haskell.external.repl._
import intellij.haskell.util.StringUtil
import intellij.haskell.util.index.HaskellModuleNameIndex

import scala.collection.JavaConverters._

private[component] object BrowseModuleComponent {

  private case class Key(project: Project, moduleName: String, psiFile: Option[PsiFile])

  private val executor = Executors.newCachedThreadPool()

  private final val Cache = CacheBuilder.newBuilder()
    .build(
      new CacheLoader[Key, Either[NoBrowseInfo, Iterable[ModuleIdentifier]]]() {

        override def load(key: Key): Either[NoBrowseInfo, Iterable[ModuleIdentifier]] = {
          findModuleIdentifiers(key, reload = false)
        }

        override def reload(key: Key, oldValue: Either[NoBrowseInfo, Iterable[ModuleIdentifier]]): ListenableFuture[Either[NoBrowseInfo, Iterable[ModuleIdentifier]]] = {
          val task = ListenableFutureTask.create[Either[NoBrowseInfo, Iterable[ModuleIdentifier]]](() => {
            findModuleIdentifiers(key, reload = true)
          })
          executor.execute(task)
          task
        }

        private def findModuleIdentifiers(key: Key, reload: Boolean): Either[NoBrowseInfo, Iterable[ModuleIdentifier]] = {
          val project = key.project
          val moduleName = key.moduleName

          key.psiFile match {
            case Some(psiFile) =>
              if (!reload && LoadComponent.isLoading(psiFile)) {
                Left(ReplIsLoading)
              } else if (LoadComponent.isLoaded(psiFile).exists(_ != Failed)) {
                StackReplsManager.getProjectRepl(psiFile).flatMap(_.getModuleIdentifiersWithLoad(moduleName, psiFile)).map { output =>
                  Right(output.stdOutLines.flatMap(l => findModuleIdentifiers(project, l, moduleName)))
                }.getOrElse(Left(NoBrowseInfoAvailable))
              } else {
                Left(NoBrowseInfoAvailable)
              }
            case None =>
              val projectHaskellFiles =
                ApplicationManager.getApplication.runReadAction(new Computable[Iterable[HaskellFile]] {
                  override def compute(): Iterable[HaskellFile] = {
                    val productionFile = HaskellModuleNameIndex.findHaskellFileByModuleName(project, moduleName, GlobalSearchScopesCore.projectProductionScope(project))
                    if (productionFile.isEmpty) {
                      HaskellModuleNameIndex.findHaskellFileByModuleName(project, moduleName, GlobalSearchScopesCore.projectTestScope(project))
                    } else {
                      productionFile
                    }
                  }
                })

              if (projectHaskellFiles.nonEmpty) {
                val output = projectHaskellFiles.headOption.flatMap(f => StackReplsManager.getProjectRepl(f).flatMap(_.getModuleIdentifiers(moduleName, f)))
                output match {
                  case Some(o) if o.stdErrLines.isEmpty => output.map(_.stdOutLines.flatMap(l => findModuleIdentifiers(project, l, moduleName))) match {
                    case Some(ids) => Right(ids)
                    case None => Left(NoBrowseInfoAvailable)
                  }
                  case _ => Left(NoBrowseInfoAvailable)
                }
              } else {
                val moduleIdentifiers = StackReplsManager.getGlobalRepl(project).flatMap(_.getModuleIdentifiers(moduleName).filter(_.stdOutLines.nonEmpty).
                  map(_.stdOutLines.flatMap(l => findModuleIdentifiers(project, l, moduleName))))
                moduleIdentifiers match {
                  case Some(ids) => Right(ids)
                  case None => Left(NoBrowseInfoAvailable)
                }
              }
          }
        }

        private def findModuleIdentifiers(project: Project, declarationLine: String, moduleName: String): Option[ModuleIdentifier] = {
          DeclarationLineUtil.findName(declarationLine).map(nd => createModuleIdentifier(nd.name, moduleName, nd.declaration))
        }

        private def createModuleIdentifier(name: String, moduleName: String, declaration: String) = {
          ModuleIdentifier(StringUtil.removeOuterParens(name), moduleName, declaration, isOperator = DeclarationLineUtil.isOperator(name))
        }
      }
    )

  def findExportedModuleIdentifiers(project: Project, moduleName: String, psiFile: Option[PsiFile]): Iterable[ModuleIdentifier] = {
    try {
      val key = Key(project, moduleName, psiFile)
      Cache.get(key) match {
        case Right(ids) => ids
        case Left(NoBrowseInfoAvailable) =>
          Cache.invalidate(key)
          Iterable()
        case Left(ReplIsLoading) =>
          Cache.refresh(key)
          Iterable()
      }
    }
    catch {
      case _: UncheckedExecutionException => Iterable()
      case _: ProcessCanceledException => Iterable()
    }
  }

  def findModuleNamesInCache(project: Project): Iterable[String] = {
    Cache.asMap().asScala.filter(_._1.project == project).map(_._1.moduleName)
  }

  def invalidateTopLevel(project: Project, psiFile: PsiFile): Unit = {
    val keys = Cache.asMap().keySet().asScala.filter(k => k.project == project && k.psiFile.contains(psiFile))
    keys.foreach(k => Cache.invalidate(k))
  }

  def refreshTopLevel(project: Project, psiFile: PsiFile): Unit = {
    val keys = Cache.asMap().keySet().asScala.filter(k => k.project == project && k.psiFile.contains(psiFile))
    keys.foreach(k => Cache.refresh(k))
  }

  def invalidateForModuleName(project: Project, moduleName: String): Unit = {
    val keys = Cache.asMap().keySet().asScala.filter(k => k.project == project && k.moduleName == moduleName && k.psiFile.isEmpty)
    keys.foreach(k => Cache.invalidate(k))
  }

  def invalidate(project: Project): Unit = {
    val keys = Cache.asMap().asScala.keys.filter(k => k.project == project)
    keys.foreach(k => Cache.invalidate(k))
  }

  private sealed trait NoBrowseInfo

  private object ReplIsLoading extends NoBrowseInfo

  private object NoBrowseInfoAvailable extends NoBrowseInfo

}

case class ModuleIdentifier(name: String, moduleName: String, declaration: String, isOperator: Boolean)
