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

package intellij.haskell.psi

import java.util

import com.intellij.openapi.project.Project
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.{PsiElement, PsiFileFactory, PsiWhiteSpace}
import intellij.haskell.psi.HaskellTypes._
import intellij.haskell.{HaskellFile, HaskellLanguage}

import scala.jdk.CollectionConverters._

object HaskellElementFactory {

  def createUnderscore(project: Project): Option[HaskellExpression] = {
    createElement(project, "_", classOf[HaskellExpression])
  }

  def createVarid(project: Project, name: String): Option[HaskellVarid] = {
    createElement(project, name, classOf[HaskellVarid])
  }

  def createConid(project: Project, name: String): Option[HaskellConid] = {
    createElement(project, name, classOf[HaskellConid])
  }

  def createVarsym(project: Project, name: String): Option[HaskellVarsym] = {
    createElement(project, name, classOf[HaskellVarsym])
  }

  def createConsym(project: Project, name: String): Option[HaskellConsym] = {
    createElement(project, name, classOf[HaskellConsym])
  }

  def createImportId(project: Project, identifier: String): Option[HaskellImportId] = {
    createElement(project, s"import Foo(${surroundWithParensIfSymbol(project, identifier)})", classOf[HaskellImportId])
  }

  def createQualifiedNameElement(project: Project, name: String): Option[HaskellQualifiedNameElement] = {
    createElement(project, name, classOf[HaskellQualifiedNameElement])
  }

  def createBody(project: Project, body: String): Option[HaskellModuleBody] = {
    createElement(project, body, classOf[HaskellModuleBody])
  }

  def createTopDeclaration(project: Project, declaration: String): Option[HaskellTopDeclaration] = {
    createElement(project, declaration, classOf[HaskellTopDeclaration])
  }

  def createDataDeclaration(project: Project, declaration: String): Option[HaskellDataDeclaration] = {
    createElement(project, declaration, classOf[HaskellDataDeclaration])
  }

  def createLanguagePragma(project: Project, languagePragma: String): Option[HaskellPragma] = {
    createElement(project, languagePragma, classOf[HaskellPragma])
  }

  def createLeafPsiElements(project: Project, code: String): util.Collection[LeafPsiElement] = {
    val haskellFile = createFileFromText(project, code)
    PsiTreeUtil.findChildrenOfType(haskellFile, classOf[LeafPsiElement])
  }

  def getLeftParenElement(project: Project): LeafPsiElement = {
    createLeafPsiElements(project, "add = (1 + 2)").asScala.find(_.getNode.getElementType == HS_LEFT_PAREN).getOrElse(throw new IllegalStateException())
  }

  def getRightParenElement(project: Project): LeafPsiElement = {
    createLeafPsiElements(project, "add = (1 + 2)").asScala.find(_.getNode.getElementType == HS_RIGHT_PAREN).getOrElse(throw new IllegalStateException())
  }

  def createWhiteSpace(project: Project, space: String = " "): Option[PsiWhiteSpace] = {
    createElement(project, space, classOf[PsiWhiteSpace])
  }

  def createComma(project: Project): PsiElement = {
    val haskellFile = createFileFromText(project, ",")
    haskellFile.getLastChild
  }

  def createNewLine(project: Project): PsiElement = {
    createFileFromText(project, "\n").getFirstChild
  }

  def createQualifier(project: Project, qualifier: String): Option[HaskellQualifier] = {
    createElement(project, "test = " + qualifier + ".bla", classOf[HaskellQualifier])
  }

  def createQConQualifier(project: Project, qConQualifier: String): Option[HaskellQConQualifier] = {
    createElement(project, qConQualifier, classOf[HaskellQConQualifier])
  }

  def createModid(project: Project, moduleName: String): Option[HaskellModid] = {
    val haskellModuleDeclaration = createElement(project, s"module $moduleName where", classOf[HaskellModuleDeclaration])
    haskellModuleDeclaration.map(_.getModid)
  }

  def createImportDeclaration(project: Project, moduleName: String, identifier: String): Option[HaskellImportDeclaration] = {
    createElement(project, s"import $moduleName (${surroundWithParensIfSymbol(project, identifier)})\n", classOf[HaskellImportDeclaration])
  }

  private def surroundWithParensIfSymbol(project: Project, identifier: String): String = {
    if (createVarid(project, identifier).isDefined || createConid(project, identifier).isDefined) {
      identifier
    } else {
      s"($identifier)"
    }
  }

  def createImportDeclaration(project: Project, importDecl: String): Option[HaskellImportDeclaration] = {
    createElement(project, s"$importDecl \n", classOf[HaskellImportDeclaration])
  }

  def createQuasiQuote(project: Project, quasiQuoteText: String): Option[HaskellQuasiQuote] = {
    createElement(project, quasiQuoteText, classOf[HaskellQuasiQuote])
  }

  private def createElement[C <: PsiElement](project: Project, newName: String, namedElementClass: Class[C]): Option[C] = {
    val file = createFileFromText(project, newName)
    Option(PsiTreeUtil.findChildOfType(file, namedElementClass))
  }

  private def createFileFromText(project: Project, text: String): HaskellFile = {
    PsiFileFactory.getInstance(project).createFileFromText("a.hs", HaskellLanguage.Instance, text).asInstanceOf[HaskellFile]
  }
}
