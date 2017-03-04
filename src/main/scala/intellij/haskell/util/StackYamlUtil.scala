package intellij.haskell.util

import java.io.{File, FileInputStream, FileNotFoundException}

import com.intellij.openapi.project.Project
import intellij.haskell.HaskellNotificationGroup
import org.yaml.snakeyaml.Yaml

object StackYamlUtil {
  private def getYamlFilePath(project: Project): String = {
    project.getBasePath + File.separator + "stack.yaml"
  }

  def getResolverFromStackYamlFile(project: Project): Option[String] = {
    try {
      Option(new Yaml()
        .load(new FileInputStream(new File(getYamlFilePath(project))))
        .asInstanceOf[java.util.Map[String, Any]]
        .get("resolver"))
        .map(_.asInstanceOf[String])
    } catch {
      case _: FileNotFoundException =>
        HaskellNotificationGroup.logErrorEvent(project, s"Can not find `stack.yaml` file.")
        None
    }
  }
}
