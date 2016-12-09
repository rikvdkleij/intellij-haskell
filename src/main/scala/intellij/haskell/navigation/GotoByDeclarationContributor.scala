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

package intellij.haskell.navigation

import com.intellij.navigation.NavigationItem
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiManager
import intellij.haskell.psi.{HaskellNamedElement, HaskellPsiUtil}
import intellij.haskell.util.StringUtil._
import intellij.haskell.util.{HaskellFileUtil, HaskellProjectUtil}

class GotoByDeclarationContributor extends HaskellChooseByNameContributor[HaskellNamedElement] {

  private var simpleCache: Iterable[HaskellNamedElement] = _

  override def getNames(project: Project, includeNonProjectItems: Boolean): Array[String] = {
    val psiManager = PsiManager.getInstance(project)
    val declarationElements = HaskellProjectUtil.findHaskellFiles(project, includeNonProjectItems).flatMap(vf => HaskellFileUtil.convertToHaskellFile(vf, psiManager).
      map(f => HaskellPsiUtil.findDeclarationElements(f)).getOrElse(Stream()))
    val elements = declarationElements.flatMap(_.getIdentifierElements)
    simpleCache = elements
    elements.map(n => n.getName).toArray
  }

  override def getItemsByName(name: String, pattern: String, project: Project, includeNonProjectItems: Boolean): Array[NavigationItem] = {
    findElementsByName(project, pattern, includeNonProjectItems).toArray
  }

  protected def find(conditionOnLowerCase: String => Boolean): Iterable[HaskellNamedElement] = {
    simpleCache.filter(ne => conditionOnLowerCase(toLowerCase(ne.getName)))
  }
}
