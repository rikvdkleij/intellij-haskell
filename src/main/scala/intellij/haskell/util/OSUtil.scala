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

package intellij.haskell.util

object OSUtil {

  final lazy val LineSeparator = System.lineSeparator()

  def isOSX: Boolean = {
    OsName.indexOf("mac") >= 0 || OsName.indexOf("darwin") >= 0
  }

  def isWindows: Boolean = {
    OsName.startsWith("windows")
  }

  private final val OsName = System.getProperty("os.name").toLowerCase
}
