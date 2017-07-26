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

package intellij.haskell.navigation

import com.intellij.navigation.{ChooseByNameContributor, NavigationItem}
import com.intellij.openapi.project.Project
import com.intellij.psi.stubs.StubIndex
import com.intellij.util.ArrayUtil
import intellij.haskell.psi.HaskellNamedElement
import intellij.haskell.psi.stubs.index.HaskellAllNameIndex
import intellij.haskell.util.HaskellProjectUtil

class GotoByNameContributor extends ChooseByNameContributor {

  override def getNames(project: Project, includeNonProjectItems: Boolean): Array[String] = {
    if (HaskellProjectUtil.isHaskellProject(project)) {
      ArrayUtil.toStringArray(StubIndex.getInstance.getAllKeys(HaskellAllNameIndex.Key, project))
    } else {
      Array()
    }
  }

  override def getItemsByName(name: String, pattern: String, project: Project, includeNonProjectItems: Boolean): Array[NavigationItem] = {
    val searchScope = HaskellProjectUtil.getSearchScope(project, includeNonProjectItems)
    val namedElements = StubIndex.getElements(HaskellAllNameIndex.Key, name, project, searchScope, classOf[HaskellNamedElement])
    namedElements.toArray(new Array[NavigationItem](namedElements.size))
  }
}
