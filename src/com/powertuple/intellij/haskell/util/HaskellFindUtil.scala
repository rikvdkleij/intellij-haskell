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

  def findDeclarationElements(project: Project, includeNonProjectItems: Boolean): Iterable[HaskellDeclarationElement] = {
    getHaskellFiles(project, includeNonProjectItems).flatMap(f => PsiTreeUtil.findChildrenOfType(f, classOf[HaskellDeclarationElement]))
  }

  def findDeclarationElements(project: Project, name: String, includeNonProjectItems: Boolean): Iterable[HaskellDeclarationElement] = {
    findDeclarationElements(project, includeNonProjectItems).filter(de => de.getIdentifierElements.map(_.getName).contains(name))
  }

  def findNamedElements(project: Project, includeNonProjectItems: Boolean): Iterable[HaskellNamedElement] = {
    getHaskellFiles(project, includeNonProjectItems).flatMap(f => PsiTreeUtil.findChildrenOfType(f, classOf[HaskellNamedElement]))
  }

  def findNamedElements(project: Project, name: String, includeNonProjectItems: Boolean): Iterable[HaskellNamedElement] = {
    findNamedElements(project, includeNonProjectItems).filter(_.getName == name)
  }

  private def getHaskellFiles(project: Project, includeNonProjectItems: Boolean) = {
    val scope = if (includeNonProjectItems) {
      GlobalSearchScope.allScope(project)
    } else {
      ModuleManager.getInstance(project).getModules.map(GlobalSearchScope.moduleScope).reduce(_.uniteWith(_))
    }
    HaskellFileIndex.getAllHaskellFiles(project, scope)
  }
}