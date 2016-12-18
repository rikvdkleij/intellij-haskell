package intellij.haskell.cabal.completion

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.module.{Module, ModuleUtilCore}
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import intellij.haskell.HaskellFile
import com.intellij.openapi.util.Computable
import intellij.haskell.cabal.CabalFile

/** Utility for finding Cabal files. */
object CabalFileFinder {

  def virtualForModule(module: Module): Option[VirtualFile] = {
    for {
      moduleFile <- Option(module.getModuleFile)
      parent <- Option(moduleFile.getParent)
      children <- Option(parent.getChildren)
      result <- children.find(_.getExtension == "cabal")
    } yield result
  }

  def psiForModule(module: Module): Option[CabalFile] = {
    virtualForModule(module).flatMap(getPsi(module.getProject, _))
  }

  def virtualForFile(file: HaskellFile): Option[VirtualFile] = {
    Option(ModuleUtilCore.findModuleForPsiElement(file)).flatMap(virtualForModule)
  }

  def psiForFile(file: HaskellFile): Option[CabalFile] = {
    virtualForFile(file).flatMap(getPsi(file.getProject, _))
  }

  def computable[A](f: => A) = new Computable[A] {
    override def compute(): A = f
  }

  private def getPsi(project: Project, cabalVFile: VirtualFile): Option[CabalFile] = {
    ApplicationManager.getApplication.runReadAction(computable {
      Option(PsiManager.getInstance(project).findFile(cabalVFile)).map {
        case cabalPsiFile: CabalFile => cabalPsiFile
        case other => throw new AssertionError(s"Expected CabalFile, got: $other")
      }
    })
  }
}
