package intellij.haskell.external.component

import java.io.{File, FileNotFoundException}
import java.net.URL

import com.intellij.openapi.project.Project
import com.intellij.util.io.URLUtil
import intellij.haskell.HaskellNotificationGroup
import intellij.haskell.external.commandLine.StackCommandLine
import intellij.haskell.util.HaskellFileUtil

import scala.io.Source

object CabalConfigComponent {
  private final val PackageNamePattern = """.* (.*) [==|installed].*""".r
  private final val LocalLtsPkgdbResolverPattern = """.*/(lts-\d+\.\d+)/.*\n""".r
  private final val LocalNightlyPkgdbResolverPattern = """.*/(nightly-\d{4}-\d{2}-\d{2})/.*\n""".r
  private final val CabalConfigFileLtsResolverPattern = """.*/(lts-\d+\.\d+)""".r
  private final val CabalConfigFileNightlyResolverPattern = """.*/(nightly-\d{4}-\d{2}-\d{2})""".r

  def getConfigFilePath(project: Project): String = {
    project.getBasePath + File.separator + "cabal.config"
  }

  def getAllAvailablePackageNames(project: Project): Option[Iterable[String]] = {
    val configFilePath = getConfigFilePath(project)
    val configFile = new File(configFilePath)
    if (!configFile.exists()) {
      downloadAndParseCabalConfigFile(project)
    } else {
      val needUpdate = for {
        oldResolver <- getResolverFromCabalConfigFile(project)
        newResolver <- getResolverFromLocalPkgdb(project)
      } yield oldResolver != newResolver

      needUpdate.flatMap(b => {
        if (b) {
          removeCabalConfig(project)
          downloadAndParseCabalConfigFile(project)
        } else {
          parseCabalConfigFile(project)
        }
      })
    }
  }

  private def downloadAndParseCabalConfigFile(project: Project): Option[Iterable[String]] = {
    downloadCabalConfig(project)
    parseCabalConfigFile(project)
  }

  private def parseCabalConfigFile(project: Project): Option[Iterable[String]] = {
    val configFilePath = getConfigFilePath(project)
    try {
      Some(Source.fromFile(configFilePath).getLines().filter(!_.startsWith("--")).map {
        case PackageNamePattern(packageName) => packageName
        case _ => ""
      }.filter(!_.isEmpty).toList)
    } catch {
      case _: FileNotFoundException =>
        HaskellNotificationGroup.logErrorEvent(project, s"Can not parse `cabal.config` file.")
        None
    }
  }

  def downloadCabalConfig(project: Project): Unit = {
    getResolverFromLocalPkgdb(project).foreach(resolver => {
      val url = new URL(s"https://www.stackage.org/$resolver/cabal.config")
      val configFilePath = getConfigFilePath(project)
      val targetFile = new File(configFilePath)

      try {
        val in = URLUtil.openStream(url)
        HaskellFileUtil.copyStreamToFile(in, targetFile)
      } catch {
        case _: Exception =>
          HaskellNotificationGroup.logErrorEvent(project, s"Can not download cabal config file for stack resolver `$resolver`.")
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

  private def getResolverFromLocalPkgdb(project: Project): Option[String] = {
    StackCommandLine.runCommand(Seq("path", "--local-pkg-db"), project).flatMap(output => {
      output.getStdout match {
        case LocalLtsPkgdbResolverPattern(resolver) => Some(resolver)
        case LocalNightlyPkgdbResolverPattern(resolver) => Some(resolver)
        case _ => None
      }
    })
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
      case _: Exception => None
    }
  }

}
