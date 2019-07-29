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
import javax.swing.event.{ChangeListener, DocumentEvent}

class HaskellConfigurable extends Configurable {
  private var isModifiedByUser = false
  private val hlintOptionsField = new JTextField
  private val useSystemGhcToggle = new JCheckBox
  private val replTimeoutField = new JTextField
  private val afterRestartLabel = new JLabel("Changes will take effect after restarting project")
  private val newProjectTemplateNameField = new JTextField
  private val hindentPathField = new JTextField
  private val hlintPathField = new JTextField
  private val hooglePathField = new JTextField
  private val stylishHaskellPathField = new JTextField
  private val useCustomToolsToggle = new JCheckBox

  override def getDisplayName: String = {
    "Haskell"
  }

  override def isModified: Boolean = this.isModifiedByUser

  import HaskellConfigurable._

  override def createComponent: JComponent = {

    def toggleToolPathsVisibility(): Unit = {
      val visible = useCustomToolsToggle.isSelected
      hindentPathField.setVisible(visible)
      hlintPathField.setVisible(visible)
      hooglePathField.setVisible(visible)
      stylishHaskellPathField.setVisible(visible)
    }

    toggleToolPathsVisibility()

    val settingsPanel = new JPanel(new GridBagLayout())

    settingsPanel.getInsets()
    val docListener: DocumentAdapter = (_: DocumentEvent) => {
      isModifiedByUser = true
    }

    hlintOptionsField.getDocument.addDocumentListener(docListener)
    replTimeoutField.getDocument.addDocumentListener(docListener)
    newProjectTemplateNameField.getDocument.addDocumentListener(docListener)
    hindentPathField.getDocument.addDocumentListener(docListener)
    hlintPathField.getDocument.addDocumentListener(docListener)
    hooglePathField.getDocument.addDocumentListener(docListener)
    stylishHaskellPathField.getDocument.addDocumentListener(docListener)
    useSystemGhcToggle.addChangeListener { _ =>
      isModifiedByUser = true
    }
    useCustomToolsToggle.addChangeListener { _ =>
      isModifiedByUser = true
      toggleToolPathsVisibility()
    }

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

    def addLabeledControl(row: Int, label: JLabel, component: JComponent): Unit = {
      settingsPanel.add(label, baseGridBagConstraints.setConstraints(
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

    val labeledControls = List(
      (new JLabel(HlintOptions), hlintOptionsField),
      (new JLabel(ReplTimeout), replTimeoutField),
      (new JLabel(""), afterRestartLabel),
      (new JLabel(NewProjectTemplateName), newProjectTemplateNameField),
      (new JLabel(BuildToolsUsingSystemGhc), useSystemGhcToggle),
      (new JLabel(UseCustomTool), useCustomToolsToggle),
      (new JLabel(""), afterRestartLabel),
      (new JLabel(""),  {
        val x = new JTextArea(PathWarning)
        x.setLineWrap(true)
        x.setWrapStyleWord(true)
        x
      }),
      (new JLabel(HindentPath), hindentPathField),
      (new JLabel(HlintPath), hlintPathField),
      (new JLabel(HooglePath), hooglePathField),
      (new JLabel(StylishHaskellPath), stylishHaskellPathField)
    )

    labeledControls.zipWithIndex.foreach {
      case ((label, control), row) => addLabeledControl(row, label, control)
    }

    settingsPanel.add(new JPanel(), baseGridBagConstraints.setConstraints(
      gridx = 0,
      gridy = labeledControls.length,
      weighty = 10.0
    ))
    settingsPanel
  }

  override def apply(): Unit = {
    val validREPLTimeout = validateREPLTimeout()

    val state = HaskellSettingsPersistentStateComponent.getInstance().getState
    state.replTimeout = validREPLTimeout
    state.hlintOptions = hlintOptionsField.getText
    state.useSystemGhc = useSystemGhcToggle.isSelected
    state.newProjectTemplateName = newProjectTemplateNameField.getText
    state.hindentPath = hindentPathField.getText
    state.hlintPath = hlintPathField.getText
    state.hooglePath = hooglePathField.getText
    state.stylishHaskellPath = stylishHaskellPathField.getText
    state.customPaths = useCustomToolsToggle.isSelected
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
    useSystemGhcToggle.setSelected(state.useSystemGhc)
    replTimeoutField.setText(state.replTimeout.toString)
    newProjectTemplateNameField.setText(state.newProjectTemplateName)
    hindentPathField.setText(state.hindentPath)
    hlintPathField.setText(state.hlintPath)
    hooglePathField.setText(state.hooglePath)
    stylishHaskellPathField.setText(state.stylishHaskellPath)
    useCustomToolsToggle.setSelected(state.customTools)
  }
}

object HaskellConfigurable {
  final val ReplTimeout = "Background REPL timeout in seconds"
  final val HlintOptions = "Hlint options"
  final val NewProjectTemplateName = "Template name for new project"
  final val BuildToolsUsingSystemGhc = "Build tools using system GHC"
  final val HindentPath = "Hindent path"
  final val HlintPath = "Hlint path"
  final val HooglePath = "Hoogle path"
  final val StylishHaskellPath = "Stylish Haskell path"
  final val UseCustomTool = "Use custom Haskell tools"
  final val PathWarning =
    """WARNING! Specifying a path for a Haskell tool will override the default
      |behavior of building that tool from the Stackage LTS. This plugin was
      |tested only with the Haskell tools from the Stackage LTS. Providing a
      |path with your own Haskell tool (and thus overriding automatic rebuild
      |or download of that tool) could cause some features of this plugin to
      |break, because the API that your tool provide may differ from what the
      |plugin expects.""".stripMargin.replace('\n', ' ')
}
