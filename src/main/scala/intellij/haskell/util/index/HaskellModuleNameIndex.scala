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

package intellij.haskell.util.index

import java.util.Collections

import com.github.blemale.scaffeine.{LoadingCache, Scaffeine}
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.{IndexNotReadyException, Project}
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.util.indexing._
import com.intellij.util.io.{EnumeratorStringDescriptor, KeyDescriptor}
import intellij.haskell.HaskellFileType
import intellij.haskell.external.component._
import intellij.haskell.psi.HaskellPsiUtil
import intellij.haskell.util.{ApplicationUtil, HaskellFileUtil, HaskellProjectUtil}

import scala.collection.JavaConverters._
import scala.concurrent.duration.{FiniteDuration, _}

/**
  * Notice that Haskell modules in libraries can be found which are not exposed
  */
object HaskellModuleNameIndex {
  private val HaskellModuleNameIndex: ID[String, Unit] = ID.create("HaskellModuleNameIndex")
  private val IndexVersion = 1
  private val KeyDescriptor = new EnumeratorStringDescriptor

  private val HaskellFileFilter = new FileBasedIndex.InputFilter() {

    override def acceptInput(file: VirtualFile): Boolean = {
      file.getFileType == HaskellFileType.Instance
    }
  }

  private case class Key(project: Project, moduleName: String)

  type Result = Either[NoInfo, Seq[PsiFile]]

  private final val Cache: LoadingCache[Key, Result] = Scaffeine().build((k: Key) => find(k, 2.second, reschedule = false))

  private def find(key: Key, timeout: FiniteDuration, reschedule: Boolean): Either[NoInfo, Seq[PsiFile]] = {
    findFiles(key.project, key.moduleName, timeout, reschedule) match {
      case Right(files) =>
        if (ApplicationManager.getApplication.isReadAccessAllowed) {
          Right(files.flatMap(vf => HaskellFileUtil.convertToHaskellFileDispatchThread(key.project, vf)))
        } else {
          val result = files.map(vf => HaskellFileUtil.convertToHaskellFileInReadAction(key.project, vf))
          if (result.exists(_.isLeft)) {
            Left(ReadActionTimeout("Read action timeout while converting virtual file to psi file"))
          } else {
            Right(result.flatMap(_.toOption).flatten)
          }
        }
      case Left(noInfo) => Left(noInfo)
    }
  }

  def fillCache(project: Project, moduleNames: Iterable[String]): Unit = {
    moduleNames.foreach(mn => {
      val key = Key(project, mn)
      find(key, 2.seconds, reschedule = true) match {
        case Right(vf) => Cache.put(key, Right(vf))
        case Left(_) => ()
      }
    })
  }

  // IntelliJ tends to send a lot of the same requests from HaskellReference to find a module name.
  // This makes the UI unresponsive if the module name can not be found because user is not finished with typing the module name.
  // So it seems to be no good solution to do the searching in UI thread because cache can not set before new request comes in.
  // So using Cache is solution because Cache.get blocks next request for same key while busy.
  def findFileByModuleName(project: Project, moduleName: String): Either[NoInfo, Seq[PsiFile]] = {
    val key = Key(project, moduleName)
    Cache.getIfPresent(key) match {
      case Some(r@Right(_)) => r
      case _ =>
        //        if (ApplicationManager.getApplication.isReadAccessAllowed) {
        //          find(key, 1.second, reschedule = false) match {
        //            case r@Right(_) =>
        //              Cache.put(key, r)
        //              r
        //            case Left(noInfo) =>
        //              Cache.invalidate(key)
        //              Left(noInfo)
        //          }
        //        } else {
        Cache.get(key) match {
          case r@Right(_) => r
          case Left(noInfo) =>
            // No invalidate to prevent UI becomes unresponsive after many calls for same module name which does not exists
            // In LoadComponent the "not found" entries will be invalidated eventually
            Left(noInfo)
        }
      //        }
    }
  }

  def invalidateNotFoundEntries(project: Project): Unit = {
    val keys = Cache.asMap().filter { case (k, v) => k.project == project && v.isLeft }.keys
    Cache.invalidateAll(keys)
  }

  def invalidate(project: Project): Unit = {
    val keys = Cache.asMap().keys.filter(_.project == project)
    keys.foreach(Cache.invalidate)
  }

  def invalidateModuleName(project: Project, moduleName: String): Unit = {
    Cache.invalidate(Key(project, moduleName))
  }

  private def findFiles(project: Project, moduleName: String, timeout: FiniteDuration, reschedule: Boolean): Either[NoInfo, Seq[VirtualFile]] = {
    if (moduleName == HaskellProjectUtil.Prelude) {
      Right(Seq())
    } else {
      val files = ApplicationUtil.scheduleInReadActionWithWriteActionPriority(
        project, {
          try {
            Some(FileBasedIndex.getInstance.getContainingFiles(HaskellModuleNameIndex, moduleName, HaskellProjectUtil.getProjectAndLibrariesModulesSearchScope(project)).asScala.toSeq)
          } catch {
            case _: IndexNotReadyException => None
          }
        },
        s"Find file for module $moduleName by index",
        timeout, reschedule = reschedule
      )

      files match {
        case Left(noInfo) => Left(noInfo)
        case Right(Some(f)) =>
          val firstFile = f.find(f => HaskellProjectUtil.isSourceFile(project, f)).orElse(f.headOption)
          Right(firstFile.toSeq ++ firstFile.map(pf => f.filterNot(_ == pf)).getOrElse(Seq()))
        case Right(None) => Left(IndexNotReady)
      }

    }
  }
}

class HaskellModuleNameIndex extends ScalaScalarIndexExtension[String] {

  private val haskellModuleNameIndexer = new HaskellModuleNameIndexer

  override def getIndexer: DataIndexer[String, Unit, FileContent] = haskellModuleNameIndexer

  override def getName: ID[String, Unit] = HaskellModuleNameIndex.HaskellModuleNameIndex

  override def getKeyDescriptor: KeyDescriptor[String] = HaskellModuleNameIndex.KeyDescriptor

  override def getInputFilter: FileBasedIndex.InputFilter = HaskellModuleNameIndex.HaskellFileFilter

  override def dependsOnFileContent: Boolean = true

  override def getVersion: Int = HaskellModuleNameIndex.IndexVersion

  class HaskellModuleNameIndexer extends DataIndexer[String, Unit, FileContent] {

    override def map(inputData: FileContent): java.util.Map[String, Unit] = {
      val psiFile = inputData.getPsiFile
      HaskellPsiUtil.findModuleNameInPsiTree(psiFile) match {
        case Some(n) => Collections.singletonMap(n, ())
        case _ => Collections.emptyMap()
      }
    }
  }

}
