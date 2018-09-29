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
import com.intellij.openapi.project.{IndexNotReadyException, Project}
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.psi.search.GlobalSearchScope
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

  type Result = Either[NoInfo, Option[PsiFile]]

  // This should be a synchronous cache because in case caller is on dispatch thread
  private final val Cache: LoadingCache[Key, Result] = Scaffeine().build((k: Key) => find(k, ApplicationUtil.ScheduleInReadActionTimeout))

  private def find(key: Key, timeout: FiniteDuration): Either[NoInfo, Option[PsiFile]] = {
    findFile(key.project, key.moduleName, timeout) match {
      case Right(vfs) => vfs match {
        case Some(vf) => HaskellFileUtil.convertToHaskellFileInReadAction(key.project, vf)
        case None => Right(None)
      }
      case Left(noInfo) => Left(noInfo)
    }
  }

  def fillCache(project: Project, moduleNames: Iterable[String]): Unit = {
    moduleNames.foreach(mn => {
      val key = Key(project, mn)
      find(key, 5.seconds) match {
        case Right(vf) => Cache.put(key, Right(vf))
        case Left(noInfo) => Left(noInfo)
      }
    })
  }

  def findFileByModuleName(project: Project, moduleName: String): Either[NoInfo, Option[PsiFile]] = {
    val key = Key(project, moduleName)
    Cache.get(key) match {
      case r@Right(_) => r
      case l@Left(NoInfoAvailable(_, _)) => l
      case noInfo =>
        Cache.invalidate(key)
        noInfo
    }
  }

  private def findFile(project: Project, moduleName: String, timeout: FiniteDuration): Either[NoInfo, Option[VirtualFile]] = {
    if (moduleName == HaskellProjectUtil.Prelude) {
      Right(None)
    } else {
      val result = ApplicationUtil.scheduleInReadActionWithWriteActionPriority(
        project, {
          try {
            Right(FileBasedIndex.getInstance.getContainingFiles(HaskellModuleNameIndex, moduleName, GlobalSearchScope.allScope(project)).asScala.headOption)
          } catch {
            case _: IndexNotReadyException => Left(IndexNotReady)
          }
        },
        s"finding file for module $moduleName by index",
        timeout
      )
      for {
        r <- result
        vf <- r
      } yield vf
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
