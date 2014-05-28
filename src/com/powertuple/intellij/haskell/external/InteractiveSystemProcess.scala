package com.powertuple.intellij.haskell.external

import scala.sys.process._
import scala.io._
import java.io._
import scala.collection.mutable.ListBuffer
import java.io.File
import scala.concurrent.{ExecutionContext, Await, Future}
import java.util.concurrent.Executors
import scala.concurrent.duration._

class InteractiveSystemProcess(val command: String, val workingDir: String) {

  private implicit val ec = new ExecutionContext {
    val threadPool = Executors.newSingleThreadExecutor

    def execute(runnable: Runnable) {
      threadPool.submit(runnable)
    }

    def reportFailure(t: Throwable) {}
  }

  private[this] var inputStream: OutputStream = _
  private[this] val stdOutListBuffer = ListBuffer[String]()
  private[this] val stdErrListBuffer = ListBuffer[String]()

  private[this] val process = Process(command, new File(workingDir)).run(
    new ProcessIO(
      stdin => inputStream = stdin,
      stdout => Source.fromInputStream(stdout).getLines.foreach(stdOutListBuffer.+=),
      stderr => Source.fromInputStream(stderr).getLines.foreach(stdErrListBuffer.+=)
    ))

  def execute(command: String): InteractiveSystemProcessOutput = synchronized {
    stdOutListBuffer.clear()
    stdErrListBuffer.clear()

    inputStream.write((command + "\n").getBytes)
    inputStream.flush()

    val waitForOutput = Future {
      while (stdOutListBuffer.lastOption != Some("OK")) {
        Thread.sleep(5)
      }
    }
    Await.result(waitForOutput, 2.second)

    val outputLinesWithoutOk = stdOutListBuffer.filter(_ != "OK")
    InteractiveSystemProcessOutput(outputLinesWithoutOk, stdErrListBuffer)
  }

  def exit() {
    inputStream.close()
  }
}

case class InteractiveSystemProcessOutput(outputLines: Seq[String], errorLines: Seq[String])
