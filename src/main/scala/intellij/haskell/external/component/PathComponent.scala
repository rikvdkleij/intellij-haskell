package intellij.haskell.external.component

import java.io.File
import java.net.URL

import com.intellij.openapi.project.Project
import com.intellij.util.io.URLUtil
import intellij.haskell.HaskellNotificationGroup
import intellij.haskell.external.commandLine.StackCommandLine
import org.apache.commons.io.FileUtils

import scala.io.Source

object PathComponent {
  private final val PackageNamePattern = """.* (.*) [==|installed].*""".r
  private final val LocalPkgdbResolverPattern = """.*/(lts-\d+\.\d+)/.*\n""".r
  private final val CabalConfigFileResolverPattern = """.*/(lts-\d+\.\d+)""".r

  def getIdeaPath(project: Project): String = {
    project.getBasePath + File.separator + ".idea"
  }

  def getConfigFilePath(project: Project): String = {
    getIdeaPath(project) + File.separator + "cabal.config"
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
    downloadCabalConfig(project).flatMap(b => {
      if (b) {
        parseCabalConfigFile(project)
      } else {
        Some(List())
      }
    })
  }

  private def parseCabalConfigFile(project: Project): Option[Iterable[String]] = {
    val configFilePath = getConfigFilePath(project)
    try {
      Some(Source.fromFile(configFilePath).getLines().filter(!_.startsWith("--")).map {
        case PackageNamePattern(packageName) => packageName
        case _ => ""
      }.filter(!_.isEmpty).toList)
    } catch {
      case _: Exception => None
    }
  }

  def downloadCabalConfig(project: Project): Option[Boolean] = {
    getResolverFromLocalPkgdb(project).map(resolver => {
      val url = new URL(s"https://www.stackage.org/$resolver/cabal.config")
      val configFilePath = getConfigFilePath(project)
      val targetFile = new File(configFilePath)

      try {
        val in = URLUtil.openStream(url)
        FileUtils.copyInputStreamToFile(in, targetFile)
        true
      } catch {
        case _: Exception =>
          HaskellNotificationGroup.logErrorBalloonEvent(project, s"Can not download cabal config file for stack resolver <b>$resolver</b>, please check your network environment.")
          false
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
        case LocalPkgdbResolverPattern(resolver) => Some(resolver)
        case _ => None
      }
    })
  }

  private def getResolverFromCabalConfigFile(project: Project): Option[String] = {
    try {
      Source.fromFile(getConfigFilePath(project)).getLines().toList.headOption.flatMap(l => {
        l match {
          case CabalConfigFileResolverPattern(resolver) => Some(resolver)
          case _ => None
        }
      })
    } catch {
      case _: Exception => None
    }
  }

}
