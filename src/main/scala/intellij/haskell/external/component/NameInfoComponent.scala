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

package intellij.haskell.external.component

import com.github.blemale.scaffeine.{LoadingCache, Scaffeine}
import com.intellij.openapi.project.Project
import com.intellij.psi.{PsiElement, PsiFile}
import intellij.haskell.external.repl.StackRepl.StackReplOutput
import intellij.haskell.external.repl.StackReplsManager
import intellij.haskell.psi._
import intellij.haskell.util.StringUtil.escapeString
import intellij.haskell.util.{ApplicationUtil, HaskellProjectUtil, StringUtil}

private[component] object NameInfoComponent {

  import intellij.haskell.external.component.NameInfoComponentResult._

  private final val ProjectInfoPattern = """(.+)-- Defined at (.+):([\d]+):([\d]+)""".r
  private final val LibraryModuleInfoPattern = """(.+)-- Defined in [`‘]([\w\.\-]+):([\w\.\-]+)['’]""".r
  private final val ModuleInfoPattern = """(.+)-- Defined in [`‘]([\w\.\-]+)['’]""".r
  private final val InfixInfoPattern = """(infix.+)""".r

  private final val Cache: LoadingCache[Key, NameInfoResult] = Scaffeine().build((k: Key) => findNameInfos(k))

  private case class Key(psiFile: PsiFile, name: String)

  def findNameInfo(psiElement: PsiElement): Option[NameInfoResult] = {
    HaskellPsiUtil.findQualifiedNameParent(psiElement).flatMap(p => findNameInfo(p))
  }

  private def findNameInfos(key: Key): NameInfoResult = {
    val psiFile = key.psiFile
    val project = psiFile.getProject
    val name = key.name
    val isSourceFile = HaskellProjectUtil.isSourceFile(psiFile)
    if (isSourceFile) {
      if (LoadComponent.isFileLoaded(psiFile)) {
        StackReplsManager.getProjectRepl(psiFile) match {
          case Some(repl) => if (repl.isBusy) {
            Left(ReplIsBusy)
          } else if (!repl.available) {
            Left(ReplNotAvailable)
          } else {
            repl.findInfo(psiFile, name) match {
              case None => Left(ReplNotAvailable)
              case Some(output) => Right(createNameInfos(project, output))
            }
          }
          case None => Left(ReplNotAvailable)
        }
      } else {
        Left(ModuleNotLoaded(psiFile.getName))
      }
    } else if (HaskellProjectUtil.isLibraryFile(psiFile)) {
      HaskellPsiUtil.findModuleName(psiFile) match {
        case None => Left(NoInfoAvailable(key.name, psiFile.getName))
        case Some(mn) =>
          StackReplsManager.getGlobalRepl(project).flatMap(_.findInfo(mn, name)) match {
            case None => Left(ReplNotAvailable)
            case Some(output) => createNameInfos(project, output) match {
              case infos if infos.nonEmpty => Right(infos)
              case _ => Left(NoInfoAvailable(key.name, psiFile.getName))
            }
          }
      }
    } else {
      Left(NoInfoAvailable(key.name, psiFile.getName))
    }
  }

  def findNameInfo(qualifiedNameElement: HaskellQualifiedNameElement): Option[NameInfoResult] = {
    val psiFile = qualifiedNameElement.getContainingFile.getOriginalFile
    val key = Key(psiFile, ApplicationUtil.runReadAction(qualifiedNameElement.getName).replaceAll("""\s+""", ""))
    Cache.getIfPresent(key) match {
      case Some(r) => Some(r)
      case None =>
        val result = Cache.get(key)
        result match {
          case Right(_) => Some(result)
          case Left(NoInfoAvailable(_, _)) =>
            None
          case Left(ReplNotAvailable) | Left(ReplIsBusy) | Left(IndexNotReady) | Left(ModuleNotLoaded(_)) | Left(ReadActionTimeout(_)) =>
            Cache.invalidate(key)
            None
        }
    }
  }

  def invalidate(psiFile: PsiFile): Unit = {
    Cache.asMap().filter(_._1.psiFile == psiFile).keys.foreach(Cache.invalidate)
  }

  def invalidateAll(project: Project): Unit = {
    Cache.asMap().map(_._1.psiFile).filter(_.getProject == project).foreach(invalidate)
    NameInfoByModuleComponent.invalidateAll(project)
  }

  private def createNameInfos(project: Project, output: StackReplOutput): Iterable[NameInfo] = {
    output.stdoutLines.flatMap(l => createNameInfo(l, project))
  }

  private def createNameInfo(outputLine: String, project: Project): Option[NameInfo] = {
    val result = outputLine match {
      case ProjectInfoPattern(declaration, filePath, lineNr, colNr) => Some(ProjectNameInfo(declaration, filePath, lineNr.toInt, colNr.toInt))
      case LibraryModuleInfoPattern(declaration, libraryName, moduleName) =>
        if (libraryName == "ghc-prim" || libraryName == "integer-gmp") {
          Some(BuiltInNameInfo(declaration, libraryName, "GHC.Base"))
        }
        else {
          Some(LibraryNameInfo(declaration, moduleName))
        }
      case ModuleInfoPattern(declaration, moduleName) => Some(LibraryNameInfo(declaration, moduleName))
      case InfixInfoPattern(declaration) => Some(InfixInfo(declaration))
      case _ => None
    }
    result
  }

  object NameInfoByModuleComponent {

    private case class Key(project: Project, moduleName: String, name: String)

    private final val Cache: LoadingCache[Key, NameInfoResult] = Scaffeine().build((k: Key) => loadByModuleAndName(k))

    private def loadByModuleAndName(key: Key): NameInfoResult = {
      findNameInfos(key)
    }

    private def findNameInfos(key: Key): Either[NoInfo, Iterable[NameInfo]] = {
      val output = StackReplsManager.getGlobalRepl(key.project).flatMap(_.findInfo(key.moduleName, key.name))
      output match {
        case Some(o) => Right(createNameInfos(key.project, o))
        case None => Left(ReplNotAvailable)
      }
    }

    def findNameInfoByModuleName(project: Project, moduleName: String, name: String): NameInfoResult = {
      val key = Key(project, moduleName, name)
      val result = Cache.get(key)
      result match {
        case Right(_) => result
        case Left(_) =>
          Cache.invalidate(key)
          result
      }
    }

    private[component] def invalidateAll(project: Project): Unit = {
      Cache.asMap().filter(_._1.project == project).keys.foreach(Cache.invalidate)
    }
  }

}

object NameInfoComponentResult {
  type NameInfoResult = Either[NoInfo, Iterable[NameInfo]]

  sealed trait NameInfo {

    def declaration: String

    def shortenedDeclaration: String = StringUtil.shortenHaskellDeclaration(declaration)

    def escapedDeclaration: String = escapeString(declaration).replaceAll("""\s+""", " ")
  }

  case class ProjectNameInfo(declaration: String, filePath: String, lineNr: Int, columnNr: Int) extends NameInfo

  case class LibraryNameInfo(declaration: String, moduleName: String) extends NameInfo

  case class BuiltInNameInfo(declaration: String, libraryName: String, moduleName: String) extends NameInfo

  case class InfixInfo(declaration: String) extends NameInfo

}