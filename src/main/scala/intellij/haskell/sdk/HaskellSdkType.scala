/*
 * Copyright 2014-2018 Rik van der Kleij
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package intellij.haskell.sdk

import java.io.File

import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots._
import com.intellij.openapi.projectRoots.impl.SdkConfigurationUtil
import com.intellij.openapi.roots.OrderRootType
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.vfs.VirtualFile
import intellij.haskell.external.execution.CommandLine
import intellij.haskell.util.{HaskellFileUtil, HaskellProjectUtil}
import intellij.haskell.{HaskellIcons, HaskellNotificationGroup}
import javax.swing.Icon
import org.jdom.Element

class HaskellSdkType extends SdkType("Haskell Tool Stack SDK") {

  override def suggestHomePath(): String = {
    if (SystemInfo.isLinux)
      "/usr/bin/stack"
    else if (SystemInfo.isMac)
      "/usr/local/bin/stack"
    else null
  }

  override def suggestSdkName(currentSdkName: String, sdkHome: String): String = "Haskell Tool Stack"

  override def createAdditionalDataConfigurable(sdkModel: SdkModel, sdkModificator: SdkModificator): AdditionalDataConfigurable = null

  override def isValidSdkHome(path: String): Boolean = {
    val stackPath = new File(path)
    !stackPath.isDirectory && stackPath.getName.toLowerCase.contains("stack") && HaskellSdkType.getNumericVersion(path).isDefined
  }

  override def getPresentableName: String = "Stack binary"

  override def saveAdditionalData(additionalData: SdkAdditionalData, additional: Element): Unit = {}

  override def getIcon: Icon = HaskellIcons.HaskellLogo

  override def getIconForAddAction: Icon = getIcon

  override def isRootTypeApplicable(`type`: OrderRootType): Boolean = false

  override def setupSdkPaths(sdk: Sdk): Unit = {}

  override def getVersionString(sdkHome: String): String = {
    if (isValidSdkHome(sdkHome)) {
      HaskellSdkType.getNumericVersion(sdkHome).getOrElse("-")
    } else {
      "-"
    }
  }

  override def getHomeChooserDescriptor: FileChooserDescriptor = {
    val descriptor: FileChooserDescriptor = new FileChooserDescriptor(true, false, false, false, false, false) {

      override def validateSelectedFiles(files: Array[VirtualFile]) {
        if (files.length != 0) {
          val selectedPath = HaskellFileUtil.getAbsolutePath(files(0))
          var pathValid = isValidSdkHome(selectedPath)
          if (!pathValid) {
            pathValid = isValidSdkHome(adjustSelectedSdkHome(selectedPath))
            if (!pathValid) {
              val message = "The selected file is not a valid Stack binary"
              throw new Exception(message)
            }
          }
          val version = HaskellSdkType.getNumericVersion(selectedPath)
          if (version.isEmpty || version.exists(_ < "1.7.0")) {
            val message = "Stack version should be > 1.7.0"
            throw new Exception(message)
          }
        }
      }
    }
    descriptor.setTitle("Select path to " + getPresentableName)
    descriptor
  }
}

object HaskellSdkType {
  def getInstance: HaskellSdkType = SdkType.findInstance(classOf[HaskellSdkType])

  def findOrCreateSdk(): Sdk = {
    SdkConfigurationUtil.findOrCreateSdk(null, getInstance)
  }

  def getNumericVersion(stackPath: String): Option[String] = {
    val workDir = new File(stackPath).getParent
    val output = CommandLine.run(
      None,
      workDir,
      stackPath,
      Seq("--numeric-version"),
      notifyBalloonError = true
    )

    if (output.getExitCode == 0) {
      Some(output.getStdout)
    } else {
      None
    }
  }

  def getStackPath(project: Project, notifyNoSdk: Boolean = true): Option[String] = {
    val projectRootManager = HaskellProjectUtil.getProjectRootManager(project)
    val stackPath = for {
      pm <- projectRootManager
      sdk <- Option(pm.getProjectSdk)
      p <- Option(sdk.getHomePath)
    } yield p

    stackPath match {
      case Some(_) => stackPath
      case None =>
        if (notifyNoSdk) {
          HaskellNotificationGroup.logErrorBalloonEvent(project, "Path to Haskell Stack binary is not configured in Project SDK setting.")
        }
        None
    }
  }

  def getSdkName(project: Project): Option[String] = {
    HaskellProjectUtil.getProjectRootManager(project).map(_.getProjectSdk).map(_.getName)
  }
}