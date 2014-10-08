/*
 * Copyright 2014 Rik van der Kleij
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

package com.powertuple.intellij.haskell.external

import com.intellij.openapi.components.{ProjectComponent, ServiceManager}
import com.intellij.openapi.editor.SelectionModel
import com.intellij.openapi.project.Project
import com.intellij.psi.{PsiElement, PsiFile}
import com.powertuple.intellij.haskell.psi.HaskellNamedElement
import com.powertuple.intellij.haskell.settings.HaskellSettings

object GhcModiManager {

  private var reinit = false

  def getInstance(project: Project) = ServiceManager.getService(project, classOf[GhcModiManager])

  def setReinit() {
    reinit = true
  }

  def findInfoFor(psiFile: PsiFile, namedElement: HaskellNamedElement): Seq[IdentifierInfo] = {
    GhcModiInfo.findInfoFor(getGhcModi(psiFile), psiFile, namedElement)
  }

  def findTypeInfoFor(psiFile: PsiFile, psiElement: PsiElement): Option[TypeInfo] = {
    GhcModiTypeInfo.findInfoFor(getGhcModi(psiFile), psiFile, psiElement)
  }

  def findTypeInfoForSelection(psiFile: PsiFile, selectionModel: SelectionModel): Option[TypeInfo] = {
    GhcModiTypeInfo.findInfoForSelection(getGhcModi(psiFile), psiFile, selectionModel)
  }

  /**
   * Assuming first definition from ghc-modi info result is the main definition.
   */
  def findTypeSignature(haskellNamedElement: HaskellNamedElement): Option[String] = {
    findInfoFor(haskellNamedElement.getContainingFile, haskellNamedElement) match {
      case Seq(info) => Some(info.typeSignature)
      case _ => None
    }
  }

  private def getGhcModi(psiFile: PsiFile) = {
    getInstance(psiFile.getProject).getGhcModi
  }
}

class GhcModiManager(val project: Project, val settings: HaskellSettings) extends ProjectComponent {
  private var ghcModi: GhcModi = _

  def getGhcModi: GhcModi = {
    if (GhcModiManager.reinit) {
      ghcModi.reinit()
      GhcModiManager.reinit = false
      ghcModi
    }
    ghcModi
  }


  override def projectOpened(): Unit = {
    ghcModi = new GhcModi(settings, project)
    ghcModi.startGhcModi()
  }

  override def projectClosed(): Unit = {
    ghcModi.exit()
  }

  override def initComponent(): Unit = {}

  override def disposeComponent(): Unit = {}

  override def getComponentName: String = "ghcModiManager"
}

