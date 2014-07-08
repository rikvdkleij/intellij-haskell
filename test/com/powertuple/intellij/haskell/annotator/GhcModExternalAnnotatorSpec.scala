/*
 * Copyright 2014 Rik van der Kleij

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

package com.powertuple.intellij.haskell.annotator

import org.scalatest.{BeforeAndAfterEach, FunSpec, GivenWhenThen, Matchers}

class GhcModExternalAnnotatorSpec extends FunSpec with Matchers with GivenWhenThen with BeforeAndAfterEach {

  val ghcModiExternalAnnotator = new GhcModiExternalAnnotator

  describe("Parsing ghc-modi output") {
    Given("output of ghc-modi")
    val output = "file/path/HaskellFile.hs:1:11:parse error on input"

    When("parsed to ghc-modi problem")
    val ghcModiProblem = ghcModiExternalAnnotator.parseGhcModiOutputLine(output)

    Then("it should contain right data")
    ghcModiProblem.lineNr should equal(1)
    ghcModiProblem.columnNr should equal(11)
    ghcModiProblem.description should equal("parse error on input")
  }

  describe("Determine annotation offset when compile error") {
    Given("some Haskell code which gives compile errors")
    val someCode =
      """
        |Some Haskell code
        |  which does not
        |
        |com pile
      """.stripMargin

    When("ghc-modi is called")
    val ghcModiResult = GhcModiResult(Seq(GhcModiProblem(4, 3, "something wrong")))
    val annotations = ghcModiExternalAnnotator.createAnnotations(ghcModiResult, someCode)

    Then("annotation holder should contain right annotation")
    annotations should have length 1

    val annotation = annotations(0)
    annotation.asInstanceOf[ErrorAnnotation].textRange.getStartOffset should equal(38)
  }

  describe("Determine annotation offset when compile warning") {
    Given("some Haskell code which gives compile warnings")
    val someCode =
      """
        |some code
      """.stripMargin

    When("ghc-modi is executed")
    val ghcModiResult = GhcModiResult(Seq(GhcModiProblem(1, 1, "Warning: some warning")))
    val annotations = ghcModiExternalAnnotator.createAnnotations(ghcModiResult, someCode)

    Then("annotation holder should contain right annotation")
    annotations should have length 1

    val annotation = annotations(0)
    annotation.asInstanceOf[WarningAnnotation].textRange.getStartOffset should equal(0)
  }
}
