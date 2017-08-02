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
 *//*
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

package intellij.haskell.action

import com.intellij.ide.actions.GotoActionBase
import com.intellij.ide.util.EditSourceUtil
import com.intellij.ide.util.gotoByName.{ChooseByNameFilter, ChooseByNameLanguageFilter, ChooseByNamePopup, GotoClassSymbolConfiguration}
import com.intellij.lang.Language
import com.intellij.navigation.{ChooseByNameContributor, NavigationItem}
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import intellij.haskell.external.component.{HoogleComponent, StackProjectManager}
import intellij.haskell.navigation.{GotoByHoogleModel, HoogleByNameContributor}
import intellij.haskell.util.HaskellEditorUtil

class HoogleNavigationAction extends GotoActionBase {

  private val contributors = Array[ChooseByNameContributor](new HoogleByNameContributor)

  override def update(actionEvent: AnActionEvent) {
    HaskellEditorUtil.enableExternalAction(actionEvent, (project: Project) => !StackProjectManager.isInitialzing(project) && StackProjectManager.isHoogleAvailable(project) && HoogleComponent.doesHoogleDatabaseExist(project))
  }

  def gotoActionPerformed(actionEvent: AnActionEvent) {
    ActionUtil.findActionContext(actionEvent).foreach(context => {

      val project = context.project
      val model = new GotoByHoogleModel(project, contributors)

      PsiDocumentManager.getInstance(project).commitAllDocuments()

      showNavigationPopup(actionEvent, model, new GotoActionBase.GotoActionCallback[Language]() {
        override protected def createFilter(popup: ChooseByNamePopup): ChooseByNameFilter[Language] = {
          new ChooseByNameLanguageFilter(popup, model, GotoClassSymbolConfiguration.getInstance(project), project)
        }

        def elementChosen(popup: ChooseByNamePopup, element: Any) {
          EditSourceUtil.navigate(element.asInstanceOf[NavigationItem], true, popup.isOpenInCurrentWindowRequested)
        }
      }, "Hoogle for words or type signature", true)
    })
  }
}