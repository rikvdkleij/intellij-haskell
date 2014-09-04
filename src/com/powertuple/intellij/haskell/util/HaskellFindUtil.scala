/*
 * Copyright 2014 Rik van der Kleij
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

package com.powertuple.intellij.haskell.util

import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTreeUtil
import com.powertuple.intellij.haskell.psi._

import scala.collection.JavaConversions._

object HaskellFindUtil {
  private var modulesScope: Option[GlobalSearchScope] = None

  def findDeclarationElements(project: Project, includeNonProjectItems: Boolean): Iterable[HaskellDeclarationElement] = {
    val scope = if (includeNonProjectItems) {
      GlobalSearchScope.allScope(project)
    } else {
      getModulesScope(project)
    }
    val haskellFiles = HaskellFileIndex.getAllHaskellFiles(project, scope)
    haskellFiles.flatMap(f => Option(PsiTreeUtil.findChildrenOfType(f, classOf[HaskellDeclarationElement]))).flatten
  }

  def findDeclarationElements(project: Project, name: String, includeNonProjectItems: Boolean): Iterable[HaskellDeclarationElement] = {
    findDeclarationElements(project, includeNonProjectItems).filter(de => de.getIdentifierElement.getName == name)
  }

  def findNamedElements(project: Project, includeNonProjectItems: Boolean): Iterable[HaskellNamedElement] = {
    val scope = if (includeNonProjectItems) {
      GlobalSearchScope.allScope(project)
    } else {
      getModulesScope(project)
    }
    val haskellFiles = HaskellFileIndex.getAllHaskellFiles(project, scope)
    haskellFiles.flatMap(f => Option(PsiTreeUtil.findChildrenOfType(f, classOf[HaskellNamedElement]))).flatten
  }

  def findNamedElements(project: Project, name: String, includeNonProjectItems: Boolean): Iterable[HaskellNamedElement] = {
    findNamedElements(project, includeNonProjectItems).filter(_.getName == name)
  }

  private def getModulesScope(project: Project) = {
    modulesScope match {
      case Some(s) => s
      case None => {
        modulesScope = Some(ModuleManager.getInstance(project).getModules.map(m => GlobalSearchScope.moduleScope(m)).reduce(_.uniteWith(_)))
        modulesScope.get
      }
    }
  }
}