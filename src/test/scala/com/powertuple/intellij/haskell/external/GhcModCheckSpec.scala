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
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.powertuple.intellij.haskell.external

import com.powertuple.intellij.haskell.util.OSUtil
import org.scalatest.{BeforeAndAfterEach, FunSpec, GivenWhenThen, Matchers}

class GhcModCheckSpec extends FunSpec with Matchers with GivenWhenThen with BeforeAndAfterEach {

  describe("Parsing ghc-mod check output") {
    Given("output of ghc-mod check")
    val output = "/file/path/HaskellFile.hs:1:11:parse error on input\u0000     and so on"

    When("parsed to ghc-mod problem")
    val ghcModProblem = GhcModCheck.parseOutputLine(output, null).get

    Then("it should contain right data")
    ghcModProblem.lineNr should equal(1)
    ghcModProblem.columnNr should equal(11)
    ghcModProblem.message should equal(s"parse error on input${OSUtil.LineSeparator}     and so on")
  }
}
