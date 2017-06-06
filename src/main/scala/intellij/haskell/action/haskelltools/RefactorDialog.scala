package intellij.haskell.action.haskelltools

import java.awt.BorderLayout
import javax.swing.{JComponent, JPanel, JTextField}

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.{DialogWrapper, ValidationInfo}

class RefactorDialog(project: Project) extends DialogWrapper(project, false) {

  private val newNameField = new JTextField()

  init()
  setTitle("New Name")

  override def createCenterPanel(): JComponent = {
    val panel = new JPanel(new BorderLayout())
    panel.add(newNameField, BorderLayout.CENTER)
    panel
  }

  override def getPreferredFocusedComponent: JComponent = newNameField

  override def doValidate(): ValidationInfo = {
    if (newNameField.getText.isEmpty) {
      new ValidationInfo("Please give a new name", newNameField)
    } else {
      null
    }
  }

  def getNewName: String = {
    newNameField.getText
  }
}