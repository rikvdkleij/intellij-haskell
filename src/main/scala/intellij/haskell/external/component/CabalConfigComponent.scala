package intellij.haskell.external.component

import java.io.{File, FileNotFoundException}
import java.net.URL

import com.intellij.openapi.project.Project
import com.intellij.util.io.URLUtil
import intellij.haskell.HaskellNotificationGroup
import intellij.haskell.stackyaml.StackYamlComponent
import intellij.haskell.util.HaskellFileUtil

import scala.collection.Iterator
import scala.io.Source

object CabalConfigComponent {
  private final val PackageNamePattern = """.* (.*) [==|installed].*""".r
  private final val LtsResolverPattern = """.*/(lts-\d+\.\d+)""".r
  private final val NightlyResolverPattern = """.*/(nightly-\d{4}-\d{2}-\d{2})""".r

  private def getCabalConfigFilePath(project: Project): String = {
    project.getBasePath + File.separator + "cabal.config"
  }

  def getAvailablePackageNames(project: Project): Iterable[String] = {
    val cabalConfigFilePath = getCabalConfigFilePath(project)
    val cabalConfigFile = new File(cabalConfigFilePath)

    StackYamlComponent.getResolver(project).map(resolver => {
      if (resolver.startsWith("lts") || resolver.startsWith("nightly")) {
        if (!cabalConfigFile.exists()) {
          downloadAndParseCabalConfigFile(project)
        } else {
          val needUpdate = for {
            oldResolver <- getResolverFromCabalConfigFile(project)
          } yield oldResolver != resolver

          needUpdate match {
            case Some(b) =>
              if (b) {
                removeCabalConfig(project)
                downloadAndParseCabalConfigFile(project)
              } else {
                parseCabalConfigFile(project)
              }
            case None => parseDefaultCabalConfigFile(project)
          }
        }
      } else {
        parseDefaultCabalConfigFile(project)
      }
    }).getOrElse(parseDefaultCabalConfigFile(project))
  }

  private def downloadAndParseCabalConfigFile(project: Project): Iterable[String] = {
    downloadCabalConfig(project)
    parseCabalConfigFile(project)
  }

  private def parseCabalConfigFileBase(project: Project, lines: Iterator[String]): Iterable[String] = {
    lines.filter(!_.startsWith("--")).map {
      case PackageNamePattern(packageName) => packageName
      case _ => ""
    }.filterNot(_.isEmpty).toList
  }

  private def parseDefaultCabalConfigFile(project: Project): Iterable[String] = {
    parseCabalConfigFileBase(project, Source.fromURL(getClass.getResource("/cabal/cabal.config")).getLines())
  }

  private def parseCabalConfigFile(project: Project): Iterable[String] = {
    parseCabalConfigFileBase(project, Source.fromFile(getCabalConfigFilePath(project)).getLines())
  }

  def downloadCabalConfig(project: Project): Unit = {
    StackYamlComponent.getResolver(project).foreach(resolver => {
      val url = new URL(s"https://www.stackage.org/$resolver/cabal.config")
      val configFilePath = getCabalConfigFilePath(project)
      val targetFile = new File(configFilePath)

      try {
        val in = URLUtil.openStream(url)
        HaskellFileUtil.copyStreamToFile(in, targetFile)
      } catch {
        case _: Exception =>
          HaskellNotificationGroup.logErrorBalloonEvent(project, s"Can not download cabal config file for stack resolver <b>$resolver</b>, please check your network environment.")
      }
    })
  }

  def removeCabalConfig(project: Project): Unit = {
    val configFilePath = getCabalConfigFilePath(project)
    val configFile = new File(configFilePath)
    if (configFile.exists()) {
      configFile.delete()
    }
  }

  private def getResolverFromCabalConfigFile(project: Project): Option[String] = {
    try {
      Source.fromFile(getCabalConfigFilePath(project)).getLines().toList.headOption.flatMap(l => {
        l match {
          case LtsResolverPattern(resolver) => Some(resolver)
          case NightlyResolverPattern(resolver) => Some(resolver)
          case _ => None
        }
      })
    } catch {
      case _: FileNotFoundException =>
        HaskellNotificationGroup.logErrorEvent(project, s"Could not find `cabal.config` file.")
        None
    }
  }

}
