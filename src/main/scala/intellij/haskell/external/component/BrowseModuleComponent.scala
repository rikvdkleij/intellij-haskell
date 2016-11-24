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
import intellij.haskell.external.repl.StackReplsManager
import intellij.haskell.util.StringUtil

import scala.collection.JavaConverters._

private[component] object BrowseModuleComponent {

  private case class Key(project: Project, moduleName: String, psiFile: Option[PsiFile])

  private val executor = Executors.newCachedThreadPool()

  private final val Cache = CacheBuilder.newBuilder()
    .build(
      new CacheLoader[Key, Option[Iterable[ModuleIdentifier]]]() {

        override def load(key: Key): Option[Iterable[ModuleIdentifier]] = {
          findModuleIdentifiers(key)
        }

        override def reload(key: Key, oldValue: Option[Iterable[ModuleIdentifier]]): ListenableFuture[Option[Iterable[ModuleIdentifier]]] = {
          val task = ListenableFutureTask.create[Option[Iterable[ModuleIdentifier]]](() => {
            findModuleIdentifiers(key)
          })
          executor.execute(task)
          task
        }

        private def findModuleIdentifiers(key: Key): Option[Iterable[ModuleIdentifier]] = {
          val project = key.project
          val moduleName = key.moduleName
          GlobalProjectInfoComponent.findGlobalProjectInfo(project).flatMap(gpi => {
            if (gpi.allAvailableLibraryModuleNames.exists(_ == moduleName)) {
              StackReplsManager.getGlobalRepl(project).getModuleIdentifiers(moduleName).filter(_.stdOutLines.nonEmpty) map (_.stdOutLines.flatMap(findModuleIdentifier(_, moduleName, project)))
            } else {
              key.psiFile match {
                case Some(f) =>
                  StackReplsManager.getProjectRepl(project).getAllTopLevelModuleIdentifiers(moduleName, f) map { output =>
                    val definedLocallyLines = output.stdOutLines.takeWhile(l => !l.startsWith("-- imported via"))
                    definedLocallyLines.flatMap(findModuleIdentifier(_, moduleName, project))
                  }
                case _ => StackReplsManager.getProjectRepl(project).getModuleIdentifiers(moduleName).map(_.stdOutLines.flatMap(findModuleIdentifier(_, moduleName, project)))
              }
            }
          })
        }

        private def findModuleIdentifier(outputLine: String, moduleName: String, project: Project): Option[ModuleIdentifier] = {
          val declaration = StringUtil.shortenHaskellDeclaration(outputLine)
          val allTokens = declaration.split("""\s+""")
          if (allTokens.isEmpty || allTokens(0) == "--") {
            None
          } else if (Seq("class", "instance").contains(allTokens(0))) {
            declaration.split("""where|=\s""").headOption.flatMap { d =>
              val tokens = d.trim.split("""=>""")
              if (tokens.size == 1) {
                createModuleIdentifier(allTokens(1), moduleName, declaration)
              } else {
                createModuleIdentifier(tokens.last.trim.split("""\s+""")(0), moduleName, declaration)
              }
            }
          } else if (allTokens(0) == "type" && allTokens(1) == "role") {
            createModuleIdentifier(allTokens(2), moduleName, declaration)
          } else if (Seq("data", "type", "newtype").contains(allTokens(0).trim)) {
            createModuleIdentifier(allTokens(1), moduleName, declaration)
          } else {
            val tokens = declaration.split("""::""")
            if (tokens.size > 1) {
              val name = tokens(0).trim
              createModuleIdentifier(name, moduleName, declaration)
            } else {
              None
            }
          }
        }

        private def createModuleIdentifier(name: String, moduleName: String, declaration: String) = {
          if (name.startsWith("(")) {
            Some(ModuleIdentifier(name.substring(1, name.length - 1), moduleName, declaration, isOperator = true))
          } else {
            Some(ModuleIdentifier(name, moduleName, declaration, isOperator = false))
          }
        }
      }
    )

  def findImportedModuleIdentifiers(project: Project, moduleName: String): Iterable[ModuleIdentifier] = {
    try {
      val key = Key(project, moduleName, None)
      Cache.get(key) match {
        case Some(result) => result
        case _ =>
          Cache.invalidate(key)
          Iterable()
      }
    }
    catch {
      case _: UncheckedExecutionException => Iterable()
      case _: ProcessCanceledException => Iterable()
    }
  }

  def findModuleNamesInCache(project: Project): Iterable[String] = {
    Cache.asMap().asScala.filter(e => e._1.project == project).map(_._1.moduleName)
  }

  def findAllTopLevelModuleIdentifiers(project: Project, moduleName: String, psiFile: PsiFile): Iterable[ModuleIdentifier] = {
    try {
      val key = Key(project, moduleName, Some(psiFile))
      Cache.get(key) match {
        case Some(result) => result
        case _ =>
          Cache.invalidate(key)
          Iterable()
      }
    }
    catch {
      case _: UncheckedExecutionException => Iterable()
      case _: ProcessCanceledException => Iterable()
    }
  }

  def refreshForModule(project: Project, moduleName: String, psiFile: PsiFile): Unit = {
    val keys = Cache.asMap().keySet().asScala.filter(k => k.project == project && k.moduleName == moduleName && k.psiFile.contains(psiFile))
    keys.foreach(k => Cache.refresh(k))
  }

  def invalidate(project: Project): Unit = {
    val keys = Cache.asMap().asScala.keys.filter(k => k.project == project)
    keys.foreach(k => Cache.invalidate(k))
  }
}

case class ModuleIdentifier(name: String, moduleName: String, declaration: String, isOperator: Boolean)
