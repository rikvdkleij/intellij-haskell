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

package intellij.haskell.settings

import java.awt.{GridBagConstraints, GridBagLayout, Insets}
import javax.swing._
import javax.swing.event.DocumentEvent

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.{Configurable, ConfigurationException}
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.DocumentAdapter
import intellij.haskell.external.GhcModProcessManager

import scala.language.{existentials, reflectiveCalls}

class HaskellConfigurable extends Configurable {
  private var isModifiedByUser = false
  private val ghcModPathField = new TextFieldWithBrowseButton
  private val haskellDocsPathField = new TextFieldWithBrowseButton
  private val hlintPathField = new TextFieldWithBrowseButton
  private val stackPathField = new TextFieldWithBrowseButton

  override def getDisplayName: String = {
    "Haskell"
  }

  override def isModified: Boolean = this.isModifiedByUser

  import HaskellConfigurable._

  override def createComponent: JComponent = {

    ghcModPathField.addBrowseFolderListener(
      s"Select $GhcMod",
      null,
      null,
      FileChooserDescriptorFactory.createSingleLocalFileDescriptor())

    haskellDocsPathField.addBrowseFolderListener(
      s"Select $HaskellDocs",
      null,
      null,
      FileChooserDescriptorFactory.createSingleLocalFileDescriptor())

    hlintPathField.addBrowseFolderListener(
      s"Select $Hlint",
      null,
      null,
      FileChooserDescriptorFactory.createSingleLocalFileDescriptor())

    stackPathField.addBrowseFolderListener(
      s"Select $Stack",
      null,
      null,
      FileChooserDescriptorFactory.createSingleLocalFileDescriptor())

    val settingsPanel = new JPanel(new GridBagLayout())

    val listener: DocumentAdapter = new DocumentAdapter() {
      override def textChanged(e: DocumentEvent) {
        isModifiedByUser = true
      }
    }

    ghcModPathField.getTextField.getDocument.addDocumentListener(listener)
    haskellDocsPathField.getTextField.getDocument.addDocumentListener(listener)
    hlintPathField.getTextField.getDocument.addDocumentListener(listener)
    stackPathField.getTextField.getDocument.addDocumentListener(listener)

    val base = new GridBagConstraints {
      insets = new Insets(2, 0, 2, 3)

      def setConstraints(anchor: Int = GridBagConstraints.CENTER, gridx: Int, gridy: Int, weightx: Double = 0, weighty: Double = 0, fill: Int = GridBagConstraints.NONE) = {
        this.anchor = anchor
        this.gridx = gridx
        this.gridy = gridy
        this.weightx = weightx
        this.weighty = weighty
        this.fill = fill
        this
      }
    }

    def addLabeledControl(row: Int, label: String, component: JComponent) {
      settingsPanel.add(new JLabel(label), base.setConstraints(
        anchor = GridBagConstraints.LINE_START,
        gridx = 0,
        gridy = row
      ))

      settingsPanel.add(component, base.setConstraints(
        gridx = 1,
        gridy = row,
        fill = GridBagConstraints.HORIZONTAL,
        weightx = 1.0
      ))

      settingsPanel.add(Box.createHorizontalStrut(1), base.setConstraints(
        gridx = 2,
        gridy = row,
        weightx = 0.1
      ))
    }

    addLabeledControl(0, GhcMod, ghcModPathField)
    addLabeledControl(1, HaskellDocs, haskellDocsPathField)
    addLabeledControl(3, Hlint, hlintPathField)
    addLabeledControl(4, Stack, stackPathField)

    settingsPanel.add(new JPanel(), base.setConstraints(
      gridx = 0,
      gridy = 5,
      weighty = 10.0
    ))

    settingsPanel
  }

  override def apply() {
    validatePaths()

    val state = HaskellSettings.getInstance().getState
    state.ghcModPath = ghcModPathField.getText
    state.haskellDocsPath = haskellDocsPathField.getText
    state.hlintPath = hlintPathField.getText
    state.stackPath = stackPathField.getText

    GhcModProcessManager.setInRestartState()

    isModifiedByUser = false
  }

  private def validatePaths() {
    def validate(command: String, path: String) = {
      if (!path.endsWith(command) && !path.trim.isEmpty) {
        throw new ConfigurationException(s"Invalid path to $command")
      }
    }
    Seq((GhcMod, ghcModPathField.getText),
      (HaskellDocs, haskellDocsPathField.getText),
      (Hlint, hlintPathField.getText),
      (Stack, stackPathField.getText)
    ).foreach({ case (c, p) => validate(c, p) })
  }

  override def disposeUIResources() {
  }

  override def getHelpTopic: String = ""

  override def reset() {
    val state = HaskellSettings.getInstance().getState
    ghcModPathField.getTextField.setText(state.ghcModPath)
    haskellDocsPathField.getTextField.setText(state.haskellDocsPath)
    hlintPathField.getTextField.setText(state.hlintPath)
    stackPathField.getTextField.setText(state.stackPath)

    isModifiedByUser = false
  }
}

object HaskellConfigurable {
  val GhcMod = "ghc-mod"
  val HaskellDocs = "haskell-docs"
  val Hlint = "hlint"
  val Stack = "stack"
}