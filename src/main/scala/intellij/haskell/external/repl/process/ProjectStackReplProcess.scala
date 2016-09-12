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

package intellij.haskell.external.repl.process

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import intellij.haskell.util.HaskellFileUtil

class ProjectStackReplProcess(project: Project) extends StackReplProcess(project) {

  private[this] var loadedPsiFile: Option[PsiFile] = None

  override def getComponentName: String = "project-stack-repl"

  def findTypeInfoFor(psiFile: PsiFile, startLineNr: Int, startColumnNr: Int, endLineNr: Int, endColumnNr: Int, expression: String): StackReplOutput = {
    checkFileIsLoadedAndExecute(psiFile, filePath => execute(s":type-at $filePath $startLineNr $startColumnNr $endLineNr $endColumnNr $expression"))
  }

  def findLocationInfoFor(psiFile: PsiFile, startLineNr: Int, startColumnNr: Int, endLineNr: Int, endColumnNr: Int, expression: String): StackReplOutput = {
    checkFileIsLoadedAndExecute(psiFile, filePath => execute(s":loc-at $filePath $startLineNr $startColumnNr $endLineNr $endColumnNr $expression"))
  }

  def findNameInfo(psiFile: PsiFile, name: String): StackReplOutput = {
    checkFileIsLoadedAndExecute(psiFile, _ => execute(s":info $name"))
  }

  def load(psiFile: PsiFile): StackReplOutput = {
    val filePath = HaskellFileUtil.getFilePath(psiFile)
    execute(":load " + filePath)
  }

  def load(moduleName: String): StackReplOutput = {
    execute(":load " + moduleName)
  }

  def getModuleIdentifiers(moduleName: String): StackReplOutput = synchronized {
    load(moduleName)
    execute(":browse! " + moduleName)
  }

  def getAllTopLevelModuleIdentifiers(moduleName: String): StackReplOutput = synchronized {
    load(moduleName)
    execute(s":browse! *$moduleName")
  }

  def findAllAvailableLibraryModules: StackReplOutput = synchronized {
    execute(":load")
    loadedPsiFile = None
    execute(""":complete repl "import " """)
  }

  private def checkFileIsLoadedAndExecute(psiFile: PsiFile, executeAction: String => StackReplOutput): StackReplOutput = {
    val filePath = HaskellFileUtil.getFilePath(psiFile)
    if (!loadedPsiFile.contains(psiFile)) {
      load(psiFile)
    }
    val output = executeAction(filePath)
    loadedPsiFile = Some(psiFile)
    output
  }
}
