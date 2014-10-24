package com.powertuple.intellij.haskell.external

import org.scalatest.{FunSpec, GivenWhenThen, Matchers}

class HlintSpec extends FunSpec with Matchers with GivenWhenThen {

  describe("Parsing hlint output") {
    Given("type info output of ghc-modi")
    val output =
      """
      [{"module":"SayHello","decl":"sayHello","severity":"Error","hint":"Redundant if","file":"src/Effe.hs","startLine":17,"startColumn":11,"endLine":17,"endColumn":44,"from":"if True then getLine else getLine","to":"getLine","note":["\"increases laziness\""]}
      ,{"module":"SayHello","decl":"x","severity":"Error","hint":"Redundant if","file":"src/Effe.hs","startLine":34,"startColumn":5,"endLine":34,"endColumn":33,"from":"if True then True else False","to":"True","note":[]}]
      """

    When("converted to list of type info")
    val hlintInfos = Hlint.deserializeHlintInfo(output)

    Then("it should contain right info")

    hlintInfos should have size 2
    val hlintInfo1 = hlintInfos(0)
    hlintInfo1.startLine shouldEqual 17
    hlintInfo1.note.size shouldEqual 1
    hlintInfo1.note(0) shouldEqual "\"increases laziness\""

    val hlintInfo2 = hlintInfos(1)
    hlintInfo2.endLine shouldEqual 34
    hlintInfo2.to shouldEqual "True"
    hlintInfo2.note should have size 0
  }
}
