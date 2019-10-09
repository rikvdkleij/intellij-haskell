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

import intellij.haskell.util.StringUtil

object DeclarationUtil {

  def getDeclarationInfo(declarationLine: String, containsQualifiedIds: Boolean): Option[DeclarationInfo] = {
    val declaration = StringUtil.removeCommentsAndWhiteSpaces(declarationLine)
    val allTokens = declaration.split("""\s+""")
    (if (allTokens.isEmpty || allTokens(0) == "--") {
      None
    } else if (Seq("class", "instance").contains(allTokens(0))) {
      declaration.split("""where|=\s""").headOption.flatMap { d =>
        val tokens = d.trim.split("=>")
        val size = tokens.size
        if (size == 1) {
          Option(tokens(0))
        } else if (size > 1) {
          Option(tokens.last.trim.split("""\s+""")(0))
        } else {
          None
        }
      }
    } else if (allTokens(0) == "type" && allTokens(1) == "role") {
      Option(allTokens(2))
    } else if (Seq("data", "type", "newtype").contains(allTokens(0).trim)) {
      Option(allTokens(1))
    } else {
      val tokens = declaration.split("::")
      if (tokens.size > 1) {
        val name = tokens(0).trim
        Option(name)
      } else {
        None
      }
    }).map(name => {
      val operator = StringUtil.isWithinParens(name)
      val id = if (operator) {
        StringUtil.removeOuterParens(name)
      } else {
        name
      }
      if (containsQualifiedIds) {
        DeclarationInfo(StringUtil.removePackageModuleQualifier(id), Some(id), StringUtil.removePackageModuleQualifier(declaration), operator)
      } else {
        DeclarationInfo(id, None, declaration, operator)
      }
    })
  }

  case class DeclarationInfo(id: String, qualifiedId: Option[String], declarationLine: String, operator: Boolean)

}

