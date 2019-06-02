/*
 * Copyright 2014-2019 Rik van der Kleij
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

import java.util
import java.util.concurrent.TimeoutException

import com.github.blemale.scaffeine.{LoadingCache, Scaffeine}
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import intellij.haskell.HaskellNotificationGroup
import intellij.haskell.external.component.HaskellComponentsManager.StackComponentInfo
import intellij.haskell.psi.HaskellPsiUtil.findImportDeclarations
import intellij.haskell.psi.{HaskellImportDeclaration, HaskellImportId, HaskellPsiUtil}
import intellij.haskell.util.{ApplicationUtil, HaskellProjectUtil, ScalaFutureUtil}

import scala.collection.JavaConverters._
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

object FileModuleIdentifiers {

  private case class Key(psiFile: PsiFile)

  private type Result = Option[ModuleIdentifiers]

  private final val Cache: LoadingCache[Key, Result] = Scaffeine().build((k: Key) => findModuleIdentifiers(k))

  import scala.concurrent.ExecutionContext.Implicits.global

  def invalidate(psiFile: PsiFile): Unit = {
    Cache.invalidate(Key(psiFile))
  }

  def refresh(psiFile: PsiFile): Unit = {
    Cache.refresh(Key(psiFile))
  }

  // Invalidate files which have imported this module
  def invalidate(moduleName: String): Unit = {
    val keys = Cache.asMap().filter { case (_, v) => v.exists(_.exists(_.exists(_.exists(_.moduleName == moduleName)))) }.keys
    Cache.invalidateAll(keys)
  }

  def invalidateAll(project: Project): Unit = {
    Cache.asMap().filter(_._1.psiFile.getProject == project).keys.foreach(Cache.invalidate)
  }

  def findAvailableModuleIdentifiers(psiFile: PsiFile): Iterable[ModuleIdentifier] = {
    if (ApplicationManager.getApplication.isReadAccessAllowed) {
      val moduleIdentifiers = Future(getModuleIdentifiers(psiFile))

      ScalaFutureUtil.waitWithCheckCancelled(psiFile.getProject, moduleIdentifiers, s"in findAvailableModuleIdentifiers to get all module identifiers for ${psiFile.getName}") match {
        case Some(mids) => mids.getOrElse(Iterable())
        case _ => Iterable()
      }
    } else {
      getModuleIdentifiers(psiFile).getOrElse(Iterable())
    }
  }

  private def getModuleIdentifiers(psiFile: PsiFile): Option[Iterable[ModuleIdentifier]] = {
    val key = Key(psiFile)
    Cache.get(key) match {
      case Some(mids) =>
        if (mids.toSeq.contains(None)) {
          Cache.invalidate(key)
        }
        Some(mids.flatten.flatten)
      case None =>
        Cache.invalidate(key)
        None
    }
  }

  private type ModuleIdentifiers = Iterable[Option[Iterable[ModuleIdentifier]]]

  private def findModuleIdentifiers(k: Key): Option[ModuleIdentifiers] = {
    val project = k.psiFile.getProject
    val psiFile = k.psiFile

    ApplicationUtil.runInReadActionWithWriteActionPriority(project, findImportDeclarations(psiFile), "In findModuleIdentifiers") match {
      case Right(importDeclarations) =>
        val noImplicitPrelude = if (HaskellProjectUtil.isSourceFile(psiFile)) {
          HaskellComponentsManager.findStackComponentInfo(psiFile).exists(info => isNoImplicitPreludeActive(info, psiFile))
        } else {
          // TODO: This can give unexpected behavior when finding references in library files
          false
        }
        val idsF1 = getModuleIdentifiersFromFullImportedModules(noImplicitPrelude, psiFile, importDeclarations)

        val idsF2 = getModuleIdentifiersFromHidingIdsImportedModules(psiFile, importDeclarations)

        val idsF3 = getModuleIdentifiersFromSpecIdsImportedModules(psiFile, importDeclarations)

        val f = for {
          f1 <- idsF1
          f2 <- idsF2
          f3 <- idsF3
        } yield (f1, f2, f3)

        try {
          val (x, y, z) = Await.result(f, 10.seconds)
          Some(x ++ y ++ z)
        } catch {
          case _: TimeoutException =>
            HaskellNotificationGroup.logInfoEvent(project, s"Timeout while find module identifiers for file ${k.psiFile.getName}")
            None
        }
      case Left(noInfo) =>
        HaskellNotificationGroup.logInfoEvent(project, s"Timeout while find import declarations in findModuleIdentifiers for file ${psiFile.getName}: ${noInfo.message}")
        None
    }
  }

  private def getModuleIdentifiersFromFullImportedModules(noImplicitPrelude: Boolean, psiFile: PsiFile, importDeclarations: Iterable[HaskellImportDeclaration]): Future[Iterable[Option[Iterable[ModuleIdentifier]]]] = {
    val importInfos = getFullImportedModules(noImplicitPrelude, psiFile, importDeclarations)

    Future.sequence(importInfos.map(importInfo => {
      val allModuleIdentifiers = HaskellComponentsManager.findModuleIdentifiers(psiFile.getProject, importInfo.moduleName)
      allModuleIdentifiers.map(mi => mi.map(i => createQualifiedModuleIdentifiers(importInfo, i)))
    }))
  }

  private def getModuleIdentifiersFromHidingIdsImportedModules(psiFile: PsiFile, importDeclarations: Iterable[HaskellImportDeclaration]): Future[Iterable[Option[Iterable[ModuleIdentifier]]]] = {
    val importInfos = getImportedModulesWithHidingIdsSpec(psiFile, importDeclarations)

    Future.sequence(importInfos.map(importInfo => {
      val allModuleIdentifiers = HaskellComponentsManager.findModuleIdentifiers(psiFile.getProject, importInfo.moduleName)
      allModuleIdentifiers.map(ids => ids.map(is => createQualifiedModuleIdentifiers(importInfo, is.filterNot(mi => importInfo.ids.exists(_ == mi.name)))))
    }))
  }

  private def getModuleIdentifiersFromSpecIdsImportedModules(psiFile: PsiFile, importDeclarations: Iterable[HaskellImportDeclaration]): Future[Iterable[Option[Iterable[ModuleIdentifier]]]] = {
    val importInfos = getImportedModulesWithSpecIds(psiFile, importDeclarations)

    Future.sequence(importInfos.map(importInfo => {
      val allModuleIdentifiers = HaskellComponentsManager.findModuleIdentifiers(psiFile.getProject, importInfo.moduleName)
      allModuleIdentifiers.map(ids => ids.map(is => createQualifiedModuleIdentifiers(importInfo, is.filter(mi => importInfo.ids.exists(id => if (mi.isOperator) s"(${mi.name})" == id else id == mi.name)))))
    }))
  }

  private sealed trait ImportInfo {
    def moduleName: String

    def qualified: Boolean

    def as: Option[String]
  }

  private case class ImportFull(moduleName: String, qualified: Boolean, as: Option[String]) extends ImportInfo

  private case class ImportWithHiding(moduleName: String, ids: Iterable[String], qualified: Boolean, as: Option[String]) extends ImportInfo

  private case class ImportWithIds(moduleName: String, ids: Iterable[String], qualified: Boolean, as: Option[String]) extends ImportInfo

  private def isNoImplicitPreludeActive(info: StackComponentInfo, psiFile: PsiFile): Boolean = {
    info.isImplicitPreludeActive || ApplicationUtil.runReadAction(HaskellPsiUtil.findLanguageExtensions(psiFile)).exists(p => ApplicationUtil.runReadAction(p.getText).contains("NoImplicitPrelude"))
  }

  private def getFullImportedModules(noImplicitPrelude: Boolean, psiFile: PsiFile, importDeclarations: Iterable[HaskellImportDeclaration]): Iterable[ImportFull] = {
    val moduleNames = for {
      id <- importDeclarations
      if Option(id.getImportSpec).isEmpty
      mn <- ApplicationUtil.runReadAction(id.getModuleName)
    } yield ImportFull(mn, Option(id.getImportQualified).isDefined, Option(id.getImportQualifiedAs).map(qa => ApplicationUtil.runReadAction(qa.getQualifier.getName)))

    if (moduleNames.map(_.moduleName).toSeq.contains(HaskellProjectUtil.Prelude) || noImplicitPrelude) {
      moduleNames
    } else {
      Iterable(ImportFull(HaskellProjectUtil.Prelude, qualified = false, None)) ++ moduleNames
    }
  }

  private def getImportedModulesWithHidingIdsSpec(psiFile: PsiFile, importDeclarations: Iterable[HaskellImportDeclaration]): Iterable[ImportWithHiding] = {
    for {
      importDeclaration <- importDeclarations.filter(i => Option(i.getImportSpec).flatMap(is => Option(is.getImportHidingSpec)).isDefined)
      importIdList = importDeclaration.getImportSpec.getImportHidingSpec.getImportIdList
      mn <- ApplicationUtil.runReadAction(importDeclaration.getModuleName)
    } yield ImportWithHiding(
      mn,
      findImportIds(importIdList),
      Option(importDeclaration.getImportQualified).isDefined,
      Option(importDeclaration.getImportQualifiedAs).map(_.getQualifier).map(q => ApplicationUtil.runReadAction(q.getName))
    )
  }

  private def getImportedModulesWithSpecIds(psiFile: PsiFile, importDeclarations: Iterable[HaskellImportDeclaration]): Iterable[ImportWithIds] = {
    for {
      importDeclaration <- importDeclarations.filter(i => Option(i.getImportSpec).flatMap(is => Option(is.getImportIdsSpec)).isDefined)
      importIdList = importDeclaration.getImportSpec.getImportIdsSpec.getImportIdList
      mn <- ApplicationUtil.runReadAction(importDeclaration.getModuleName)
    } yield ImportWithIds(
      mn,
      findImportIds(importIdList),
      Option(importDeclaration.getImportQualified).isDefined,
      Option(importDeclaration.getImportQualifiedAs).map(_.getQualifier).map(q => ApplicationUtil.runReadAction(q.getName))
    )
  }

  private def createQualifiedModuleIdentifiers(importInfo: ImportInfo, moduleIdentifiers: Iterable[ModuleIdentifier]): Iterable[ModuleIdentifier] = {
    moduleIdentifiers.flatMap(mi => {
      (importInfo.as, importInfo.qualified) match {
        case (None, false) => Iterable(mi, mi.copy(name = mi.moduleName + "." + mi.name))
        case (None, true) => Iterable(mi.copy(name = mi.moduleName + "." + mi.name))
        case (Some(q), false) => Iterable(mi, mi.copy(name = q + "." + mi.name))
        case (Some(q), true) => Iterable(mi.copy(name = q + "." + mi.name))
      }
    })
  }

  private def findImportIds(importIdList: util.List[HaskellImportId]): Iterable[String] = {
    importIdList.asScala.flatMap(importId => Iterable(ApplicationUtil.runReadAction(importId.getCname.getName)) ++ importId.getCnameDotDotList.asScala.flatMap(cndd => Option(cndd.getCname).map(cn => ApplicationUtil.runReadAction(cn.getName))))
  }
}
