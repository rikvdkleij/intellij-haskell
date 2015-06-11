/*
 * Copyright 2015 Rik van der Kleij
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

import com.intellij.lang.ASTNode
import com.intellij.openapi.project.Project
import com.intellij.psi.codeStyle.CodeStyleSettingsManager
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.{PsiFileFactory, PsiWhiteSpace}
import com.powertuple.intellij.haskell.util.OSUtil
import com.powertuple.intellij.haskell.{HaskellFile, HaskellFileType, HaskellLanguage}

object HaskellElementFactory {
  def createVarId(project: Project, name: String): ASTNode = {
    val haskellFile = createFileFromText(project, name)
    PsiTreeUtil.findChildOfType(haskellFile, classOf[HaskellVarId]).getNode
  }


  def createConId(project: Project, name: String): ASTNode = {
    val haskellFile = createFileFromText(project, name)
    PsiTreeUtil.findChildOfType(haskellFile, classOf[HaskellConId]).getNode
  }

  def createVarSym(project: Project, name: String): ASTNode = {
    val haskellFile = createFileFromText(project, name)
    PsiTreeUtil.findChildOfType(haskellFile, classOf[HaskellVarSym]).getNode
  }

  def createVarDotSym(project: Project, name: String): ASTNode = {
    val haskellFile = createFileFromText(project, name)
    PsiTreeUtil.findChildOfType(haskellFile, classOf[HaskellVarDotSym]).getNode
  }

  def createConSym(project: Project, name: String): ASTNode = {
    val haskellFile = createFileFromText(project, name)
    PsiTreeUtil.findChildOfType(haskellFile, classOf[HaskellConSym]).getNode
  }

  def createExpression(project: Project, expression: String): HaskellExpression = {
    val haskellFile = createFileFromText(project, expression)
    PsiTreeUtil.findChildOfType(haskellFile, classOf[HaskellExpression])
  }

  def createQVarConOp(project: Project, qVarConOp: String): HaskellQVarConOpElement = {
    val haskellFile = createFileFromText(project, qVarConOp)
    PsiTreeUtil.findChildOfType(haskellFile, classOf[HaskellQVarConOpElement])
  }

  def createBody(project: Project, body: String): HaskellModuleBody = {
    val haskellFile = createFileFromText(project, body)
    PsiTreeUtil.findChildOfType(haskellFile, classOf[HaskellModuleBody])
  }

  def createTopDeclaration(project: Project, declaration: String): HaskellTopDeclaration = {
    val haskellFile = createFileFromText(project, declaration)
    PsiTreeUtil.findChildOfType(haskellFile, classOf[HaskellTopDeclaration])
  }

  def createLanguagePragma(project: Project, languagePragma: String): HaskellLanguagePragma = {
    val haskellFile = createFileFromText(project, languagePragma)
    PsiTreeUtil.findChildOfType(haskellFile, classOf[HaskellLanguagePragma])
  }

  def createWhiteSpace(project: Project, space: String = " ") = {
    val haskellFile = createFileFromText(project, space)
    PsiTreeUtil.findChildOfType(haskellFile, classOf[PsiWhiteSpace])
  }

  def createTab(project: Project) = {
    val tabSize = CodeStyleSettingsManager.getInstance().getCurrentSettings.getTabSize(HaskellFileType.INSTANCE)
    createWhiteSpace(project, " " * tabSize)
  }

  def createNewLine(project: Project) = {
    createFileFromText(project, OSUtil.LineSeparator.toString).getFirstChild
  }

  def createQualifier(project: Project, qualifier: String) = {
    val haskellFile = createFileFromText(project, qualifier + ".dummy")
    PsiTreeUtil.findChildOfType(haskellFile, classOf[HaskellQualifier]).getNode
  }

  def createModId(project: Project, name: String) = {
    val haskellFile = createFileFromText(project, "module " + name)
    PsiTreeUtil.findChildOfType(haskellFile, classOf[HaskellModId]).getNode
  }

  private def createFileFromText(project: Project, text: String): HaskellFile = {
    PsiFileFactory.getInstance(project).createFileFromText("a.hs", HaskellLanguage.Instance, text).asInstanceOf[HaskellFile]
  }
}
