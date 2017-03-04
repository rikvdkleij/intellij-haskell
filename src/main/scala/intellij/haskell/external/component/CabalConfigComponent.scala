package intellij.haskell.external.component

import java.io.{File, FileNotFoundException}
import java.net.URL

import com.intellij.openapi.project.Project
import com.intellij.util.io.URLUtil
import intellij.haskell.HaskellNotificationGroup
import intellij.haskell.util.{HaskellFileUtil, StackYamlUtil}

import scala.collection.Iterator
import scala.io.Source

object CabalConfigComponent {
  private final val PackageNamePattern = """.* (.*) [==|installed].*""".r
  private final val CabalConfigFileLtsResolverPattern = """.*/(lts-\d+\.\d+)""".r
  private final val CabalConfigFileNightlyResolverPattern = """.*/(nightly-\d{4}-\d{2}-\d{2})""".r

  def getConfigFilePath(project: Project): String = {
    project.getBasePath + File.separator + "cabal.config"
  }

  def getAllAvailablePackageNames(project: Project): Option[Iterable[String]] = {
    val configFilePath = getConfigFilePath(project)
    val configFile = new File(configFilePath)

    StackYamlUtil.getResolverFromStackYamlFile(project).map(resolver => {
      if (resolver.startsWith("lts") || resolver.startsWith("nightly")) {
        if (!configFile.exists()) {
          downloadAndParseCabalConfigFile(project)
        } else {
          val needUpdate = for {
            oldResolver <- getResolverFromCabalConfigFile(project)
            newResolver <- StackYamlUtil.getResolverFromStackYamlFile(project)
          } yield oldResolver != newResolver

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
    }).orElse(Some(parseDefaultCabalConfigFile(project)))
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
    parseCabalConfigFileBase(project, Source.fromFile(getConfigFilePath(project)).getLines())
  }

  def downloadCabalConfig(project: Project): Unit = {
    StackYamlUtil.getResolverFromStackYamlFile(project).foreach(resolver => {
      val url = new URL(s"https://www.stackage.org/$resolver/cabal.config")
      val configFilePath = getConfigFilePath(project)
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
    val configFilePath = getConfigFilePath(project)
    val configFile = new File(configFilePath)
    if (configFile.exists()) {
      configFile.delete()
    }
  }

  private def getResolverFromCabalConfigFile(project: Project): Option[String] = {
    try {
      Source.fromFile(getConfigFilePath(project)).getLines().toList.headOption.flatMap(l => {
        l match {
          case CabalConfigFileLtsResolverPattern(resolver) => Some(resolver)
          case CabalConfigFileNightlyResolverPattern(resolver) => Some(resolver)
          case _ => None
        }
      })
    } catch {
      case _: FileNotFoundException =>
        HaskellNotificationGroup.logErrorEvent(project, s"Can not find `cabal.config` file.")
        None
    }
  }

}
