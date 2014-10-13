package com.powertuple.intellij.haskell.util

object OSUtil {

  private final val OsName = System.getProperty("os.name").toLowerCase

  def isOSX: Boolean = {
    OsName.indexOf("mac") >= 0 || OsName.indexOf("darwin") >= 0
  }
}
