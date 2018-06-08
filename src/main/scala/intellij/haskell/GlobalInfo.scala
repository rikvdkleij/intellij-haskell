package intellij.haskell

import java.io.File
import java.nio.file.attribute.PosixFilePermission
import java.nio.file.{Files, Paths}

import com.intellij.openapi.vfs.VfsUtil
import intellij.haskell.util.HaskellFileUtil

object GlobalInfo {

  final val LibrarySourcedDirName = "lib"
  final val StackWorkDirName = ".stack-work"
  final val StackageLtsVersion = "lts-11"
  private final val ToolsBinDirName = "bin"

  private final val IntelliJHaskellDirName = ".intellij-haskell"

  def getIntelliJHaskellDirectory: File = {
    val homeDirectory = HaskellFileUtil.getAbsolutePath(VfsUtil.getUserHomeDir)
    val directory = new File(homeDirectory, IntelliJHaskellDirName)
    if (directory.exists()) {
      val directoryPath = Paths.get(directory.getAbsolutePath)
      val permissions = Files.getPosixFilePermissions(directoryPath)
      if (permissions.contains(PosixFilePermission.GROUP_WRITE)) {
        permissions.remove(PosixFilePermission.GROUP_WRITE)
        Files.setPosixFilePermissions(directoryPath, permissions)
      }
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

  def toolPath(toolName: String): String = {
    Paths.get(GlobalInfo.toolsBinPath, toolName).toString
  }

}
