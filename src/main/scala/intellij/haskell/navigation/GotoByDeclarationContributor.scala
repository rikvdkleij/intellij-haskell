/*
 * Copyright 2014-2020 Rik van der Kleij
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

import com.intellij.lang.Language
import com.intellij.navigation.{ChooseByNameContributor, GotoClassContributor, NavigationItem}
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.stubs.StubIndex
import com.intellij.util.{ArrayUtil, Processor}
import intellij.haskell.HaskellLanguage
import intellij.haskell.psi.stubs.index.HaskellAllNameIndex
import intellij.haskell.psi.{HaskellNamedElement, HaskellPsiUtil}
import intellij.haskell.util.HaskellProjectUtil

import scala.collection.mutable.ListBuffer
import scala.jdk.CollectionConverters._

class GotoByDeclarationContributor extends GotoClassContributor {

  override def getNames(project: Project, includeNonProjectItems: Boolean): Array[String] = {
    GotoHelper.getNames(project, includeNonProjectItems)
  }

  override def getItemsByName(name: String, pattern: String, project: Project, includeNonProjectItems: Boolean): Array[NavigationItem] = {
    val namedElements = GotoHelper.getNamedElements(name, pattern, project, includeNonProjectItems)
    namedElements.flatMap(ne => HaskellPsiUtil.findHighestDeclarationElement(ne)).toArray
  }

  override def getQualifiedName(item: NavigationItem): String = {
    Option(item.getPresentation).map(_.getPresentableText).getOrElse("-")
  }

  override def getQualifiedNameSeparator: String = {
    "."
  }

  override def getElementKind: String = {
    "declaration"
  }

  override def getElementLanguage: Language = {
    HaskellLanguage.Instance
  }
}

class GotoByNameContributor extends ChooseByNameContributor {

  override def getNames(project: Project, includeNonProjectItems: Boolean): Array[String] = {
    GotoHelper.getNames(project, includeNonProjectItems)
  }

  override def getItemsByName(name: String, pattern: String, project: Project, includeNonProjectItems: Boolean): Array[NavigationItem] = {
    val namedElements = GotoHelper.getNamedElements(name, pattern, project, includeNonProjectItems)
    namedElements.toArray
  }
}

private object GotoHelper {

  def getNames(project: Project, includeNonProjectItems: Boolean): Array[String] = {
    if (HaskellProjectUtil.isHaskellProject(project)) {
      ArrayUtil.toStringArray(StubIndex.getInstance.getAllKeys(HaskellAllNameIndex.Key, project))
    } else {
      Array()
    }
  }

  def getNamedElements(name: String, pattern: String, project: Project, includeNonProjectItems: Boolean): Seq[HaskellNamedElement] = {
    val searchScope = HaskellProjectUtil.getSearchScope(project, includeNonProjectItems)
    val result = ListBuffer[String]()
    val re = pattern.toLowerCase.flatMap(c => StringUtil.escapeToRegexp(c.toString) + ".*")
    val processor = new Processor[String]() {
      override def process(ne: String): Boolean = {
        ProgressManager.checkCanceled()
        if (ne.toLowerCase.matches(re)) {
          result.+=(ne)
        }
        true
      }
    }

    StubIndex.getInstance().processAllKeys(HaskellAllNameIndex.Key, processor, searchScope, null)

    result.flatMap(name => StubIndex.getElements(HaskellAllNameIndex.Key, name, project, searchScope, classOf[HaskellNamedElement]).asScala).toSeq
  }
}