package com.powertuple.intellij.haskell.external

import com.powertuple.intellij.haskell.settings.HaskellSettings

object SystemProcessContainer {

  private var reconnect = false

  private var ghcModi: Option[InteractiveSystemProcess] = None

  def getGhcModi(workingDir: String) = {
    ghcModi match {
      case None => setGhcModi(workingDir); ghcModi.get
      case Some(gm) if reconnect => gm.exit(); setGhcModi(workingDir); reconnect = false; ghcModi.get
      case Some(gm) => gm
    }
  }

  def setReconnect() {
    reconnect = true
  }

  private def setGhcModi(workingDir: String) {
    ghcModi = Some(new InteractiveSystemProcess(HaskellSettings.getInstance().getState.ghcModiPath, workingDir))
  }
}
