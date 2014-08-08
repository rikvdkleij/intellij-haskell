/*
 * Copyright 2014 Rik van der Kleij

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

package com.powertuple.intellij.haskell.psi

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFileFactory
import com.powertuple.intellij.haskell.{HaskellFile, HaskellFileType}

object HaskellElementFactory {

  def createVar(project: Project, name: String): HaskellVar = {
    val haskellFile = createFile(project, name)
    haskellFile.getFirstChild.asInstanceOf[HaskellVar]
  }

  def createCon(project: Project, name: String): HaskellCon = {
    val haskellFile = createFile(project, name)
    haskellFile.getFirstChild.asInstanceOf[HaskellCon]
  }

  private def createFile(project: Project, text: String): HaskellFile = {
    val name = "dummy.hs"
    PsiFileFactory.getInstance(project).createFileFromText(name, HaskellFileType.INSTANCE, text).asInstanceOf[HaskellFile]
  }
}
