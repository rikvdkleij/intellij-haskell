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

import org.scalatest.{FunSpec, GivenWhenThen, Matchers}

class HLintSpec extends FunSpec with Matchers with GivenWhenThen {

  describe("HLint component") {
    it("should parse HLint output") {
      Given("output of HLint")
      val output =
        """
      [{"module":["SayHello"],"decl":["sayHello"],"severity":"Error","hint":"Redundant if","file":"src/Effe.hs","startLine":17,"startColumn":11,"endLine":17,"endColumn":44,"from":"if True then getLine else getLine","to":"getLine","note":["\"increases laziness\""]}
      ,{"module":["SayHello"],"decl":["x"],"severity":"Error","hint":"Redundant if","file":"src/Effe.hs","startLine":34,"startColumn":5,"endLine":34,"endColumn":33,"from":"if True then True else False","to":"True","note":[]}]
      """

      When("converted to list of hlint infos")
      val hlintInfos = HLintComponent.deserializeHLintInfo(null, output)

      Then("it should contain right info")

      hlintInfos should have size 2

      val hlintInfo1 = hlintInfos.head
      hlintInfo1.startLine shouldEqual 17
      hlintInfo1.note.size shouldEqual 1
      hlintInfo1.note.head shouldEqual "\"increases laziness\""

      val hlintInfo2 = hlintInfos(1)
      hlintInfo2.endLine shouldEqual 34
      hlintInfo2.to shouldEqual Some("True")
      hlintInfo2.note should have size 0
    }
  }
}
