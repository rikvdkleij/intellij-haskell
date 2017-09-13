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
 */

package intellij.haskell.settings

import java.awt.{GridBagConstraints, GridBagLayout, Insets}
import javax.swing._
import javax.swing.event.DocumentEvent

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.{Configurable, ConfigurationException}
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.util.SystemInfo
import com.intellij.ui.DocumentAdapter

import scala.language.{existentials, reflectiveCalls}

class HaskellConfigurable extends Configurable {
  private var isModifiedByUser = false
  private val hindentPathField = new TextFieldWithBrowseButton
  private val stylishHaskellPathField = new TextFieldWithBrowseButton
  private val replTimeoutField = new JTextField
  private val replTimeoutLabel = new JLabel("Changed timeout will take effect after restarting project")

  override def getDisplayName: String = {
    "Haskell"
  }

  override def isModified: Boolean = this.isModifiedByUser

  import HaskellConfigurable._

  override def createComponent: JComponent = {

    hindentPathField.addBrowseFolderListener(
      s"Select $Hindent",
      null,
      null,
      FileChooserDescriptorFactory.createSingleLocalFileDescriptor())

    stylishHaskellPathField.addBrowseFolderListener(
      s"Select $StylishHaskell",
      null,
      null,
      FileChooserDescriptorFactory.createSingleLocalFileDescriptor())

    val settingsPanel = new JPanel(new GridBagLayout())

    val listener: DocumentAdapter = (_: DocumentEvent) => {
      isModifiedByUser = true
    }

    hindentPathField.getTextField.getDocument.addDocumentListener(listener)
    stylishHaskellPathField.getTextField.getDocument.addDocumentListener(listener)
    replTimeoutField.getDocument.addDocumentListener(listener)

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

    addLabeledControl(1, Hindent, hindentPathField)
    addLabeledControl(2, StylishHaskell, stylishHaskellPathField)
    addLabeledControl(3, ReplTimout, replTimeoutField)
    addLabeledControl(4, "", replTimeoutLabel)

    settingsPanel
  }

  override def apply() {
    validatePaths()
    val validREPLTimeout = validateREPLTimeout()

    val state = HaskellSettingsPersistentStateComponent.getInstance().getState
    state.hindentPath = hindentPathField.getText
    state.stylishHaskellPath = stylishHaskellPathField.getText
    state.replTimeout = validREPLTimeout
  }

  private def validatePaths() {
    def validate(command: String, path: String) = {
      val suffix = if (SystemInfo.isWindows) command + ".exe" else command
      if (!path.endsWith(suffix) && !path.trim.isEmpty) {
        throw new ConfigurationException(s"Invalid path to $command")
      }
    }

    Seq((Hindent, hindentPathField.getText), (StylishHaskell, stylishHaskellPathField.getText)
    ).foreach({ case (c, p) => validate(c, p) })
  }

  private def validateREPLTimeout(): Integer = {
    val timeout = try {
      Integer.valueOf(replTimeoutField.getText)
    } catch {
      case _: NumberFormatException => throw new ConfigurationException(s"Invalid REPL timeout")
    }

    if (timeout <= 0) {
      throw new ConfigurationException(s"REPL timeout should be larger than 0")
    }
    timeout
  }

  override def disposeUIResources() {
  }

  override def getHelpTopic: String = ""

  override def reset() {
    val state = HaskellSettingsPersistentStateComponent.getInstance().getState
    hindentPathField.getTextField.setText(state.hindentPath)
    stylishHaskellPathField.getTextField.setText(state.stylishHaskellPath)
    replTimeoutField.setText(state.replTimeout.toString)
  }
}

object HaskellConfigurable {
  final val Hindent = "hindent"
  final val StylishHaskell = "stylish-haskell"
  final val ReplTimout = "REPL timeout in seconds"
}