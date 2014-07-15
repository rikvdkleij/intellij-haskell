/*
 * Copyright 2014 Rik van der Kleij

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

package com.powertuple.intellij.haskell.actions

import com.intellij.ide.actions.{CreateFileFromTemplateAction, CreateFileFromTemplateDialog}
import com.intellij.openapi.project.{DumbAware, Project}
import com.intellij.openapi.ui.InputValidatorEx
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiDirectory
import com.powertuple.intellij.haskell.HaskellIcons

object CreateHaskellFileAction {
  private final val NEW_HASKELL_FILE = "New Haskell File"
}

class CreateHaskellFileAction extends CreateFileFromTemplateAction(CreateHaskellFileAction.NEW_HASKELL_FILE, "", HaskellIcons.HASKELL_SMALL_LOGO) with DumbAware {

  protected def buildDialog(project: Project, directory: PsiDirectory, builder: CreateFileFromTemplateDialog.Builder) {
    builder.setTitle(CreateHaskellFileAction.NEW_HASKELL_FILE).addKind("Empty module", HaskellIcons.HASKELL_SMALL_LOGO, "Haskell Module").setValidator(new InputValidatorEx {

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

  protected def getActionName(directory: PsiDirectory, newName: String, templateName: String): String = {
    CreateHaskellFileAction.NEW_HASKELL_FILE
  }
}