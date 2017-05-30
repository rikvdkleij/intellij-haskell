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
import com.intellij.openapi.roots.ModifiableRootModel
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
    val moduleBuilder = HaskellModuleType.getInstance.createModuleBuilder()
    HaskellProjectUtil.getModuleManager(project).map(_.getModifiableModel).map { moduleModel =>

      ApplicationManager.getApplication.runWriteAction(new Runnable {
        override def run(): Unit = {
          val moduleDirectory = getModuleRootDirectory
          val moduleName = getModuleName(moduleDirectory)
          moduleBuilder.setName(moduleName)
          moduleBuilder.setModuleFilePath(getModuleImlFilePath(moduleDirectory, moduleName))

          moduleBuilder.commit(project)
          moduleBuilder.addModuleConfigurationUpdater((module: Module, rootModel: ModifiableRootModel) => {
            moduleBuilder.setupRootModel(rootModel)
          })
        }
      })

      moduleModel.getModules.toList.asJava
    }.getOrElse(new util.ArrayList[Module]())
  }

  private def getModuleRootDirectory = {
    val moduleDirectory = new File(getFileToImport)
    if (moduleDirectory.isDirectory) {
      moduleDirectory
    }
    else
      throw new IllegalStateException("What to import should be directory")
  }

  private def getModuleName(moduleDirectory: File): String = {
    HaskellProjectUtil.findCabalPackageName(moduleDirectory) match {
      case Some(n) => n
      case _ => throw new IllegalStateException("Can not find Cabal file")
    }
  }

  private def getModuleImlFilePath(moduleDir: File, moduleName: String): String = {
    new File(moduleDir, moduleName).getAbsolutePath + ".iml"
  }
}