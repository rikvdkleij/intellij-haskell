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

import java.io.File
import java.util
import javax.swing.Icon

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.module.{ModifiableModuleModel, Module}
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.{ModifiableRootModel, ModuleRootManager}
import com.intellij.openapi.roots.ui.configuration.ModulesProvider
import com.intellij.packaging.artifacts.ModifiableArtifactModel
import com.intellij.projectImport.ProjectImportBuilder
import intellij.haskell.HaskellIcons
import intellij.haskell.util.HaskellProjectUtil

import scala.collection.JavaConverters._

class StackProjectImportBuilder extends ProjectImportBuilder[Unit] {
  override def getName: String = "Haskell Stack"

  override def getList: util.List[Unit] = new util.ArrayList[Unit]()

  override def getIcon: Icon = HaskellIcons.HaskellSmallLogo

  override def setOpenProjectSettingsAfter(on: Boolean): Unit = {}

  override def setList(list: util.List[Unit]): Unit = ()

  override def isMarked(element: Unit): Boolean = true

  override def commit(project: Project, model: ModifiableModuleModel, modulesProvider: ModulesProvider, artifactModel: ModifiableArtifactModel): java.util.List[Module] = {
    val haskellModuleBuilder = HaskellModuleType.getInstance.createModuleBuilder()
    HaskellProjectUtil.getModuleManager(project).map(_.getModifiableModel).map { moduleModel =>
      val module = moduleModel.newModule(getModuleFilePath, HaskellModuleType.getInstance.getId)
      val rootModel = ModuleRootManager.getInstance(module).getModifiableModel
      rootModel.inheritSdk()

      ApplicationManager.getApplication.runWriteAction(new Runnable {
        override def run(): Unit = {
          haskellModuleBuilder.setName(getModuleName)
          haskellModuleBuilder.setModuleFilePath(getModuleFilePath)

          //haskellModuleBuilder.createModule(moduleModel)

          rootModel.commit()
          moduleModel.commit()

          haskellModuleBuilder.commit(project)
          haskellModuleBuilder.addModuleConfigurationUpdater((module: Module, rootModel: ModifiableRootModel) => {
            haskellModuleBuilder.setupRootModel(rootModel)
          })
        }
      })

      moduleModel.getModules.toList.asJava
    }.getOrElse(new util.ArrayList[Module]())
  }

  private def getModuleName: String = {
    val moduleDirPath = new File(getFileToImport)
    if (moduleDirPath.isDirectory) moduleDirPath.getName
    else
      throw new IllegalStateException("What to import has to be directory")
  }

  private def getModuleFilePath = {
    new File(new File(getFileToImport), getModuleName).getAbsolutePath + ".iml"
  }
}