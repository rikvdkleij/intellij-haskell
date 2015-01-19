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

import java.util.concurrent.{Callable, Executors, TimeUnit}

import com.google.common.cache.{CacheBuilder, CacheLoader}
import com.google.common.util.concurrent.{ListenableFuture, ListenableFutureTask, UncheckedExecutionException}
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.powertuple.intellij.haskell.psi._
import com.powertuple.intellij.haskell.util.{FileUtil, HaskellElementCondition, HaskellFindUtil}

import scala.annotation.tailrec
import scala.collection.mutable.ListBuffer

object GhcModiInfo {

  private final val GhcModiInfoPattern = """(.+)-- Defined at (.+):([\d]+):([\d]+)""".r
  private final val GhcModiInfoLibraryPathPattern = """(.+)-- Defined in ‘([\w\.\-]+):([\w\.\-]+)’""".r
  private final val GhcModiInfoLibraryPattern = """(.+)-- Defined in ‘([\w\.\-]+)’""".r

  private final val NoInfoIndicator = "Cannot show info"

  private final val Executor = Executors.newCachedThreadPool()

  private case class NamedElementInfo(filePath: String, identifier: String, project: Project)

  private final val InfoCache = CacheBuilder.newBuilder()
      .refreshAfterWrite(10, TimeUnit.SECONDS)
      .build(
        new CacheLoader[NamedElementInfo, GhcModiOutput]() {
          private def findInfoFor(namedElementInfo: NamedElementInfo): GhcModiOutput = {
            val cmd = s"info ${namedElementInfo.filePath} ${namedElementInfo.identifier}"
            GhcModiManager.getGhcModi(namedElementInfo.project).execute(cmd)
          }

          override def load(namedElementInfo: NamedElementInfo): GhcModiOutput = {
            findInfoFor(namedElementInfo)
          }

          override def reload(namedElementInfo: NamedElementInfo, oldInfo: GhcModiOutput): ListenableFuture[GhcModiOutput] = {
            val task = ListenableFutureTask.create(new Callable[GhcModiOutput]() {
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
    val ghcModiOutput = findIdentifier(namedElement).map { id =>
      try {
        InfoCache.get(NamedElementInfo(FileUtil.getFilePath(psiFile), id, psiFile.getProject))
      }
      catch {
        case _: UncheckedExecutionException => GhcModiOutput()
        case _: ProcessCanceledException => GhcModiOutput()
      }
    }

    (for {
      output <- ghcModiOutput
      outputLine <- output.outputLines.headOption
      identifierInfos <- createIdentifierInfos(outputLine, psiFile.getProject)
    } yield identifierInfos).getOrElse(Iterable())
  }

  private def createIdentifierInfos(outputLine: String, project: Project): Option[Iterable[IdentifierInfo]] = {
    if (outputLine == NoInfoIndicator) {
      None
    } else {
      val outputLines = outputLine.split("\u0000")
      val outputInfos = createInfoPerDefinition(outputLines, ListBuffer()).map(_.map(_.trim).mkString(" "))
      Some(outputInfos.flatMap(s => createIdentifierInfo(s, project)))
    }
  }

  @tailrec
  private def createInfoPerDefinition(outputLines: Array[String], outputInfos: ListBuffer[Array[String]]): ListBuffer[Array[String]] = {
    val index = outputLines.indexWhere(_.contains("Defined"))
    if (index > -1) {
      val pair = outputLines.splitAt(index + 1)
      createInfoPerDefinition(pair._2, outputInfos.+=(pair._1))
    } else {
      outputInfos
    }
  }

  private def createIdentifierInfo(outputInfo: String, project: Project): Option[IdentifierInfo] = {
    outputInfo match {
      case GhcModiInfoPattern(typeSignature, filePath, lineNr, colNr) => Some(ProjectIdentifierInfo(typeSignature, Some(filePath), lineNr.toInt, colNr.toInt))
      case GhcModiInfoLibraryPathPattern(typeSignature, libraryName, module) =>
        if (libraryName == "ghc-prim" || libraryName == "integer-gmp") {
          Some(BuiltInIdentifierInfo(typeSignature, libraryName, "GHC.Base"))
        }
        else {
          createLibraryIdentifierInfo(module, typeSignature, project)
        }
      case GhcModiInfoLibraryPattern(typeSignature, module) =>
        createLibraryIdentifierInfo(module, typeSignature, project)
      case _ => None
    }
  }

  private def findIdentifier(namedElement: HaskellNamedElement): Option[String] = {
    val qVarConOp = Option(PsiTreeUtil.findFirstParent(namedElement, HaskellElementCondition.QVarConOpElementCondition)).map(_.asInstanceOf[HaskellQVarConOpElement])
    // Workaround for https://github.com/kazu-yamamoto/ghc-mod/issues/432
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
    importDeclarations.map(i => Option(i.getImportQualifiedAs).map(qa => QualifiedImport(qa.getQualifier.getName, i.getModuleName))).flatten
  }

  private def createLibraryIdentifierInfo(module: String, typeSignature: String, project: Project) = {
    FileUtil.findModuleFilePath(module, project) match {
      case Some(fp) => Some(LibraryIdentifierInfo(typeSignature, Some(fp), module))
      case None => Some(LibraryIdentifierInfo(typeSignature, None, module))
    }
  }
}

sealed trait IdentifierInfo {
  def typeSignature: String
}

trait FileInfo {
  def filePath: Option[String]
}

case class ProjectIdentifierInfo(typeSignature: String, filePath: Option[String], lineNr: Int, colNr: Int) extends IdentifierInfo with FileInfo

case class LibraryIdentifierInfo(typeSignature: String, filePath: Option[String], module: String) extends IdentifierInfo with FileInfo

case class BuiltInIdentifierInfo(typeSignature: String, libraryName: String, module: String) extends IdentifierInfo