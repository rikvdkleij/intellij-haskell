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

import com.intellij.xml.util.XmlStringUtil
import intellij.haskell.external.component.DeclarationLineUtil

object StringUtil {

  private final val PackageQualifierPattern = """([a-z\-]+\-[\.0-9]+\:)?([A-Z][A-Za-z\-\']+\.)+"""

  def toLowerCase(s: String): String = {
    s.trim.toLowerCase
  }

  def escapeString(s: String): String = {
    XmlStringUtil.escapeString(s, false, false)
  }

  /**
    * Because of enabling DuplicateRecordFields accessor functions contain `$sel:`.
    */
  def shortenHaskellDeclaration(declaration: String): String = {
    removeCommentsAndWhiteSpaces(declaration.replace(".$sel:", ".").replaceAll(PackageQualifierPattern, ""))
  }

  def removeCommentsAndWhiteSpaces(code: String): String = {
    code.replaceAll("""\{\-[^\}]+\-\}""", " ").replaceAll("""\-\-.*""", " ").replaceAll("""\s+""", " ")
  }

  def removeOuterParens(name: String): String = {
    if (DeclarationLineUtil.isOperator(name)) {
      name.substring(1, name.length - 1)
    } else {
      name
    }
  }
}
