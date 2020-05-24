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

package intellij.haskell.navigation

import com.intellij.BundleBase
import com.intellij.ide.IdeBundle
import com.intellij.ide.util.PropertiesComponent
import com.intellij.ide.util.gotoByName.{CustomMatcherModel, FilteringGotoByModel}
import com.intellij.lang.Language
import com.intellij.navigation.{ChooseByNameContributor, NavigationItem}
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.SystemInfo
import com.intellij.psi.PsiElement
import com.intellij.psi.presentation.java.SymbolPresentationUtil
import javax.swing.ListCellRenderer

class GotoByHoogleModel(val project: Project, val contributors: Array[ChooseByNameContributor]) extends FilteringGotoByModel[Language](project, contributors) with CustomMatcherModel {

  // Helping the Scala compiler to see that ListCellRenderer is parameterized by AnyRef
  override def getListCellRenderer: ListCellRenderer[_] = super.getListCellRenderer

  protected def filterValueFor(item: NavigationItem): Language = {
    null
  }

  def getPromptText: String = {
    "Hoogle for it"
  }

  def getCheckBoxName: String = {
    BundleBase.replaceMnemonicAmpersand("Include &non-&&project identifiers")
  }

  def getNotInMessage: String = {
    IdeBundle.message("label.no.matches.found.in.project", project.getName)
  }

  def getNotFoundMessage: String = {
    IdeBundle.message("label.no.matches.found")
  }

  override def getCheckBoxMnemonic: Char = {
    // Some combination like Alt+N, Ant+O, etc are a dead symbols, therefore
    // we have to change mnemonics for Mac users.
    if (SystemInfo.isMac) 'P' else 'n'
  }

  def loadInitialCheckBoxState: Boolean = {
    val propertiesComponent: PropertiesComponent = PropertiesComponent.getInstance(myProject)
    true.toString == propertiesComponent.getValue("GoToClass.toSaveIncludeLibraries") && true.toString == propertiesComponent.getValue("GoToSymbol.includeLibraries")
  }

  def saveInitialCheckBoxState(state: Boolean): Unit = {
    val propertiesComponent: PropertiesComponent = PropertiesComponent.getInstance(myProject)
    if (true.toString == propertiesComponent.getValue("GoToClass.toSaveIncludeLibraries")) {
      propertiesComponent.setValue("GoToSymbol.includeLibraries", state.toString)
    }
  }

  def getFullName(element: Any): String = {
    Option(getElementName(element)).map(name => {
      element match {
        case e: PsiElement => SymbolPresentationUtil.getSymbolContainerText(e) + "." + name
        case _ => name
      }
    }).orNull
  }

  def getSeparators: Array[String] = {
    new Array[String](0)
  }

  def willOpenEditor: Boolean = {
    true
  }

  def matches(popupItem: String, userPattern: String): Boolean = {
    true
  }
}