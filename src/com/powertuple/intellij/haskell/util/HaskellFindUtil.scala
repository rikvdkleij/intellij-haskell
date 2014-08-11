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

import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTreeUtil
import com.powertuple.intellij.haskell.psi.{HaskellDeclarationElement, HaskellStartDefinition}

object HaskellFindUtil {

  def findDeclarations(project: Project, includeNonProjectItems: Boolean): Iterable[HaskellDeclarationElement] = {
    val scope = if (includeNonProjectItems) {
      GlobalSearchScope.allScope(project)
    } else {
      GlobalSearchScope.projectScope(project)
    }
    val haskellFiles = HaskellFileIndex.getAllHaskellFiles(project, scope)
    haskellFiles.flatMap(f => Option(PsiTreeUtil.getChildrenOfType(f, classOf[HaskellDeclarationElement]))).flatten
  }

  def findDeclarations(project: Project, name: String, includeNonProjectItems: Boolean): Iterable[HaskellDeclarationElement] = {
    findDeclarations(project, includeNonProjectItems).filter(hv => hv.getIdentifier == name)
  }

  def findDefinitions(project: Project, includeNonProjectItems: Boolean): Iterable[HaskellStartDefinition] = {
    val scope = if (includeNonProjectItems) {
      GlobalSearchScope.allScope(project)
    } else {
      GlobalSearchScope.projectScope(project)
    }
    val haskellFiles = HaskellFileIndex.getAllHaskellFiles(project, scope)
    haskellFiles.flatMap(f => Option(PsiTreeUtil.getChildrenOfType(f, classOf[HaskellStartDefinition]))).flatten
  }

  def findDefinitions(project: Project, name: String, includeNonProjectItems: Boolean): Iterable[HaskellStartDefinition] = {
    findDefinitions(project, includeNonProjectItems).filter(hv => hv.getIdentifier == name)
  }
}