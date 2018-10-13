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

import intellij.haskell.util.StringUtil

object DeclarationLineUtil {

  def findName(declarationLine: String): Option[NameAndShortDeclaration] = {
    val declaration = StringUtil.shortenHaskellDeclaration(declarationLine)
    val allTokens = declaration.split("""\s+""")
    val name = if (allTokens.isEmpty || allTokens(0) == "--") {
      None
    } else if (Seq("class", "instance").contains(allTokens(0))) {
      declaration.split("""where|=\s""").headOption.flatMap { d =>
        val tokens = d.trim.split("""=>""")
        if (tokens.size == 1) {
          Option(allTokens(1))
        } else {
          Option(tokens.last.trim.split("""\s+""")(0))
        }
      }
    } else if (allTokens(0) == "type" && allTokens(1) == "role") {
      Option(allTokens(2))
    } else if (Seq("data", "type", "newtype").contains(allTokens(0).trim)) {
      Option(allTokens(1))
    } else {
      val tokens = declaration.split("""::""")
      if (tokens.size > 1) {
        val name = tokens(0).trim
        Option(name)
      } else {
        None
      }
    }
    name.map(n => NameAndShortDeclaration(n, declaration))
  }

}

case class NameAndShortDeclaration(name: String, declaration: String)
