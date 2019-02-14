package intellij.haskell.stackyaml

import java.io.{File, FileInputStream, FileNotFoundException}
import java.util

import com.intellij.openapi.project.Project
import intellij.haskell.HaskellNotificationGroup
import org.yaml.snakeyaml.Yaml

import scala.collection.JavaConverters._

object StackYamlComponent {

  def isNixEnabled(project: Project): Boolean = {
    {
      for {
        items <- getYamlItems(project)
        nix <- items.get("nix").flatMap(Option(_)).map(_.asInstanceOf[java.util.LinkedHashMap[String, Any]].asScala.toMap)
        enabled <- nix.get("enable").flatMap(Option(_))
      } yield {
        enabled match {
          case b: Boolean if b => b
          case _ => false
        }
      }
    }.contains(true)
  }

  def getResolver(project: Project): Option[String] = {
    getYamlItems(project).flatMap(_.get("resolver")).map(_.asInstanceOf[String])
  }

  def getPackagePaths(project: Project): Option[Seq[String]] = {
    for {
      items <- getYamlItems(project)
      packages <- getPackages(project, items)
    } yield {
      packages match {
        case p: util.ArrayList[_] =>
          p.asScala.flatMap {
            case s: String if isNotURL(s) => Seq(s)
            case m: util.Map[_, _] =>
              val map = m.asInstanceOf[util.Map[String, Any]].asScala.toMap
              val location = getLocation(project, map)
              if (location.isDefined) {
                getSubdirs(project, map).getOrElse(location.toSeq)
              } else {
                Seq()
              }
            case _ => Seq()
          }
        case _ => Seq()
      }
    }
  }

  private def getPackages(project: Project, items: Map[String, Any]): Option[Any] = {
    items.get("packages") match {
      case Some(p) => Some(p)
      case _ =>
        HaskellNotificationGroup.logErrorEvent(project, s"Could not find `packages` in `stack.yaml` file in project directory")
        None
    }
  }

  private def isNotURL(s: String) = {
    !(s.startsWith("http://") || s.startsWith("https://"))
  }

  private def getLocation(project: Project, items: Map[String, Any]): Option[String] = {
    items.get("location") match {
      case Some(l: String) if isNotURL(l) => Some(l)
      case _ =>
        HaskellNotificationGroup.logErrorEvent(project, s"Only local paths are supported in `location` of `packages` in `stack.yaml`")
        None
    }
  }

  private def getSubdirs(project: Project, items: Map[String, Any]): Option[Seq[String]] = {
    items.get("subdirs") match {
      case Some(sd: util.ArrayList[_]) => Some(sd.asInstanceOf[util.ArrayList[String]].asScala)
      case _ => None
    }
  }

  private def getYamlItems(project: Project): Option[Map[String, Any]] = {
    try {
      Option(new Yaml()
        .load(new FileInputStream(new File(getYamlFilePath(project))))
        .asInstanceOf[util.Map[String, Any]].asScala.toMap)
    } catch {
      case _: FileNotFoundException =>
        HaskellNotificationGroup.logErrorEvent(project, s"Could not find `stack.yaml` file in project directory")
        None
    }
  }

  private def getYamlFilePath(project: Project): String = {
    project.getBasePath + File.separator + "stack.yaml"
  }
}
