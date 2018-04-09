package intellij.haskell

import java.io.File

import com.intellij.openapi.vfs.VfsUtil
import intellij.haskell.util.HaskellFileUtil

object GlobalInfo {

  private final val IntelliJHaskellDirName = ".intellij-haskell"

  def getIntelliJHaskellDirectory: File = {
    val homeDirectory = HaskellFileUtil.getAbsolutePath(VfsUtil.getUserHomeDir)
    new File(homeDirectory, IntelliJHaskellDirName)
  }

}
