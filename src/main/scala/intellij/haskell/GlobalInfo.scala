package intellij.haskell

import java.io.File
import java.nio.file.Paths

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
    new File(homeDirectory, IntelliJHaskellDirName)
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
