/*
 * Copyright 2014-2017 Rik van der Kleij
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

package intellij.haskell.action

import java.io.File
import java.text.ParseException

import com.intellij.ide.actions.CreateFileFromTemplateDialog.Builder
import com.intellij.ide.actions.{CreateFileAction, CreateFileFromTemplateAction}
import com.intellij.ide.fileTemplates.{FileTemplate, FileTemplateManager, FileTemplateUtil}
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.{DumbAware, Project}
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.ui.{InputValidatorEx, Messages}
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.{PsiDirectory, PsiFile}
import intellij.haskell.HaskellIcons

object CreateHaskellFileAction {
  private final val NEW_HASKELL_FILE = "New Haskell File"
}

class CreateHaskellFileAction extends CreateFileFromTemplateAction(CreateHaskellFileAction.NEW_HASKELL_FILE, "", HaskellIcons.HaskellSmallLogo) with DumbAware {

  override def buildDialog(project: Project, directory: PsiDirectory, builder: Builder): Unit = {
    builder.setTitle(CreateHaskellFileAction.NEW_HASKELL_FILE).addKind("Empty module", HaskellIcons.HaskellSmallLogo, "Haskell Module").setValidator(new InputValidatorEx {

      def checkInput(inputString: String): Boolean = {
        true
      }

      def canClose(inputString: String): Boolean = {
        !StringUtil.isEmptyOrSpaces(inputString) && getErrorText(inputString) == null
      }

      def getErrorText(inputString: String): String = {
        val checkString = if (inputString.contains(".")) {
          inputString.trim.split("\\.").last
        } else {
          inputString
        }

        if (!StringUtil.isCapitalized(checkString)) {
          s"'$inputString' is not a valid Haskell module name"
        } else {
          null
        }
      }
    })
  }

  private def createFileFromTemplate(pathItems: Option[List[String]], fileName: String, template: FileTemplate, fileDir: PsiDirectory): PsiFile = {
    pathItems match {
      case None => null
      case Some(items) =>
        // Adapted from super definition.
        val mkdirs = new CreateFileAction.MkDirs(fileName, fileDir)
        val name = mkdirs.newName
        val dir = mkdirs.directory
        val project = dir.getProject

        val nameWithmodulePrefix = if (invalidPathItems(items) || items.isEmpty) {
          name
        } else {
          items.mkString(".") + "." + name
        }

        // Patch props with custom property.
        val props = FileTemplateManager.getInstance(project).getDefaultProperties()
        props.setProperty("NAME", nameWithmodulePrefix)

        val element = FileTemplateUtil.createFromTemplate(template, name, props, dir)
        val psiFile = element.getContainingFile

        try {
          val virtualFile = Option(psiFile.getVirtualFile)

          virtualFile.foreach(vFile => {
            FileEditorManager.getInstance(project).openFile(vFile, true)
            Option(getDefaultTemplateProperty).foreach(defaultTemplateProperty => {
              PropertiesComponent.getInstance(project).setValue(defaultTemplateProperty, template.getName)
            })
          })
        } catch {
          case e: ParseException => Messages.showErrorDialog(project, "Error parsing Haskell Module template: " + e.getMessage, "Create File from Template");
        }

        psiFile
    }
  }

  override def createFileFromTemplate(fileName: String, template: FileTemplate, fileDir: PsiDirectory): PsiFile = {
    val path = fileDir.getVirtualFile.getPath
    val pathItems = ProjectRootManager.getInstance(fileDir.getProject)
      .getContentSourceRoots
      .map(_.getPath)
      .find(path.startsWith)
      .map(s => if (s != path) {
        path.replace(s + File.separator, "").split(File.separator).toList
      } else {
        List()
      })

    if (fileName.contains(".")) {
      var targetDir = fileDir
      val names = fileName.trim().split("\\.").toList
      val moduleName = names.last
      val prefixes = names.dropRight(1)

      prefixes.foreach(dirName => {
        targetDir = Option(targetDir.findSubdirectory(dirName)).getOrElse(targetDir.createSubdirectory(dirName))
      })

      createFileFromTemplate(pathItems.map(_ ++ prefixes), moduleName, template, targetDir)
    } else {
      createFileFromTemplate(pathItems, fileName, template, fileDir)
    }
  }

  /**
    * Returns true if any directory name starts with a lower case letter.
    */
  private def invalidPathItems(pathItems: List[String]): Boolean = {
    pathItems.exists(s => s.isEmpty || !StringUtil.isCapitalized(s.substring(0, 1)))
  }

  protected def getActionName(directory: PsiDirectory, newName: String, templateName: String): String = {
    CreateHaskellFileAction.NEW_HASKELL_FILE
  }

}