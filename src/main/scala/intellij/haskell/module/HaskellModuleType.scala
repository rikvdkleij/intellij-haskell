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

package intellij.haskell.module

import javax.swing.Icon

import com.intellij.openapi.module.{Module, ModuleType, ModuleTypeManager, ModuleUtil}
import com.intellij.openapi.project.Project
import intellij.haskell.HaskellIcons

import scala.collection.JavaConverters._

class HaskellModuleType extends ModuleType[HaskellModuleBuilder](HaskellModuleType.Id) {

  override def createModuleBuilder(): HaskellModuleBuilder = new HaskellModuleBuilder

  override def getName: String = "Haskell module"

  override def getDescription: String = "Haskell module for Haskell project"

  override def getNodeIcon(isOpened: Boolean): Icon = HaskellIcons.HaskellSmallLogo

  override def getBigIcon: Icon = HaskellIcons.HaskellLogo
}

object HaskellModuleType {
  val Id = "HASKELL_MODULE"

  def getInstance: HaskellModuleType = {
    ModuleTypeManager.getInstance.findByID(Id).asInstanceOf[HaskellModuleType]
  }

  def findHaskellProjectModules(project: Project): Iterable[Module] = {
    ModuleUtil.getModulesOfType(project, HaskellModuleType.getInstance).asScala
  }
}
