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
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.powertuple.intellij.haskell

import com.intellij.lang.refactoring.NamesValidator
import com.intellij.openapi.project.Project

class HaskellNamesValidator extends NamesValidator {

  override def isKeyword(name: String, project: Project): Boolean = {
    val lexer = new HaskellLexer
    lexer.start(name.toCharArray)
    HaskellParserDefinition.RESERVED_IDS.contains(lexer.getTokenType)
  }

  override def isIdentifier(name: String, project: Project): Boolean = {
    // TODO: Also check no reserved op and dashes
    !isKeyword(name, project)
  }
}
