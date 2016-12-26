package intellij.haskell.external.component

import java.io.File

import com.intellij.openapi.project.Project
import intellij.haskell.HaskellNotificationGroup
import intellij.haskell.external.commandLine.{CommandLine, StackCommandLine}

import scala.io.Source

object PathComponent {
  private final val PackagePattern = """.* (.*) [==|installed].*""".r
  private final val ResolverPattern = """.*/(lts-\d+\.\d+)/.*\n""".r

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
      downloadCabalConfig(project).flatMap(b => {
        if (b) {
          parseCabalConfigFile(configFilePath)
        } else {
          Some(List())
        }
      })
    } else {
      parseCabalConfigFile(configFilePath)
    }
  }

  private def parseCabalConfigFile(configFilePath: String): Option[Iterable[String]] = {
    try {
      Some(Source.fromFile(configFilePath).getLines().filter(!_.startsWith("--")).map {
        case PackagePattern(packageName) => packageName
        case _ => ""
      }.filter(!_.isEmpty).toList)
    } catch {
      case _: Exception => None
    }
  }

  def downloadCabalConfig(project: Project): Option[Boolean] = {
    getResolver(project).map(resolver => {
      val url = s"https://www.stackage.org/$resolver/cabal.config"
      val stdErr = CommandLine.runProgram(Some(project), getIdeaPath(project), "wget", Seq("--no-check-certificate", url), 10000, captureOutputToLog = true, logErrorAsInfo = true).map(_.getStderr)

      if (stdErr.exists(_.contains("ERROR"))) {
        HaskellNotificationGroup.logErrorBalloonEvent(project, s"Can not download cabal config file for stack resolver $resolver.")
        false
      } else {
        true
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

  private def getResolver(project: Project): Option[String] = {
    StackCommandLine.runCommand(Seq("path", "--local-pkg-db"), project).flatMap(output => {
      output.getStdout match {
        case ResolverPattern(resolver) => Some(resolver)
        case _ => None
      }
    })
  }
}
