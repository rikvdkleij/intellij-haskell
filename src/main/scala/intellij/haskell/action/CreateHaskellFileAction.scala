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

package intellij.haskell.action

import java.text.ParseException

import com.intellij.ide.actions.{CreateFileAction, CreateFileFromTemplateAction, CreateFileFromTemplateDialog}
import com.intellij.ide.fileTemplates.{FileTemplate, FileTemplateManager, FileTemplateUtil}
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.{DumbAware, Project}
import com.intellij.openapi.ui.{InputValidatorEx, Messages}
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.{PsiDirectory, PsiFile}
import intellij.haskell.HaskellIcons
import intellij.haskell.util.HaskellFileUtil

object CreateHaskellFileAction {
  private final val NEW_HASKELL_FILE = "New Haskell File"
}

class CreateHaskellFileAction extends CreateFileFromTemplateAction(CreateHaskellFileAction.NEW_HASKELL_FILE, "", HaskellIcons.HaskellSmallLogo) with DumbAware {

  override def buildDialog(project: Project, directory: PsiDirectory, builder: CreateFileFromTemplateDialog.Builder) {
    builder.setTitle(CreateHaskellFileAction.NEW_HASKELL_FILE).addKind("Empty module", HaskellIcons.HaskellSmallLogo, "Haskell Module").setValidator(new InputValidatorEx {

      def checkInput(inputString: String): Boolean = {
        true
      }

      def canClose(inputString: String): Boolean = {
        !StringUtil.isEmptyOrSpaces(inputString) && getErrorText(inputString) == null
      }

      def getErrorText(inputString: String): String = {
        if (!StringUtil.isCapitalized(inputString)) {
          s"'$inputString' is not a valid Haskell module name"
        } else {
          null
        }
      }
    })
  }


  override def createFileFromTemplate(originName: String, template: FileTemplate, originDir: PsiDirectory): PsiFile = {
    val pathItems = HaskellFileUtil.getPathFromSourceRoot(originDir.getProject, originDir.getVirtualFile).orNull

    // Adapted from super definition.
    val mkdirs = new CreateFileAction.MkDirs(originName, originDir)
    val name = mkdirs.newName
    val dir = mkdirs.directory
    val project = dir.getProject

    val nameWithmodulePrefix = if (pathItems == null || invalidPathItems(pathItems) || pathItems.isEmpty) {
      name
    } else {
      pathItems.mkString(".") + "." + name
    }

    try {
      // Patch props with custom property.
      val props = FileTemplateManager.getInstance(project).getDefaultProperties()
      props.setProperty("NAME", nameWithmodulePrefix)
      val element = FileTemplateUtil.createFromTemplate(template, name, props, dir)

      val psiFile = element.getContainingFile

      val virtualFile = psiFile.getVirtualFile
      if (virtualFile != null) {
        FileEditorManager.getInstance(project).openFile(virtualFile, true)
        val defaultTemplateProperty = getDefaultTemplateProperty
        if (defaultTemplateProperty != null) {
          PropertiesComponent.getInstance(project).setValue(defaultTemplateProperty, template.getName)
        }
        return psiFile
      }
    } catch {
      case e: ParseException => Messages.showErrorDialog(project, "Error parsing Velocity template: " + e.getMessage, "Create File from Template");
      case e: Exception => throw e
    }

    null
  }

  /**
    * Returns true if any directory name starts with a lower case letter.
    */
  def invalidPathItems(pathItems: List[String]): Boolean = {
    pathItems.exists(s => s.isEmpty || !StringUtil.isCapitalized(s.substring(0, 1)))
  }

  protected def getActionName(directory: PsiDirectory, newName: String, templateName: String): String = {
    CreateHaskellFileAction.NEW_HASKELL_FILE
  }
}