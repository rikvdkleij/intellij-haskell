package com.powertuple.intellij.haskell.psi

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFileFactory
import com.powertuple.intellij.haskell.HaskellFileType

object HaskellElementFactory {

  def createVar(project: Project, name: String): HaskellVarid = {
    val haskellFile = createFile(project, name)
    haskellFile.getFirstChild.asInstanceOf[HaskellVarid]
  }

  def createFile(project: Project, text: String): HaskellFile = {
    val name: String = "dummy.hs"
    PsiFileFactory.getInstance(project).createFileFromText(name, HaskellFileType.INSTANCE, text).asInstanceOf[HaskellFile]
  }
}
