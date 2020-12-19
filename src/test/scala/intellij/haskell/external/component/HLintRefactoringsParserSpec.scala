package intellij.haskell.external.component

import intellij.haskell.external.component.HLintRefactoringsParser._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class HLintRefactoringsParserSpec extends AnyFlatSpec with Matchers {

  val delete = "[Delete {rtype = Import, pos = SrcSpan {startLine = 5, startCol = 1, endLine = 5, endCol = 14}}]"
  val pos = "pos = SrcSpan {startLine = 5, startCol = 1, endLine = 5, endCol = 14}"

  val replace = "[Replace {rtype = Expr, pos = SrcSpan {startLine = 1, startCol = 19, endLine = 1, endCol = 45}, " +
    "subts = [(\"f\",SrcSpan {startLine = 1, startCol = 32, endLine = 1, endCol = 33})," +
    "(\"x\",SrcSpan {startLine = 1, startCol = 36, endLine = 1, endCol = 45})], " +
    "orig = \"concatMap f . x\"}])]"

  val subts = "[(\"f\",SrcSpan {startLine = 1, startCol = 32, endLine = 1, endCol = 33}),(\"x\",SrcSpan {startLine = 1, startCol = 36, endLine = 1, endCol = 45})]"

  val modifyComment = "[ModifyComment {pos = SrcSpan {startLine = 1, startCol = 1, endLine = 1, endCol = 19}, newComment = \"{-# INLINE[~k] f #-}\"}]"

  val insertComment = "[InsertComment {pos = SrcSpan {startLine = 1, startCol = 1, endLine = 1, endCol = 40}, newComment = \"{-# NOINLINE slaves #-}\"}]"

  val removeAsKeyword = "[RemoveAsKeyword {pos = SrcSpan {startLine = 1, startCol = 1, endLine = 1, endCol = 24}}]"

  behavior of "HLint refactor parser"

  "pos parser" should "pass" in {
    parsePos(pos).get.value shouldEqual SrcSpan(5, 1, 5, 14)
  }

  "delete parser" should "pass" in {
    parseRefactoring(delete) should matchPattern {
      case Right(Delete(Import, SrcSpan(5, 1, 5, 14))) =>
    }
  }

  "subts parser" should "pass" in {
    parseSubts(subts).get.value shouldEqual List(("f", SrcSpan(1, 32, 1, 33)), ("x", SrcSpan(1, 36, 1, 45)))
  }

  "replace parser" should "pass" in {
    parseRefactoring(replace) should matchPattern {
      case Right(Replace(Expr, SrcSpan(1, 19, 1, 45), List(("f", SrcSpan(1, 32, 1, 33)), ("x", SrcSpan(1, 36, 1, 45))), "concatMap f . x")) =>
    }
  }

  "modify comment parser" should "pass" in {
    parseRefactoring(modifyComment) should matchPattern {
      case Right(ModifyComment(SrcSpan(1, 1, 1, 19), "{-# INLINE[~k] f #-}")) =>
    }
  }

  "insert comment parser" should "pas" in {
    parseRefactoring(insertComment) should matchPattern {
      case Right(InsertComment(SrcSpan(1, 1, 1, 40), "{-# NOINLINE slaves #-}")) =>
    }
  }

  "remove as keyword parser" should "pass" in {
    parseRefactoring(removeAsKeyword) should matchPattern {
      case Right(RemoveAsKeyword(SrcSpan(1, 1, 1, 24))) =>
    }
  }
}