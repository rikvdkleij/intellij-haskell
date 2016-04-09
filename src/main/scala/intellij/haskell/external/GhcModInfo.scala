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

package intellij.haskell.external

import java.util.concurrent.{Callable, Executors, TimeUnit}

import com.google.common.cache.{CacheBuilder, CacheLoader}
import com.google.common.util.concurrent.{ListenableFuture, ListenableFutureTask, UncheckedExecutionException}
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import intellij.haskell.psi._
import intellij.haskell.psi.impl.HaskellPsiImplUtil
import intellij.haskell.util.HaskellEditorUtil.escapeString
import intellij.haskell.util.{FileUtil, HaskellElementCondition, HaskellFindUtil}

import scala.collection.mutable.ListBuffer

object GhcModInfo {

  private final val GhcModInfoPattern = """(.+)-- Defined at (.+):([\d]+):([\d]+)""".r
  private final val GhcModInfoLibraryPathPattern = """(.+)-- Defined in ‘([\w\.\-]+):([\w\.\-]+)’""".r
  private final val GhcModInfoLibraryPattern = """(.+)-- Defined in ‘([\w\.\-]+)’""".r

  private final val NoInfoIndicator = "Cannot show info"

  private final val Executor = Executors.newCachedThreadPool()

  private case class NamedElementInfo(filePath: String, identifier: String, project: Project)

  private final val InfoCache = CacheBuilder.newBuilder()
    .refreshAfterWrite(3, TimeUnit.SECONDS)
    .build(
      new CacheLoader[NamedElementInfo, GhcModOutput]() {
        private def findInfoFor(namedElementInfo: NamedElementInfo): GhcModOutput = {
          val cmd = s"info ${namedElementInfo.filePath} ${namedElementInfo.identifier}"
          GhcModProcessManager.getGhcModProcess(namedElementInfo.project).execute(cmd)
        }

        override def load(namedElementInfo: NamedElementInfo): GhcModOutput = {
          findInfoFor(namedElementInfo)
        }

        override def reload(namedElementInfo: NamedElementInfo, oldInfo: GhcModOutput): ListenableFuture[GhcModOutput] = {
          val task = ListenableFutureTask.create(new Callable[GhcModOutput]() {
            def call() = {
              val newInfo = findInfoFor(namedElementInfo)
              if (newInfo.outputLines.isEmpty || newInfo.outputLines.head == NoInfoIndicator) {
                oldInfo
              } else {
                newInfo
              }
            }
          })
          Executor.execute(task)
          task
        }
      }
    )

  def findInfoFor(psiFile: PsiFile, namedElement: HaskellNamedElement): Iterable[IdentifierInfo] = {
    val ghcModOutput = findIdentifier(namedElement).map { id =>
      try {
        val key = NamedElementInfo(FileUtil.getFilePath(psiFile), id, psiFile.getProject)
        val output = InfoCache.get(key)
        if (output.outputLines.isEmpty || output.outputLines.head == NoInfoIndicator) {
          InfoCache.refresh(key)
          InfoCache.get(key)
        } else {
          output
        }
      }
      catch {
        case _: UncheckedExecutionException => GhcModOutput()
        case _: ProcessCanceledException => GhcModOutput()
      }
    }

    (for {
      output <- ghcModOutput
      outputLine <- output.outputLines.headOption
      identifierInfos <- createIdentifierInfos(outputLine, psiFile.getProject)
    } yield identifierInfos).getOrElse(Iterable())
  }

  private def createIdentifierInfos(outputLine: String, project: Project): Option[Iterable[IdentifierInfo]] = {
    if (outputLine == NoInfoIndicator) {
      None
    } else {
      val outputLines = outputLine.split("\u0000")
      val outputInfos = createInfoPerDefinition(outputLines)
      Some(outputInfos.map(sb => createIdentifierInfo(sb.toString, project)))
    }
  }

  private def createInfoPerDefinition(outputLines: Array[String]): ListBuffer[StringBuilder] = {
    outputLines.foldLeft(ListBuffer[StringBuilder]())((lb, sb) =>
      if (sb.startsWith(" ")) {
        lb.last.append(sb)
        lb
      }
      else {
        lb += new StringBuilder(2, sb)
      })
  }

  private def createIdentifierInfo(outputInfo: String, project: Project): IdentifierInfo = {
    outputInfo match {
      case GhcModInfoPattern(typeSignature, filePath, lineNr, colNr) => ProjectIdentifierInfo(escapeString(typeSignature), Some(filePath), lineNr.toInt, colNr.toInt)
      case GhcModInfoLibraryPathPattern(typeSignature, libraryName, module) =>
        if (libraryName == "ghc-prim" || libraryName == "integer-gmp") {
          BuiltInIdentifierInfo(escapeString(typeSignature), libraryName, "GHC.Base")
        }
        else {
          createLibraryIdentifierInfo(module, escapeString(typeSignature), project)
        }
      case GhcModInfoLibraryPattern(typeSignature, module) =>
        createLibraryIdentifierInfo(module, escapeString(typeSignature), project)
      case d => NoLocationIdentifierInfo(d)
    }
  }

  private def findIdentifier(namedElement: HaskellNamedElement): Option[String] = {
    val qVarConOp = Option(PsiTreeUtil.findFirstParent(namedElement, HaskellElementCondition.QVarConOpElementCondition)).map(_.asInstanceOf[HaskellQVarConOpElement])
    // Workaround for https://github.com/DanielG/ghc-mod/issues/432
    qVarConOp.map { qvco =>
      qvco.getQualifier match {
        case Some(q) =>
          val importedQualifiedModules = getImportedQualifiedModules(namedElement.getContainingFile)
          val projectModuleNames = HaskellFindUtil.findProjectModules(namedElement.getProject).map(_.getModuleName)
          if (importedQualifiedModules.find(qi => qi.qualifier == q).map(_.moduleName).exists(m => projectModuleNames.exists(_ == m))) {
            qvco.getIdentifierElement.getName
          } else {
            qvco.getName
          }
        case None => qvco.getIdentifierElement.getName
      }
    }
  }

  private case class QualifiedImport(qualifier: String, moduleName: String)

  private def getImportedQualifiedModules(psiFile: PsiFile): Iterable[QualifiedImport] = {
    val importDeclarations = HaskellPsiHelper.findImportDeclarations(psiFile)
    importDeclarations.flatMap(i => Option(i.getImportQualifiedAs).flatMap(qa => Option(qa.getQualifier).map(q => QualifiedImport(q.getName, i.getModuleName))))
  }

  private def createLibraryIdentifierInfo(module: String, typeSignature: String, project: Project) = {
    LibraryIdentifierInfo(typeSignature, FileUtil.findModuleFilePath(module, project), module)
  }
}

sealed trait IdentifierInfo {
  def declaration: String
}

trait FileInfo {
  def filePath: Option[String]
}

case class ProjectIdentifierInfo(declaration: String, filePath: Option[String], lineNr: Int, colNr: Int) extends IdentifierInfo with FileInfo

case class LibraryIdentifierInfo(declaration: String, filePath: Option[String], module: String) extends IdentifierInfo with FileInfo

case class BuiltInIdentifierInfo(declaration: String, libraryName: String, module: String) extends IdentifierInfo

case class NoLocationIdentifierInfo(declaration: String) extends IdentifierInfo
