package intellij.haskell

import java.io.File

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtil
import intellij.haskell.util.HaskellFileUtil
import io.github.soc.directories.ProjectDirectories

object GlobalInfo {

  final val LibrarySourcedDirName = "lib"
  final val StackWorkDirName = ".stack-work"
  final val StackageLtsVersion = "lts-12"
  private final val ToolsBinDirName = "bin"

  private final val IntelliJHaskellDirectories = ProjectDirectories.from("com.github", "rikvdkleij", "intellij-haskell")

  def getIntelliJHaskellDirectory: File = {
    val directory = new File(IntelliJHaskellDirectories.cacheDir)
    if (directory.exists()) {
      HaskellFileUtil.removeGroupWritePermission(directory)
    } else {
      HaskellFileUtil.createDirectoryIfNotExists(directory, onlyWriteableByOwner = true)
    }
    directory
  }

  def getLibrarySourcesPath: File = {
    new File(getIntelliJHaskellDirectory, GlobalInfo.LibrarySourcedDirName)
  }

  def toolsStackRootPath: File = {
    new File(getIntelliJHaskellDirectory, StackageLtsVersion)
  }

  def toolsBinPath: File = {
    new File(toolsStackRootPath, ToolsBinDirName)
  }

  def toolPath(toolName: String): File = {
    new File(toolsBinPath, toolName)
  }

  def getIntelliJProjectDirectory(project: Project): File = {
    val intelliJProjectDirectory = new File(GlobalInfo.getIntelliJHaskellDirectory, project.getName)
    if (!intelliJProjectDirectory.exists()) {
      FileUtil.createDirectory(intelliJProjectDirectory)
    }
    intelliJProjectDirectory
  }
}
