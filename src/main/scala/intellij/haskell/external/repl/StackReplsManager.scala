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

package intellij.haskell.external.repl

import com.intellij.openapi.project.Project

object StackReplsManager {

  def getProjectRepl(project: Project): Option[ProjectStackReplProcess] = {
    if (project.isDisposed) {
      None
    } else {
      Option(project.getComponent(classOf[ProjectStackReplProcess]))
    }
  }

  def getGlobalRepl(project: Project): Option[GlobalStackReplProcess] = {
    if (project.isDisposed) {
      None
    } else {
      Option(project.getComponent(classOf[GlobalStackReplProcess]))
    }
  }
}


