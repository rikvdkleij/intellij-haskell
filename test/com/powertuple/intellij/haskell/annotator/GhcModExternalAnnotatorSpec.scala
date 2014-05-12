package com.powertuple.intellij.haskell.annotator

import org.scalatest.{BeforeAndAfterEach, GivenWhenThen, Matchers, FunSpec}
import com.intellij.execution.process.ProcessOutput

class GhcModExternalAnnotatorSpec extends FunSpec with Matchers with GivenWhenThen with BeforeAndAfterEach {

  val ghcModExternalAnnotator = new GhcModExternalAnnotator

  describe("Parsing ghc-mod output") {
    Given("output of ghc-mod")
    val output = new ProcessOutput()
    output.appendStdout("file/path/HaskellFile.hs:1:11:parse error on input\n")
    output.appendStdout("file/path/HaskellFile.hs:12:5:another parse error on input")

    When("parsed to problem list")
    val problems = ghcModExternalAnnotator.parseGhcModOutput(output)

    Then("list should have size 2")
    problems should have size 2
    val problem1 = problems(0)
    val problem2 = problems(1)

    And("contain right data")
    problem1.lineNr should equal(1)
    problem1.columnNr should equal(11)
    problem1.description should equal("parse error on input")

    problem2.lineNr should equal(12)
    problem2.columnNr should equal(5)
    problem2.description should equal("another parse error on input")
  }

  describe("Determine annotation offset when compile error") {
    Given("some Haskell code which gives compile errors")
    val someCode =
      """
        |Some Haskell code
        |which does not
        |
        |com pile
      """.stripMargin

    When("ghc-mod is executed")
    val ghcModResult = GhcModResult(Seq(GhcModProblem(4, 3, "something wrong")))
    val annotations = ghcModExternalAnnotator.createAnnotations(ghcModResult, someCode)

    Then("annotation holder should contain right annotation")
    annotations should have length 1

    val annotation = annotations(0)
    annotation.asInstanceOf[ErrorAnnotation].textRange.getStartOffset should equal(36)
  }

  describe("Determine annotation offset when compile warning") {
    Given("some Haskell code which gives compile warnings")
    val someCode =
      """
        |some code
      """.stripMargin

    When("ghc-mod is executed")
    val ghcModResult = GhcModResult(Seq(GhcModProblem(1, 1, "Warning: some warning")))
    val annotations = ghcModExternalAnnotator.createAnnotations(ghcModResult, someCode)

    Then("annotation holder should contain right annotation")
    annotations should have length 1

    val annotation = annotations(0)
    annotation.asInstanceOf[WarningAnnotation].textRange.getStartOffset should equal(0)
  }
}
