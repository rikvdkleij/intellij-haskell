/*
 * Copyright 2014-2020 Rik van der Kleij
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

package intellij.haskell.external.component

import com.github.blemale.scaffeine.{LoadingCache, Scaffeine}
import com.intellij.openapi.editor.SelectionModel
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.psi.{PsiElement, PsiFile}
import intellij.haskell.external.repl.{ProjectStackRepl, StackReplsManager}
import intellij.haskell.psi._
import intellij.haskell.util.{ApplicationUtil, HaskellFileUtil, LineColumnPosition}

import scala.concurrent.TimeoutException

private[component] object TypeInfoComponent {

  import intellij.haskell.external.component.TypeInfoComponentResult._

  private case class Key(psiFile: PsiFile, qualifiedNameElement: HaskellQualifiedNameElement)

  private final val Cache: LoadingCache[Key, TypeInfoResult] = Scaffeine().build((k: Key) => findTypeInfoResult(k.psiFile, k.qualifiedNameElement))

  def findTypeInfoForElement(element: PsiElement): TypeInfoResult = {
    def getFile = {
      Option(element.getContainingFile).map(_.getOriginalFile)
    }

    def getFileName = {
      getFile.map(_.getName).getOrElse("-")
    }

    if (element.getNode.getElementType == HaskellTypes.HS_UNDERSCORE) {
      findTypeInfoResult(element.getContainingFile, element)
    } else {
      (for {
        qne <- HaskellPsiUtil.findQualifiedName(element)
        pf <- getFile
      } yield {
        Key(pf, qne)
      }).map(k => findTypeInfo(k)).getOrElse(Left(NoInfoAvailable(element.getText, getFileName)))
    }
  }

  def findTypeInfoForSelection(psiFile: PsiFile, selectionModel: SelectionModel): TypeInfoResult = {
    val moduleName = HaskellPsiUtil.findModuleName(psiFile)
    if (LoadComponent.isModuleLoaded(moduleName, psiFile)) {
      {
        for {
          vf <- HaskellFileUtil.findVirtualFile(psiFile)
          sp <- LineColumnPosition.fromOffset(vf, selectionModel.getSelectionStart)
          ep <- LineColumnPosition.fromOffset(vf, selectionModel.getSelectionEnd)
        } yield {
          StackReplsManager.getProjectRepl(psiFile).flatMap(_.findTypeInfo(moduleName, psiFile, sp.lineNr, sp.columnNr, ep.lineNr, ep.columnNr, selectionModel.getSelectedText)) match {
            case Some(output) if output.stderrLines.nonEmpty => Left(NoInfoAvailable(selectionModel.getSelectedText, psiFile.getName, Some(output.stderrLines.mkString(" "))))
            case Some(output) => Right(TypeInfo(output.stdoutLines.headOption.filterNot(_.trim.isEmpty).mkString(" ")))
            case None => Left(ReplNotAvailable)
          }
        }
      }.getOrElse(Left(NoInfoAvailable(selectionModel.getSelectedText, psiFile.getName)))
    } else {
      Left(ModuleNotAvailable(moduleName.getOrElse(psiFile.getName)))
    }
  }

  def invalidateAll(project: Project): Unit = {
    Cache.asMap().filter(_._1.psiFile.getProject == project).keys.foreach(Cache.invalidate)
  }

  private def findTypeInfoResult(psiFile: PsiFile, element: PsiElement): TypeInfoResult = {
    ProgressManager.checkCanceled()
    val findTypeInfo = for {
      vf <- HaskellFileUtil.findVirtualFile(psiFile)
      to = element.getTextOffset
      sp <- LineColumnPosition.fromOffset(vf, to)
      _ = ProgressManager.checkCanceled()
      t = element.getText
      ep <- LineColumnPosition.fromOffset(vf, to + (if (t.length > 1) t.length - 1 else 1))
      t = element.getText
      _ = ProgressManager.checkCanceled()
      mn = HaskellPsiUtil.findModuleName(psiFile)
      if element.isValid
    } yield {
      ProgressManager.checkCanceled()
      repl: ProjectStackRepl => repl.findTypeInfo(mn, psiFile, sp.lineNr, sp.columnNr, ep.lineNr, ep.columnNr, t)
    }

    findTypeInfo match {
      case None => Left(NoInfoAvailable(element.getText, psiFile.getName))
      case Some(f) => StackReplsManager.getProjectRepl(psiFile) match {
        case None => Left(ReplNotAvailable)
        case Some(repl) =>
          if (!repl.available) {
            Left(ReplNotAvailable)
          } else {
            f(repl) match {
              case Some(output) if output.stderrLines.nonEmpty => Left(NoInfoAvailable(element.getText, psiFile.getName, Some(output.stderrLines.mkString(" "))))
              case Some(output) => Right(TypeInfo(output.stdoutLines.filterNot(_.trim.isEmpty).mkString(" ")))
              case None => Left(ReplNotAvailable)
            }
          }
      }
    }
  }

  private def findTypeInfo(key: Key): TypeInfoResult = {
    try {
      val result = ApplicationUtil.runReadAction(Cache.get(key), Some(key.psiFile.getProject))
      result match {
        case Right(_) => result
        case Left(ReadActionTimeout(_)) | Left(IndexNotReady) | Left(ModuleNotAvailable(_)) | Left(ReplNotAvailable) =>
          Cache.invalidate(key)
          result
        case _ => result

      }
    } catch {
      case e: TimeoutException => Left(ReadActionTimeout(e.getMessage))
    }
  }
}

object TypeInfoComponentResult {

  type TypeInfoResult = Either[NoInfo, TypeInfo]

  case class TypeInfo(typeSignature: String)

}
