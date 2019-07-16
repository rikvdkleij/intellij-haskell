/*
 * Copyright 2014-2019 Rik van der Kleij
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

import com.intellij.openapi.options.{Configurable, ConfigurationException}
import com.intellij.ui.DocumentAdapter
import javax.swing._
import javax.swing.event.DocumentEvent

class HaskellConfigurable extends Configurable {
  private var isModifiedByUser = false
  private val hlintOptionsField = new JTextField
  private val replTimeoutField = new JTextField
  private val replTimeoutLabel = new JLabel("Changed timeout will take effect after restarting project")
  private val newProjectTemplateNameField = new JTextField

  override def getDisplayName: String = {
    "Haskell"
  }

  override def isModified: Boolean = this.isModifiedByUser

  import HaskellConfigurable._

  override def createComponent: JComponent = {

    val settingsPanel = new JPanel(new GridBagLayout())

    settingsPanel.getInsets()
    val listener: DocumentAdapter = (_: DocumentEvent) => {
      isModifiedByUser = true
    }

    hlintOptionsField.getDocument.addDocumentListener(listener)
    replTimeoutField.getDocument.addDocumentListener(listener)
    newProjectTemplateNameField.getDocument.addDocumentListener(listener)

    class SettingsGridBagConstraints extends GridBagConstraints {

      def setConstraints(anchor: Int = GridBagConstraints.CENTER, gridx: Int, gridy: Int, weightx: Double = 0, weighty: Double = 0, fill: Int = GridBagConstraints.NONE): SettingsGridBagConstraints = {
        this.anchor = anchor
        this.gridx = gridx
        this.gridy = gridy
        this.weightx = weightx
        this.weighty = weighty
        this.fill = fill
        this.insets = new Insets(2, 0, 2, 3)
        this
      }
    }

    val baseGridBagConstraints = new SettingsGridBagConstraints

    def addLabeledControl(row: Int, label: String, component: JComponent): Unit = {
      settingsPanel.add(new JLabel(label), baseGridBagConstraints.setConstraints(
        anchor = GridBagConstraints.LINE_START,
        gridx = 0,
        gridy = row
      ))

      settingsPanel.add(component, baseGridBagConstraints.setConstraints(
        gridx = 1,
        gridy = row,
        fill = GridBagConstraints.HORIZONTAL,
        weightx = 1.0
      ))

      settingsPanel.add(Box.createHorizontalStrut(1), baseGridBagConstraints.setConstraints(
        gridx = 2,
        gridy = row,
        weightx = 0.1
      ))
    }

    addLabeledControl(1, HlintOptions, hlintOptionsField)
    addLabeledControl(2, ReplTimeout, replTimeoutField)
    addLabeledControl(3, "", replTimeoutLabel)
    addLabeledControl(4, NewProjectTemplateName, newProjectTemplateNameField)

    settingsPanel.add(new JPanel(), baseGridBagConstraints.setConstraints(
      gridx = 0,
      gridy = 7,
      weighty = 10.0
    ))
    settingsPanel
  }

  override def apply(): Unit = {
    val validREPLTimeout = validateREPLTimeout()

    val state = HaskellSettingsPersistentStateComponent.getInstance().getState
    state.replTimeout = validREPLTimeout
    state.hlintOptions = hlintOptionsField.getText
    state.newProjectTemplateName = newProjectTemplateNameField.getText
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

  override def disposeUIResources(): Unit = {}

  override def getHelpTopic: String = ""

  override def reset(): Unit = {
    val state = HaskellSettingsPersistentStateComponent.getInstance().getState
    hlintOptionsField.setText(state.hlintOptions)
    replTimeoutField.setText(state.replTimeout.toString)
    newProjectTemplateNameField.setText(state.newProjectTemplateName)
  }
}

object HaskellConfigurable {
  final val ReplTimeout = "Background REPL timeout in seconds"
  final val HlintOptions = "Hlint options"
  final val NewProjectTemplateName = "Template name for new project"
}