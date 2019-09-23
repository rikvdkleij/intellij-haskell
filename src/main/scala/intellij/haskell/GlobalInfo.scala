package intellij.haskell

import java.io.File

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.util.io.FileUtil
import intellij.haskell.util.HaskellFileUtil
import io.github.soc.directories.ProjectDirectories

object GlobalInfo {

  final val LibrarySourcedDirName = "lib"
  final val StackWorkDirName = ".stack-work"
  final val StackageLtsVersion = "lts-14"
  private final val ToolsBinDirName = "bin"

  private final val IntelliJHaskellDirectories = ProjectDirectories.from("com.github", "rikvdkleij", "intellij-haskell")

  lazy val getIntelliJHaskellDirectory: File = {
    val directory = new File(IntelliJHaskellDirectories.cacheDir)
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

  lazy val defaultHindentPath: File = {
    toolPath(HTool.Hindent)
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
}
