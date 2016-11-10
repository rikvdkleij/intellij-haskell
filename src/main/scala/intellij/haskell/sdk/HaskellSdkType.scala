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

import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots._
import com.intellij.openapi.projectRoots.impl.SdkConfigurationUtil
import com.intellij.openapi.roots.{OrderRootType, ProjectRootManager}
import com.intellij.openapi.util.io.FileUtil
import intellij.haskell.HaskellIcons
import intellij.haskell.external.commandLine.CommandLine
import org.jdom.Element

class HaskellSdkType extends SdkType("Haskel Tool Stack SDK") {

  override def suggestHomePath(): String = "/usr/bin"

  override def suggestSdkName(currentSdkName: String, sdkHome: String): String = "Haskell Tool Stack"

  override def createAdditionalDataConfigurable(sdkModel: SdkModel, sdkModificator: SdkModificator): AdditionalDataConfigurable = null

  override def isValidSdkHome(path: String): Boolean = {
    val stackPath = new File(path)
    stackPath.isDirectory && stackPath.listFiles.map(f => FileUtil.getNameWithoutExtension(f)).contains("stack")
  }

  override def getPresentableName: String = "Stack binary folder"

  override def saveAdditionalData(additionalData: SdkAdditionalData, additional: Element): Unit = {}

  override def getIcon: Icon = HaskellIcons.HaskellLogo

  override def getIconForAddAction: Icon = getIcon

  override def isRootTypeApplicable(`type`: OrderRootType): Boolean = false

  override def setupSdkPaths(sdk: Sdk): Unit = {}

  override def getVersionString(sdkHome: String): String = HaskellSdkType.getNumericVersion(sdkHome)
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
    val output = CommandLine.runCommand(
      sdkHome,
      createPath(sdkHome),
      Seq("--numeric-version")
    )
    output.map(_.getStdout).getOrElse("-")
  }

  def getStackPath(project: Project): String = {
    val sdkHomePath = getSdkHomePath(project)
    sdkHomePath.map(p => createPath(p)).getOrElse(throw new IllegalStateException("Path to directory of `stack` binary expected to be set in Project SDK setting"))
  }

  private def createPath(sdkHome: String) = {
    sdkHome + File.separator + "stack"
  }
}