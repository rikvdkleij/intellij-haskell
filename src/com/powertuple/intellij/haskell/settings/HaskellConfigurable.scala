package com.powertuple.intellij.haskell.settings

import com.intellij.openapi.options.{ConfigurationException, Configurable}
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import javax.swing._
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import java.awt.{Insets, GridBagConstraints, GridBagLayout}
import javax.swing.event.DocumentEvent
import com.intellij.ui.DocumentAdapter
import com.powertuple.intellij.haskell.external.SystemProcessContainer

class HaskellConfigurable extends Configurable {
  private var isModifiedByUser = false
  private val ghcModPathField = new TextFieldWithBrowseButton
  private val ghcModiPathField = new TextFieldWithBrowseButton
  private val hdocsPathField = new TextFieldWithBrowseButton

  override def getDisplayName: String = {
    "Haskell"
  }

  override def isModified: Boolean = this.isModifiedByUser

  private val GhcMod = "ghc-mod"
  private val GhcModi = "ghc-modi"
  private val Hdocs = "hdocs"

  override def createComponent: JComponent = {

    ghcModPathField.addBrowseFolderListener(
      s"Select $GhcMod",
      null,
      null,
      FileChooserDescriptorFactory.createSingleLocalFileDescriptor())

    ghcModiPathField.addBrowseFolderListener(
      s"Select $GhcModi",
      null,
      null,
      FileChooserDescriptorFactory.createSingleLocalFileDescriptor())

    hdocsPathField.addBrowseFolderListener(
      s"Select $Hdocs",
      null,
      null,
      FileChooserDescriptorFactory.createSingleFolderDescriptor())

    val settingsPanel = new JPanel(new GridBagLayout())

    val listener: DocumentAdapter = new DocumentAdapter() {
      override def textChanged(e: DocumentEvent) {
        isModifiedByUser = true
      }
    }

    ghcModPathField.getTextField.getDocument.addDocumentListener(listener)
    hdocsPathField.getTextField.getDocument.addDocumentListener(listener)
    ghcModiPathField.getTextField.getDocument.addDocumentListener(listener)

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
    addLabeledControl(2, GhcModi, ghcModiPathField)
    addLabeledControl(1, Hdocs, hdocsPathField)


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
    state.ghcModiPath = ghcModiPathField.getText
    state.hdocsPath = hdocsPathField.getText

    SystemProcessContainer.setReconnect()

    isModifiedByUser = false
  }

  private def validatePaths() {
    def validate(command: String, path: String) = {
      if (!path.endsWith(command) && !path.trim.isEmpty) {
        throw new ConfigurationException(s"Invalid path to $command")
      }
    }
    Seq((GhcMod, ghcModPathField.getText), (GhcModi, ghcModiPathField.getText), (Hdocs, hdocsPathField.getText)).foreach({ case (c, p) => validate(c, p)})
  }

  override def disposeUIResources() {
  }

  override def getHelpTopic: String = ""

  override def reset() {
    val state = HaskellSettings.getInstance().getState
    ghcModPathField.getTextField.setText(state.ghcModPath)
    ghcModiPathField.getTextField.setText(state.ghcModiPath)
    hdocsPathField.getTextField.setText(state.hdocsPath)

    isModifiedByUser = false
  }
}