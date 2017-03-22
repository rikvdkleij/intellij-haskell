package intellij.haskell.runconfig.action

import java.awt.BorderLayout
import javax.swing.{JComponent, JPanel}

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.{DialogWrapper, ValidationInfo}
import com.intellij.ui.TextFieldWithHistory
import com.intellij.util.execution.ParametersListUtil

import scala.collection.JavaConverters._

object RunStackCommandDialog {
  private val SEPARATOR = "%%%%"
  private val KEY = "RunStackCommandDialog.history"
}

class RunStackCommandDialog(private val project: Project)
  extends DialogWrapper(project, false) {

  private val commandField = new TextFieldWithHistory()

  init()
  commandField.setHistory(readHistory().asJava)
  setTitle("Run Stack Command")

  override def createCenterPanel(): JComponent = {
    val panel = new JPanel(new BorderLayout())
    panel.add(commandField, BorderLayout.CENTER)
    panel
  }

  override def getPreferredFocusedComponent: JComponent = commandField

  override def doValidate(): ValidationInfo = {
    if (commandField.getText.isEmpty) {
      new ValidationInfo("Specify command", commandField)
    } else {
      null
    }
  }

  def getStackCommandLine: List[String] = {
    commandField.addCurrentTextToHistory()
    writeHistory(commandField.getHistory.asScala.toList)
    ParametersListUtil.parse(commandField.getText).asScala.toList
  }

  private def readHistory(): List[String] =
    PropertiesComponent.getInstance(project)
      .getValue(RunStackCommandDialog.KEY, "")
      .split(RunStackCommandDialog.SEPARATOR)
      .toList

  private def writeHistory(history: List[String]) {
    PropertiesComponent.getInstance(project)
      .setValue(RunStackCommandDialog.KEY, history.mkString(RunStackCommandDialog.SEPARATOR))
  }

}
