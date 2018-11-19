package intellij.haskell

import java.io.File
import java.nio.file.{Path, Paths}

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

  def getLibrarySourcesPath: String = {
    Paths.get(getIntelliJHaskellDirectory.getAbsolutePath, GlobalInfo.LibrarySourcedDirName).toString
  }

  def toolsStackRootPath: String = {
    Paths.get(getIntelliJHaskellDirectory.getAbsolutePath, StackageLtsVersion).toString
  }

  def toolsBinPath: String = {
    Paths.get(toolsStackRootPath, ToolsBinDirName).toString
  }

  def toolPath(toolName: String): Path = {
    Paths.get(GlobalInfo.toolsBinPath, toolName)
  }

  def getIntelliJProjectDirectory(project: Project): Path = {
    val intelliJProjectDirectory = Paths.get(GlobalInfo.getIntelliJHaskellDirectory.getAbsolutePath, project.getName)
    val intelliJProjectFile = new File(intelliJProjectDirectory.toString)
    if (!intelliJProjectFile.exists()) {
      FileUtil.createDirectory(intelliJProjectFile)
    }
    intelliJProjectDirectory
  }
}
