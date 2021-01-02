package intellij.haskell.ui

import java.awt.BorderLayout

import com.intellij.openapi.ui.DialogWrapper
import javax.swing.{JComponent, JLabel, JPanel, JTextField}

class EnterNameDialog(prompt: String, suggestion: String = "") extends DialogWrapper(true) {
  private val textField = if (suggestion.isEmpty) new JTextField(10) else new JTextField(suggestion)
  init()
  setTitle(prompt)
  override def createCenterPanel(): JComponent = {
    val dialogPanel: JPanel = new JPanel(new BorderLayout)

    val label: JLabel = new JLabel(prompt)
    dialogPanel.add(label, BorderLayout.NORTH)

    dialogPanel.add(textField, BorderLayout.SOUTH)

    dialogPanel
  }

  override def getPreferredFocusedComponent: JComponent = textField

  def getName: String = textField.getText

}
