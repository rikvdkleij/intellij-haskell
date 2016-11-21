/*
 * Copyright 2016 Rik van der Kleij
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
import javax.swing.Icon

import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots._
import com.intellij.openapi.projectRoots.impl.SdkConfigurationUtil
import com.intellij.openapi.roots.{OrderRootType, ProjectRootManager}
import com.intellij.openapi.vfs.VirtualFile
import intellij.haskell.HaskellIcons
import intellij.haskell.external.commandLine.CommandLine
import org.jdom.Element

class HaskellSdkType extends SdkType("Haskell Tool Stack SDK") {

  override def suggestHomePath(): String = "/usr/bin"

  override def suggestSdkName(currentSdkName: String, sdkHome: String): String = "Haskell Tool Stack"

  override def createAdditionalDataConfigurable(sdkModel: SdkModel, sdkModificator: SdkModificator): AdditionalDataConfigurable = null

  override def isValidSdkHome(path: String): Boolean = {
    val stackPath = new File(path)
    stackPath.isFile && path.toLowerCase.contains("stack") && HaskellSdkType.getNumericVersion(path).isDefined
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
      @throws[Exception]
      override def validateSelectedFiles(files: Array[VirtualFile]) {
        if (files.length != 0) {
          val selectedPath: String = files(0).getPath
          var valid = isValidSdkHome(selectedPath)
          if (!valid) {
            valid = isValidSdkHome(adjustSelectedSdkHome(selectedPath))
            if (!valid) {
              val message = "The selected file is not a valid Stack binary"
              throw new Exception(message)
            }
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

  def getSdkHomePath(project: Project): Option[String] = {
    Option(ProjectRootManager.getInstance(project).getProjectSdk).map(_.getHomePath)
  }

  def findOrCreateSdk(): Sdk = {
    SdkConfigurationUtil.findOrCreateSdk(null, getInstance)
  }

  def getNumericVersion(sdkHome: String) = {
    val workDir = new File(sdkHome).getParent
    CommandLine.runCommand(
      workDir,
      sdkHome,
      Seq("--numeric-version")
    ).map(_.getStdout)
  }

  def getStackPath(project: Project): String = {
    val sdkHomePath = getSdkHomePath(project)
    sdkHomePath.getOrElse(throw new IllegalStateException("Path to Haskell Stack binary expected to be set in Project SDK setting"))
  }
}