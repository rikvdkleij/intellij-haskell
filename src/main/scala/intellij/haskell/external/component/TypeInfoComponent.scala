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

package intellij.haskell.external.component

import com.github.blemale.scaffeine.{LoadingCache, Scaffeine}
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.SelectionModel
import com.intellij.openapi.util.Computable
import com.intellij.psi.{PsiElement, PsiFile}
import intellij.haskell.external.repl.StackReplsManager
import intellij.haskell.psi._
import intellij.haskell.util.LineColumnPosition

private[component] object TypeInfoComponent {

  import intellij.haskell.external.component.TypeInfoComponentResult._

  private case class Key(psiFile: PsiFile, startLineNr: Int, startColumnNr: Int, endLineNr: Int, endColumnNr: Int, expression: String)

  private final val Cache: LoadingCache[Key, TypeInfoResult] = Scaffeine().build((k: Key) => findTypeInfoResult(k))

  def findTypeInfoForElement(psiElement: PsiElement): Option[TypeInfoResult] = {
    val result = if (psiElement.isValid) {
      ApplicationManager.getApplication.runReadAction(new Computable[Option[Key]] {
        override def compute(): Option[Key] = {
          for {
            qne <- HaskellPsiUtil.findQualifiedNameParent(psiElement)
            to = qne.getTextOffset
            pf <- Option(psiElement.getContainingFile)
            sp <- LineColumnPosition.fromOffset(pf, to)
            ep <- LineColumnPosition.fromOffset(pf, to + qne.getText.length)
          } yield Key(pf, sp.lineNr, sp.columnNr, ep.lineNr, ep.columnNr, qne.getName)
        }
      }).map(findTypeInfo)
    } else {
      None
    }
    result
  }

  def findTypeInfoForSelection(psiFile: PsiFile, selectionModel: SelectionModel): Option[TypeInfoResult] = {
    val result = for {
      sp <- LineColumnPosition.fromOffset(psiFile, selectionModel.getSelectionStart)
      ep <- LineColumnPosition.fromOffset(psiFile, selectionModel.getSelectionEnd)
    } yield findTypeInfo(Key(psiFile, sp.lineNr, sp.columnNr, ep.lineNr, ep.columnNr, selectionModel.getSelectedText))
    result
  }

  def invalidate(psiFile: PsiFile): Unit = {
    Cache.asMap().filter(_._1.psiFile == psiFile).keys.foreach(Cache.invalidate)
  }

  private def findTypeInfoResult(key: Key): TypeInfoResult = {
    if (LoadComponent.isBusy(key.psiFile)) {
      Left(ReplIsBusy)
    } else {
      val moduleName = HaskellPsiUtil.findModuleName(key.psiFile, runInRead = true)
      val typeInfo = StackReplsManager.getProjectRepl(key.psiFile).flatMap(_.findTypeInfo(moduleName, key.psiFile, key.startLineNr, key.startColumnNr, key.endLineNr, key.endColumnNr, key.expression)) match {
        case Some(output) => output.stdoutLines.headOption.filterNot(_.trim.isEmpty).map(ti => Right(TypeInfo(ti, output.stderrLines.nonEmpty))).getOrElse(Left(NoInfoAvailable))
        case _ => Left(ReplNotAvailable)
      }
      typeInfo
    }
  }

  private def findTypeInfo(key: Key): TypeInfoResult = {
    val result = Cache.get(key)
    result match {
      case Right(_) => result
      case Left(NoInfoAvailable) =>
        result
      case Left(ReplNotAvailable) =>
        Cache.invalidate(key)
        result
      case Left(ReplIsBusy) =>
        Cache.invalidate(key)
        result
    }
  }
}

object TypeInfoComponentResult {

  type TypeInfoResult = Either[NoInfo, TypeInfo]

  case class TypeInfo(typeSignature: String, withFailure: Boolean)

}
