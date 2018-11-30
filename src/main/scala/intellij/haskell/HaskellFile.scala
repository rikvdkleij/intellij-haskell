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

package intellij.haskell

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.openapi.fileTypes.{FileType, FileTypeConsumer, FileTypeFactory, LanguageFileType}
import com.intellij.psi.FileViewProvider
import icons.HaskellIcons
import javax.swing._
import org.jetbrains.annotations.NotNull

class HaskellFile(viewProvider: FileViewProvider) extends PsiFileBase(viewProvider, HaskellLanguage.Instance) {

  @NotNull
  def getFileType: FileType = {
    HaskellFileType.Instance
  }

  override def toString: String = {
    "Haskell file"
  }

  override def getIcon(flags: Int): Icon = {
    super.getIcon(flags)
  }
}

object HaskellFileType {
  final val Instance: HaskellFileType = new HaskellFileType

  final val HaskellFileExtension = "hs"
}

class HaskellFileType extends LanguageFileType(HaskellLanguage.Instance) {

  def getName: String = {
    "Haskell file"
  }

  def getDescription: String = {
    "Haskell language file"
  }

  def getDefaultExtension: String = {
    HaskellFileType.HaskellFileExtension
  }

  def getIcon: Icon = {
    HaskellIcons.HaskellFileLogo
  }
}

class HaskellLanguageFileTypeFactory extends FileTypeFactory {
  def createFileTypes(consumer: FileTypeConsumer) {
    consumer.consume(HaskellFileType.Instance, HaskellFileType.Instance.getDefaultExtension)
  }
}