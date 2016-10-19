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

import com.intellij.navigation.ChooseByNameContributor
import com.intellij.openapi.project.Project
import intellij.haskell.util.StringUtil._

abstract class HaskellChooseByNameContributor[T] extends ChooseByNameContributor {

  protected def findElementsByName(project: Project, pattern: String, includeNonProjectItems: Boolean): Iterable[T] = {
    val lowerCasePattern = toLowerCase(pattern)
    if (pattern.endsWith(" ")) {
      find((ne: String) => ne == lowerCasePattern)
    } else {
      val patterns = lowerCasePattern.split(' ')
      if (patterns.length == 1) {
        find((ne: String) => ne.startsWith(patterns.head.trim))
      } else {
        find((ne: String) => patterns.contains(ne))
      }
    }
  }

  protected def find(conditionOnLowerCase: String => Boolean): Stream[T]
}
