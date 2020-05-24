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

package intellij.haskell.action

import com.intellij.openapi.vcs.CheckinProjectPanel
import com.intellij.openapi.vcs.changes.CommitContext
import com.intellij.openapi.vcs.checkin.{CheckinHandler, CheckinHandlerFactory}
import intellij.haskell.util.HaskellProjectUtil

class HaskellOptimizeImportsCheckinHandlerFactory extends CheckinHandlerFactory {
  override def createHandler(panel: CheckinProjectPanel, commitContext: CommitContext): CheckinHandler = {
    if (HaskellProjectUtil.isHaskellProject(panel.getProject)) {
      new HaskellOptimizeImportsBeforeCheckinHandler(panel.getProject, panel)
    } else {
      CheckinHandler.DUMMY
    }
  }
}
