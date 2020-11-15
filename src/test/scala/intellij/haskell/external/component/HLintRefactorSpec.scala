package intellij.haskell.external.component

import intellij.haskell.external.component.HLintRefactoringsParser._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers


class HLintRefactorSpec extends AnyFlatSpec with Matchers {

  val delete = "[Delete {rtype = Import, pos = SrcSpan {startLine = 5, startCol = 1, endLine = 5, endCol = 14}}]"
  val pos = "pos = SrcSpan {startLine = 5, startCol = 1, endLine = 5, endCol = 14}"

  val replace = "[Replace {rtype = Expr, pos = SrcSpan {startLine = 1, startCol = 19, endLine = 1, endCol = 45}, " +
    "subts = [(\"f\",SrcSpan {startLine = 1, startCol = 32, endLine = 1, endCol = 33})," +
    "(\"x\",SrcSpan {startLine = 1, startCol = 36, endLine = 1, endCol = 45})], " +
    "orig = \"concatMap f . x\"}])]"

  val subts = "[(\"f\",SrcSpan {startLine = 1, startCol = 32, endLine = 1, endCol = 33}),(\"x\",SrcSpan {startLine = 1, startCol = 36, endLine = 1, endCol = 45})]"

  behavior of "HLint refactor parser"

  "pos parser" should "pass" in {
    parsePos(pos).get.value shouldEqual SrcSpan(5, 1, 5, 14)
  }

  "delete parser" should "pass" in {
    parseDeleteRefactoring(delete).get.value shouldEqual Delete(Import, SrcSpan(5, 1, 5, 14))
  }

  "subts parser" should "pass" in {
    parseSubts(subts).get.value shouldEqual List(("f", SrcSpan(1, 32, 1, 33)), ("x", SrcSpan(1, 36, 1, 45)))
  }

  "replace parser" should "pass" in {
    parseReplaceRefactoring(replace).get.value shouldEqual Replace(Expr, SrcSpan(1, 19, 1, 45), List(("f", SrcSpan(1, 32, 1, 33)), ("x", SrcSpan(1, 36, 1, 45))), "concatMap f . x")
  }

}