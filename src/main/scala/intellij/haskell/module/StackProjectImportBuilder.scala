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

package intellij.haskell.module

import java.io.File
import java.nio.file.Paths
import java.util

import com.intellij.ide.util.projectWizard.ModuleBuilder
import com.intellij.openapi.module.{ModifiableModuleModel, Module, ModuleType}
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModifiableRootModel
import com.intellij.openapi.roots.ui.configuration.ModulesProvider
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.packaging.artifacts.ModifiableArtifactModel
import com.intellij.projectImport.ProjectImportBuilder
import intellij.haskell.HaskellIcons
import intellij.haskell.stackyaml.StackYamlComponent
import intellij.haskell.util.{HaskellFileUtil, HaskellProjectUtil}
import javax.swing.Icon

import scala.collection.JavaConverters._

class StackProjectImportBuilder extends ProjectImportBuilder[Unit] {
  override def getName: String = "Haskell Stack"

  override def getList: util.List[Unit] = new util.ArrayList[Unit]()

  override def getIcon: Icon = HaskellIcons.HaskellSmallLogo

  override def setOpenProjectSettingsAfter(on: Boolean): Unit = {}

  override def setList(list: util.List[Unit]): Unit = ()

  override def isMarked(element: Unit): Boolean = true

  private final val projectRootRelativePath = "."

  override def getTitle: String = "Stack project importer"

  override def commit(project: Project, model: ModifiableModuleModel, modulesProvider: ModulesProvider, artifactModel: ModifiableArtifactModel): java.util.List[Module] = {
    val moduleBuilder = HaskellModuleType.getInstance.createModuleBuilder()

    val packagePaths = StackYamlComponent.getPackagePaths(project).getOrElse(Seq(projectRootRelativePath))

    packagePaths.foreach(packageRelativePath => {
      val moduleDirectory = HaskellModuleBuilder.getModuleRootDirectory(packageRelativePath, getFileToImport)
      HaskellModuleBuilder.createCabalInfo(project, getFileToImport, packageRelativePath) match {
        case Some(cabalInfo) =>
          val packageName = cabalInfo.packageName
          moduleBuilder.setCabalInfo(cabalInfo)
          moduleBuilder.setName(packageName)
          moduleBuilder.setModuleFilePath(getModuleImlFilePath(moduleDirectory, packageName))
          moduleBuilder.commit(project)
          moduleBuilder.addModuleConfigurationUpdater((_: Module, rootModel: ModifiableRootModel) => {
            moduleBuilder.setupRootModel(rootModel)
          })
        case None => ()
      }
    })

    if (!packagePaths.contains(projectRootRelativePath)) {
      val parentModuleBuilder = new ParentModuleBuilder(project)
      parentModuleBuilder.setModuleFilePath(Paths.get(project.getBasePath, project.getName + "-parent.iml").toString)
      parentModuleBuilder.setName("Parent module")
      parentModuleBuilder.commit(project)
      parentModuleBuilder.addModuleConfigurationUpdater((_: Module, rootModel: ModifiableRootModel) => {
        parentModuleBuilder.setupRootModel(rootModel)
      })
    }

    HaskellProjectUtil.getModuleManager(project).map(_.getModules).getOrElse(Array()).toList.asJava
  }

  private def getModuleImlFilePath(moduleDirectory: File, packageName: String): String = {
    Paths.get(moduleDirectory.getAbsolutePath, packageName + ".iml").toString
  }
}

class ParentModuleBuilder(val project: Project) extends ModuleBuilder {
  override def isOpenProjectSettingsAfter = true

  override def canCreateModule = true

  override def setupRootModel(modifiableRootModel: ModifiableRootModel): Unit = {
    modifiableRootModel.addContentEntry(HaskellFileUtil.getUrlByPath(project.getBasePath))

    val stackWorkDirectory = HaskellModuleBuilder.getStackWorkDirectory(this)
    stackWorkDirectory.mkdir()
    Option(LocalFileSystem.getInstance.refreshAndFindFileByIoFile(stackWorkDirectory)).foreach(f => {
      val contentEntry = doAddContentEntry(modifiableRootModel)
      contentEntry.addExcludeFolder(f)
    })
  }

  override def getModuleType: ModuleType[_ <: ModuleBuilder] = HaskellModuleType.getInstance

  override def getPresentableName = "Parent Module"

  override def getGroupName: String = getPresentableName

  override def isTemplateBased = true

  override def getDescription = "Module at root of project so directories at root level are accessible"
}