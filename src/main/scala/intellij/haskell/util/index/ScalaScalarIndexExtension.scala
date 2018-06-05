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

import java.io.{DataInput, DataOutput}

import com.intellij.util.indexing.FileBasedIndexExtension
import com.intellij.util.io.DataExternalizer


/**
  * A specialization of FileBasedIndexExtension allowing to create a mapping [DataObject -> List of files containing this object]
  *
  */
object ScalaScalarIndexExtension {
  final val VoidDataExternalizer: DataExternalizer[Unit] = new ScalaScalarIndexExtension.UnitDataExternalizer

  private class UnitDataExternalizer extends DataExternalizer[Unit] {
    def save(out: DataOutput, value: Unit) {
    }

    def read(in: DataInput): Unit = {
      ()
    }
  }

}

abstract class ScalaScalarIndexExtension[K] extends FileBasedIndexExtension[K, Unit] {
  final def getValueExternalizer: DataExternalizer[Unit] = {
    ScalaScalarIndexExtension.VoidDataExternalizer
  }
}

