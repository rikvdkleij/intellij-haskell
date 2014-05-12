package com.powertuple.intellij.haskell.util

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.CapturingProcessHandler
import com.intellij.execution.process.ProcessOutput
import java.io.File
import scala.collection.JavaConversions._

object HaskellSystemUtil {
  val StandardTimeout = 10 * 1000

  def getProcessOutput(workDir: String, exePath: String, arguments: Seq[String], timeout: Int = StandardTimeout): ProcessOutput = {
    if (!new File(workDir).isDirectory || !new File(exePath).canExecute) {
      new ProcessOutput
    }
    val cmd: GeneralCommandLine = new GeneralCommandLine
    cmd.setWorkDirectory(workDir)
    cmd.setExePath(exePath)
    cmd.addParameters(arguments)
    execute(cmd, timeout)
  }

  def execute(cmd: GeneralCommandLine): ProcessOutput = {
    execute(cmd, StandardTimeout)
  }

  def execute(cmd: GeneralCommandLine, timeout: Int): ProcessOutput = {
    val processHandler: CapturingProcessHandler = new CapturingProcessHandler(cmd.createProcess)
    if (timeout < 0) processHandler.runProcess else processHandler.runProcess(timeout)
  }
}