/*
 * Copyright 2014 Rik van der Kleij
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

package com.powertuple.intellij.haskell.psi

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.util.PsiTreeUtil
import com.powertuple.intellij.haskell.{HaskellFile, HaskellLanguage}

object HaskellElementFactory {
  def createQvar(project: Project, name: String): HaskellQvar = {
    val haskellFile = createFileFromText(project, name)
    PsiTreeUtil.findChildOfType(haskellFile, classOf[HaskellQvar])
  }

  def createQcon(project: Project, name: String): HaskellQcon = {
    val haskellFile = createFileFromText(project, name)
    haskellFile.getFirstChild.asInstanceOf[HaskellQcon]
  }

  def createQvarop(project: Project, name: String): HaskellQvarop = {
    val haskellFile = createFileFromText(project, name)
    haskellFile.getFirstChild.asInstanceOf[HaskellQvarop]
  }

  def createQconop(project: Project, name: String) = {
    val haskellFile = createFileFromText(project, name)
    haskellFile.getFirstChild.asInstanceOf[HaskellQconop]
  }

  def createDeclarationFromText(project: Project, text: String): HaskellDeclarationElement = {
    val fileFromText: HaskellFile = createFileFromText(project, text)
    PsiTreeUtil.findChildOfType(fileFromText, classOf[HaskellDeclarationElement])
  }

  private def createFileFromText(project: Project, text: String): HaskellFile = {
    PsiFileFactory.getInstance(project).createFileFromText("a.hs", HaskellLanguage.Instance, text).asInstanceOf[HaskellFile]
  }
}
