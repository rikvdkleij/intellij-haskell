/*
 * Copyright 2014-2018 Rik van der Kleij
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

import java.util.concurrent.ConcurrentHashMap

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Computable
import com.intellij.psi.PsiElement
import intellij.haskell.external.component.HaskellComponentsManager
import intellij.haskell.psi.{HaskellNamedElement, HaskellPsiUtil}

import scala.collection.JavaConverters._

object TypeInfoUtil {

  private val activeTaskByProject = new ConcurrentHashMap[Project, Boolean]().asScala

  def preloadTypesAround(currentElement: PsiElement) {
    if (currentElement.isValid) {
      val namedElements = ApplicationManager.getApplication.runReadAction(new Computable[Iterable[HaskellNamedElement]] {

        override def compute(): Iterable[HaskellNamedElement] = {
          HaskellPsiUtil.findExpressionParent(currentElement).map(HaskellPsiUtil.findNamedElements).getOrElse(Iterable())
        }
      })

      val project = currentElement.getProject
      if (activeTaskByProject.get(project).contains(true)) {
        // To prevent growing queue of requests for getting type info
        activeTaskByProject.remove(project)
      }

      ApplicationManager.getApplication.executeOnPooledThread(new Runnable {

        override def run(): Unit = {
          activeTaskByProject.put(project, true)

          namedElements.foreach(e => {
            if (activeTaskByProject.get(project).contains(true)) {
              HaskellComponentsManager.findTypeInfoForElement(e)
              // We have to wait for other requests which have more priority because those are on dispatch thread
              Thread.sleep(100)
            } else {
              return
            }
          })

          activeTaskByProject.remove(project)
        }
      })
    }
  }
}
