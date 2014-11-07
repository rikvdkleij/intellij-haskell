package com.powertuple.intellij.haskell.util

import sun.security.action.GetPropertyAction

object OSUtil {

  private final val OsName = System.getProperty("os.name").toLowerCase

  final val LineSeparator = java.security.AccessController.doPrivileged(new GetPropertyAction("line.separator"))

  def isOSX: Boolean = {
    OsName.indexOf("mac") >= 0 || OsName.indexOf("darwin") >= 0
  }
}
