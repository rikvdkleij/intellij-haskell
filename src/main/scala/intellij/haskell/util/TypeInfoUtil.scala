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

package intellij.haskell.util

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.util.Computable
import com.intellij.psi.PsiElement
import intellij.haskell.external.component.HaskellComponentsManager
import intellij.haskell.psi.{HaskellNamedElement, HaskellPsiUtil}

object TypeInfoUtil {

  def preloadTypesAround(currentElement: PsiElement) {
    val namedElements = ApplicationManager.getApplication.runReadAction(new Computable[Iterable[HaskellNamedElement]] {

      override def compute(): Iterable[HaskellNamedElement] = {
        HaskellPsiUtil.findExpressionParent(currentElement).map(HaskellPsiUtil.findNamedElements).getOrElse(Iterable())
      }
    })
    namedElements.foreach(e => {
      HaskellComponentsManager.findTypeInfoForElement(e, forceGetInfo = false)
      // We have to wait for other requests which have more prio because those are on dispatch thread
      Thread.sleep(50)
    })
  }
}
