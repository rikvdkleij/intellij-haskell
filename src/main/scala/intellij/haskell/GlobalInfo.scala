package intellij.haskell

import com.intellij.application.options.PathMacrosImpl
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.util.io.FileUtil
import intellij.haskell.settings.HaskellSettingsState
import intellij.haskell.util.HaskellFileUtil
import io.github.soc.directories.ProjectDirectories

import java.io.File

object GlobalInfo {

  final val LibrarySourcedDirName = "lib"
  final val StackWorkDirName = ".stack-work"
  final val StackageLtsVersion = "lts-16"
  private final val ToolsBinDirName = "bin"

  final lazy val DefaultCachePath = {
    // Workaround https://github.com/rikvdkleij/intellij-haskell/issues/503
    if (SystemInfo.isWindows) {
      System.setProperty("jdk.lang.Process.allowAmbiguousCommands", "true")
    }
    ProjectDirectories.from("com.github", "rikvdkleij", "intellij-haskell").cacheDir
  }

  lazy val getIntelliJHaskellDirectory: File = {
    val directory = new File(HaskellSettingsState.getCachePath)
    if (directory.exists()) {
      HaskellFileUtil.removeGroupWritePermission(directory)
    } else {
      HaskellFileUtil.createDirectoryIfNotExists(directory, onlyWriteableByOwner = true)
    }
    directory
  }

  lazy val getLibrarySourcesPath: File = {
    new File(getIntelliJHaskellDirectory, GlobalInfo.LibrarySourcedDirName)
  }

  lazy val toolsStackRootPath: File = {
    new File(getIntelliJHaskellDirectory, StackageLtsVersion)
  }

  lazy val toolsBinPath: File = {
    new File(toolsStackRootPath, ToolsBinDirName)
  }

  def toolPath(tool: HTool): File = {
    val name = if (SystemInfo.isWindows) tool.name + ".exe" else tool.name
    new File(toolsBinPath, name)
  }

  lazy val defaultHlintPath: File = {
    toolPath(HTool.Hlint)
  }

  lazy val defaultHooglePath: File = {
    toolPath(HTool.Hoogle)
  }

  lazy val defaultStylishHaskellPath: File = {
    toolPath(HTool.StylishHaskell)
  }

  lazy val defaultOrmoluPath: File = {
    toolPath(HTool.Ormolu)
  }

  def getIntelliJProjectDirectory(project: Project): File = {
    val intelliJProjectDirectory = new File(GlobalInfo.getIntelliJHaskellDirectory, project.getName)
    synchronized {
      if (!intelliJProjectDirectory.exists()) {
        FileUtil.createDirectory(intelliJProjectDirectory)
      }
    }
    intelliJProjectDirectory
  }

  def pathVariables: java.util.Map[String, String] = {
    PathMacrosImpl.getInstanceEx.getUserMacros
  }
}
