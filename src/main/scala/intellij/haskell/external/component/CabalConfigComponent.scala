package intellij.haskell.external.component

import java.io.File
import java.net.URL

import com.intellij.openapi.project.Project
import com.intellij.util.io.URLUtil
import intellij.haskell.stackyaml.StackYamlComponent
import intellij.haskell.util.HaskellFileUtil
import intellij.haskell.{GlobalInfo, HaskellNotificationGroup}

import scala.io.Source

object CabalConfigComponent {
  private final val PackageNamePattern = """.* (.*) [==|installed].*""".r
  private final val LtsResolverPattern = """.*/(lts-\d+\.\d+)""".r
  private final val NightlyResolverPattern = """.*/(nightly-\d{4}-\d{2}-\d{2})""".r

  def getAvailablePackageNames(project: Project): Iterable[String] = {
    val cabalConfigFile = getCabalConfigFile(project)

    StackYamlComponent.getResolver(project).map(resolver => {
      if (resolver.startsWith("lts") || resolver.startsWith("nightly")) {
        if (!cabalConfigFile.exists()) {
          downloadAndParseCabalConfigFile(project)
        } else {
          val needUpdate = for {
            oldResolver <- getResolverFromCabalConfigFile(project, cabalConfigFile)
          } yield oldResolver != resolver

          needUpdate match {
            case Some(b) =>
              if (b) {
                removeCabalConfig(project)
                downloadAndParseCabalConfigFile(project)
              } else {
                parseCabalConfigFile(project, cabalConfigFile)
              }
            case None => parseDefaultCabalConfigFile()
          }
        }
      } else {
        parseDefaultCabalConfigFile()
      }
    }).getOrElse(parseDefaultCabalConfigFile())
  }

  private def getCabalConfigFilePath(project: Project): String = {
    GlobalInfo.getIntelliJProjectDirectory(project) + File.separator + "cabal.config"
  }

  private def getCabalConfigFile(project: Project): File = {
    new File(getCabalConfigFilePath(project))
  }

  private def downloadAndParseCabalConfigFile(project: Project): Iterable[String] = {
    downloadCabalConfig(project)
    val cabalConfigFile = getCabalConfigFile(project)
    if (cabalConfigFile.exists()) {
      parseCabalConfigFile(project, cabalConfigFile)
    } else {
      parseDefaultCabalConfigFile()
    }
  }

  private def parseCabalConfigLine(line: String): Option[String] = {
    line match {
      case PackageNamePattern(packageName) => Some(packageName)
      case _ => None
    }
  }

  private def parseDefaultCabalConfigFile(): Seq[String] = {
    val url = getClass.getResource("/cabal/cabal.config")
    val source = Source.fromURL(url)
    try {
      source.getLines.flatMap(parseCabalConfigLine).toList
    } finally {
      source.close()
    }
  }

  private def parseCabalConfigFile(project: Project, cabalConfigFile: File): Iterable[String] = {
    readCabalConfigFile(project, cabalConfigFile)
  }

  private def readCabalConfigFile(project: Project, cabalConfigFile: File): Seq[String] = {
    val bufferedSource = Source.fromFile(cabalConfigFile)
    try {
      bufferedSource.getLines.flatMap(parseCabalConfigLine).toList
    } catch {
      case _: Exception => Seq()
    } finally {
      bufferedSource.close()
    }
  }

  private def downloadCabalConfig(project: Project): Unit = {
    def logError(resolver: String): Unit = {
      HaskellNotificationGroup.logErrorBalloonEvent(project, s"Can not download cabal config file for stack resolver <b>$resolver</b>, please check your network environment. Falling back to default Cabal.config")
    }

    StackYamlComponent.getResolver(project).foreach(resolver => {
      val url = new URL(s"https://www.stackage.org/$resolver/cabal.config")
      val targetFile = getCabalConfigFile(project)

      try {
        val inputStream = URLUtil.openStream(url)
        try {
          HaskellFileUtil.copyStreamToFile(inputStream, targetFile)
        } catch {
          case _: Exception => logError(resolver)
        } finally {
          inputStream.close()
        }
      } catch {
        case _: Exception => logError(resolver)
      }
    })
  }

  private def removeCabalConfig(project: Project): Unit = {
    val configFile = getCabalConfigFile(project)
    if (configFile.exists()) {
      configFile.delete()
    }
  }

  private def getResolverFromCabalConfigFile(project: Project, cabalConfigFile: File): Option[String] = {
    readCabalConfigFile(project, cabalConfigFile).headOption.flatMap(l => {
      l match {
        case LtsResolverPattern(resolver) => Some(resolver)
        case NightlyResolverPattern(resolver) => Some(resolver)
        case _ => None
      }
    })
  }

}
