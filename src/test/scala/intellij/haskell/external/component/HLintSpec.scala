/*
 * Copyright 2014-2020 Rik van der Kleij
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

import org.scalatest.GivenWhenThen
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class HLintSpec extends AnyFunSpec with Matchers with GivenWhenThen {

  describe("HLint component") {
    it("should parse HLint output") {
      Given("output of HLint")
      val output =
        """
           [{"module":["Hello113"],"decl":["testa"],"severity":"Warning","hint":"Redundant bracket","file":"/testproject/src/Hello113.hs","startLine":7,"startColumn":9,"endLine":7,"endColumn":15,"from":"(\"aa\")","to":"\"aa\"","note":["redundant parens"],"refactorings":"[Replace {rtype = Expr, pos = SrcSpan {startLine = 7, startCol = 9, endLine = 7, endCol = 15}, subts = [(\"x\",SrcSpan {startLine = 7, startCol = 10, endLine = 7, endCol = 14})], orig = \"x\"}]"}
           ,{"module":["Hello113"],"decl":["yes"],"severity":"Warning","hint":"Use concatMap","file":"/testproject/src/Hello113.hs","startLine":27,"startColumn":19,"endLine":27,"endColumn":45,"from":"concat . map f . baz . bar","to":"concatMap f . baz . bar","note":[],"refactorings":"[Replace {rtype = Expr, pos = SrcSpan {startLine = 27, startCol = 19, endLine = 27, endCol = 45}, subts = [(\"f\",SrcSpan {startLine = 27, startCol = 32, endLine = 27, endCol = 33}),(\"x\",SrcSpan {startLine = 27, startCol = 36, endLine = 27, endCol = 45})], orig = \"concatMap f . x\"}]"}]
      """

      When("converted to list of hlint infos")
      val hlintInfos = HLintComponent.parseHLintOutput(null, output)

      Then("it should contain right info")

      hlintInfos should have size 2

      val hlintInfo1 = hlintInfos.head
      hlintInfo1.file shouldEqual "/testproject/src/Hello113.hs"
      hlintInfo1.startLine shouldEqual 7
      hlintInfo1.note.size shouldEqual 1
      hlintInfo1.note.head shouldEqual "redundant parens"
      hlintInfo1.refactorings shouldEqual "[Replace {rtype = Expr, pos = SrcSpan {startLine = 7, startCol = 9, endLine = 7, endCol = 15}, subts = [(\"x\",SrcSpan {startLine = 7, startCol = 10, endLine = 7, endCol = 14})], orig = \"x\"}]"

      val hlintInfo2 = hlintInfos(1)
      hlintInfo2.file shouldEqual "/testproject/src/Hello113.hs"
      hlintInfo2.startLine shouldEqual 27
      hlintInfo2.endLine shouldEqual 27
      hlintInfo2.refactorings shouldEqual "[Replace {rtype = Expr, pos = SrcSpan {startLine = 27, startCol = 19, endLine = 27, endCol = 45}, subts = [(\"f\",SrcSpan {startLine = 27, startCol = 32, endLine = 27, endCol = 33}),(\"x\",SrcSpan {startLine = 27, startCol = 36, endLine = 27, endCol = 45})], orig = \"concatMap f . x\"}]"
    }
  }
}
