package intellij.haskell.external.component

import fastparse.Parsed.{Failure, Success}
import fastparse.SingleLineWhitespace._
import fastparse._

object HLintRefactoringsParser {

  case class SrcSpan(startLine: Int, startCol: Int, endLine: Int, endCol: Int)

  type Subts = Seq[(String, SrcSpan)]

  sealed trait Refactoring
  case class Delete(rType: RType, pos: SrcSpan) extends Refactoring
  case class Replace(rType: RType, pos: SrcSpan, subts: Subts, orig: String, deletes: Seq[Delete]) extends Refactoring
  case class ModifyComment(pos: SrcSpan, newComment: String) extends Refactoring
  case class InsertComment(pos: SrcSpan, insertComment: String) extends Refactoring
  case class RemoveAsKeyword(pos: SrcSpan) extends Refactoring

  sealed trait RType
  case object Expr extends RType
  case object Decl extends RType
  case object Pattern extends RType
  case object Stmt extends RType
  case object Type extends RType
  case object ModuleName extends RType
  case object Bind extends RType
  case object Match extends RType
  case object Import extends RType

  def parseRefactoring(hlintOutput: String): Either[String, Refactoring] = parse(hlintOutput, refactoringParser(_), verboseFailures = true) match {
    case Success(value, _) => Right(value)
    case Failure(label, i, _) => Left(s"Could not parse HLint output | HLintOutput: $hlintOutput | Label: $label | Index: $i")
  }

  private def refactoringParser[_: P]: P[Refactoring] = P("[" ~ (deleteParser | replaceParser | modifyCommentParser | insertCommentParser | removeAsKeywordParser) ~ "]")

  private[component] def parseSubts(hlintOutput: String): Parsed[Subts] = parse(hlintOutput, subtsParser(_), verboseFailures = true)

  private[component] def parsePos(hlintOutput: String): Parsed[SrcSpan] = parse(hlintOutput, posParser(_), verboseFailures = true)

  private def deleteParser[_: P]: P[Delete] = P("Delete" ~ keyRtypePosParser(Pass)).map({ case (x, y, _) => Delete(x, y) })

  private def replaceParser[_: P]: P[Replace] = P("Replace" ~ keyRtypePosParser(commaParser ~ "subts =" ~ subtsParser ~ commaParser ~ keyValueParser("orig", string)) ~ (commaParser ~ deleteParser).rep)
    .map({ case (x, y, (w, z), q) => Replace(x, y, w, z, q) })

  private def modifyCommentParser[_: P]: P[ModifyComment] = P("ModifyComment" ~ "{" ~ posParser ~ commaParser ~ keyValueParser("newComment", string) ~ "}").
    map({ case (x, y) => ModifyComment(x, y) })

  private def insertCommentParser[_: P]: P[InsertComment] = P("InsertComment" ~ "{" ~ posParser ~ commaParser ~ keyValueParser("newComment", string) ~ "}").
    map({ case (x, y) => InsertComment(x, y) })

  private def removeAsKeywordParser[_: P]: P[RemoveAsKeyword] = P("RemoveAsKeyword" ~ "{" ~ posParser ~ "}").
    map({ case (x) => RemoveAsKeyword(x) })

  private def keyRtypePosParser[_: P, A](rest: => P[A]) = "{" ~ keyRtypeParser ~ commaParser ~ posParser ~ rest ~ "}"

  private def subtsParser[_: P] = P("[" ~ (subtParser ~ commaParser.?).rep ~ "]")

  private def subtParser[_: P] = P("(" ~ string ~ commaParser ~ srcSpanParser ~ ")")

  private def rtypeParser[_: P]: P[RType] = {
    P(IgnoreCase("Expr")).map(_ => Expr) |
      P(IgnoreCase("Decl")).map(_ => Decl) |
      P(IgnoreCase("Type")).map(_ => Type) |
      P(IgnoreCase("Pattern")).map(_ => Pattern) |
      P(IgnoreCase("Stmt")).map(_ => Stmt) |
      P(IgnoreCase("ModuleName")).map(_ => ModuleName) |
      P(IgnoreCase("Bind")).map(_ => Bind) |
      P(IgnoreCase("Match")).map(_ => Match) |
      P(IgnoreCase("Import")).map(_ => Import)
  }

  private def stringChars(c: Char) = c != '\"' && c != '\\'

  private def strChars[_: P] = P(CharsWhile(stringChars))

  private def hexDigit[_: P] = P(CharIn("0-9a-fA-F"))

  private def unicodeEscape[_: P] = P("u" ~ hexDigit ~ hexDigit ~ hexDigit ~ hexDigit)

  private def escape[_: P] = P("\\" ~ (CharIn("\"/\\\\bfnrt") | unicodeEscape))

  private def string[_: P] = P("\"" ~/ (strChars | escape).rep.! ~ "\"")

  private def digits[_: P] = P(CharsWhileIn("0-9"))

  private def keyValueParser[_: P, A](keyName: String, valueParser: => P[A]) = s"$keyName" ~ "=" ~ valueParser

  private def keyDigitsParser[_: P](keyName: String) = keyValueParser[P[String], String](keyName, digits.!).map(_.toInt)

  private def keyRtypeParser[_: P] = keyValueParser("rtype", rtypeParser)

  private def commaParser[_: P] = ","

  private def posParser[_: P] = "pos" ~ "=" ~ srcSpanParser

  private def srcSpanParser[_: P] = "SrcSpan" ~
    ("{" ~
      keyDigitsParser("startLine") ~ commaParser ~
      keyDigitsParser("startCol") ~ commaParser ~
      keyDigitsParser("endLine") ~ commaParser ~
      keyDigitsParser("endCol") ~
      "}").map { case (sl, sc, el, ec) => SrcSpan(sl, sc, el, ec) }
}
