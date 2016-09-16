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
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import intellij.haskell.external.repl.StackReplsManager

import scala.collection.JavaConversions._

private[component] object BrowseModuleComponent {

  private final val PackageNameQualifierPattern = """([a-z\-]+[0-9\.]+:)?([A-Z][A-Za-z\-\']+\.)+"""

  private case class Key(project: Project, moduleName: String, psiFile: Option[PsiFile], allTopLevel: Boolean)

  private val executor = Executors.newCachedThreadPool()

  private final val Cache = CacheBuilder.newBuilder()
    .build(
      new CacheLoader[Key, Iterable[ModuleIdentifier]]() {

        override def load(key: Key): Iterable[ModuleIdentifier] = {
          findModuleIdentifiers(key)
        }

        override def reload(key: Key, oldValue: Iterable[ModuleIdentifier]): ListenableFuture[Iterable[ModuleIdentifier]] = {
          val task = ListenableFutureTask.create(new Callable[Iterable[ModuleIdentifier]]() {
            def call() = {
              findModuleIdentifiers(key)
            }
          })
          executor.execute(task)
          task
        }

        private def findModuleIdentifiers(key: Key): Iterable[ModuleIdentifier] = {
          val project = key.project
          val moduleName = key.moduleName
          if (ProjectModulesComponent.findAvailableModules(project).allModuleNames.contains(moduleName)) {
            if (key.allTopLevel) {
              val outputLines = StackReplsManager.getProjectRepl(project).getAllTopLevelModuleIdentifiers(moduleName, key.psiFile).stdOutLines
              val definedLocallyLines = outputLines.takeWhile(l => !l.startsWith("-- imported via"))
              definedLocallyLines.flatMap(findModuleIdentifier(_, moduleName, project))
            } else {
              StackReplsManager.getProjectRepl(project).getModuleIdentifiers(moduleName, key.psiFile).stdOutLines.flatMap(findModuleIdentifier(_, moduleName, project))
            }
          } else {
            StackReplsManager.getGlobalRepl(project).getModuleIdentifiers(moduleName).stdOutLines.flatMap(findModuleIdentifier(_, moduleName, project))
          }
        }

        private def findModuleIdentifier(outputLine: String, moduleName: String, project: Project): Option[ModuleIdentifier] = {
          val declaration = outputLine.replaceAll(PackageNameQualifierPattern, "").replaceAll("""\s+""", " ").replaceAll("""\{\-(.+)\-\}""", "")
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
          } else if (Seq("data", "type").contains(allTokens(0).trim)) {
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

  def findImportedModuleIdentifiers(project: Project, moduleName: String, psiFile: Option[PsiFile]): Iterable[ModuleIdentifier] = {
    try {
      Cache.get(Key(project, moduleName, psiFile, allTopLevel = false))
    }
    catch {
      case _: UncheckedExecutionException => Iterable()
      case _: ProcessCanceledException => Iterable()
    }
  }

  def findAllTopLevelModuleIdentifiers(project: Project, moduleName: String, psiFile: Option[PsiFile]): Iterable[ModuleIdentifier] = {
    try {
      Cache.get(Key(project, moduleName, psiFile, allTopLevel = true))
    }
    catch {
      case _: UncheckedExecutionException => Iterable()
      case _: ProcessCanceledException => Iterable()
    }
  }

  def isCacheEmptyForModule(moduleName: String): Boolean = {
    !Cache.asMap().exists(_._1.moduleName == moduleName)
  }

  def refreshForModule(project: Project, moduleName: String): Unit = {
    val keys = Cache.asMap().filter(k => k._1.project == project && k._1.moduleName == moduleName).keys
    keys.foreach(k => Cache.refresh(k))
  }

  def invalidate(): Unit = {
    val keys = Cache.asMap().keys
    keys.foreach(k => Cache.invalidate(k))
  }
}

case class ModuleIdentifier(name: String, moduleName: String, declaration: String, isOperator: Boolean)
