package com.powertuple.intellij.haskell;
import com.intellij.lexer.*;
import com.intellij.psi.tree.IElementType;
import static com.powertuple.intellij.haskell.psi.HaskellTypes.*;

%%

%{
  public _HaskellLexer() {
    this((java.io.Reader)null);
  }
%}

%public
%class _HaskellLexer
%implements FlexLexer
%function advance
%type IElementType
%unicode

control_character    = [\000 - \037]
NEWLINE             = \r|\n|\r\n
WHITE_SPACE         = ([ \t\f] | {control_character})+

small               = [a-z_]          // ignoring any unicode lowercase letter for now
large               = [A-Z]           // ignoring any unicode uppercase letter for now
digit               = [0-9]           // ignoring any unicode decimal digit for now
DECIMAL             = {digit}+

hexit               = [0-9A-Fa-f]
HEXADECIMAL         = 0[xX]{hexit}+

octit               = [0-7]
OCTAL               = 0[oO]{octit}+

FLOAT               = [-+]?([0-9]+(\\.[0-9]*)?|\\.[0-9]+)([eE][-+]?[0-9]+)?

COMMENT             = "--"[^\r\n]*
NCOMMENT            = "{-" (.|{NEWLINE})* "-}"

CHARACTER_LITERAL   = \' [^\'\\\r\n\f]* \'
STRING_LITERAL      = \" [^\"\\\r\n\f]* \"

VAR_ID              = ({small} ({small} | {large} | {digit} | ')* )+
CON_ID              = ({large} ({small} | {large})* )+

%%
<YYINITIAL> {

    {COMMENT}             { return HS_COMMENT; }
    {NCOMMENT}            { return HS_NCOMMENT; }
    {NEWLINE}             { return HS_NEWLINE; }
    {WHITE_SPACE}         { return com.intellij.psi.TokenType.WHITE_SPACE; }

    // not listed as reserved identifier but have meaning in certain context,
    // let's say specialreservedid
    "as"                  { return HS_AS; }
    "qualified"           { return HS_QUALIFIED; }
    "hiding"              { return HS_HIDING; }

    // reservedid
    "case"                { return HS_CASE; }
    "class"               { return HS_CLASS; }
    "data"                { return HS_DATA; }
    "default"             { return HS_DEFAULT; }
    "deriving"            { return HS_DERIVING; }
    "do"                  { return HS_DO; }
    "else"                { return HS_ELSE; }
    "foreign"             { return HS_FOREIGN; }
    "if"                  { return HS_IF; }
    "import"              { return HS_IMPORT; }
    "in"                  { return HS_IN; }
    "infix"               { return HS_INFIX; }
    "infixl"              { return HS_INFIXL; }
    "infixr"              { return HS_INFIXR; }
    "instance"            { return HS_INSTANCE; }
    "let"                 { return HS_LET; }
    "module"              { return HS_MODULE; }
    "newtype"             { return HS_NEWTYPE; }
    "of"                  { return HS_OF; }
    "then"                { return HS_THEN; }
    "type"                { return HS_TYPE; }
    "where"               { return HS_WHERE; }
    "_"                   { return HS_UNDERSCORE; }

    {VAR_ID}              { return HS_VAR_ID; }
    {CON_ID}              { return HS_CON_ID; }

    {CHARACTER_LITERAL}   { return HS_CHARACTER_LITERAL;  }
    {STRING_LITERAL}      { return HS_STRING_LITERAL;  }

    {DECIMAL}             { return HS_DECIMAL; }
    {HEXADECIMAL}         { return HS_HEXADECIMAL; }
    {OCTAL}               { return HS_OCTAL; }
    {FLOAT}               { return HS_FLOAT; }

    // ascSymbol except reservedop
    "!"                   { return HS_EXCLAMATION_MARK; }
    "#"                   { return HS_HASH; }
    "$"                   { return HS_DOLLAR; }
    "%"                   { return HS_PERCENTAGE; }
    "&"                   { return HS_AMPERSAND; }
    "*"                   { return HS_STAR; }
    "+"                   { return HS_PLUS; }
    "."                   { return HS_DOT; }
    "/"                   { return HS_SLASH; }
    "<"                   { return HS_LT; }
    ">"                   { return HS_GT; }
    "?"                   { return HS_QUESTION_MARK; }
    "^"                   { return HS_CARET; }
    "-"                   { return HS_DASH; }

    // special
    "("                   { return HS_LEFT_PAREN; }
    ")"                   { return HS_RIGHT_PAREN; }
    ","                   { return HS_COMMA; }
    ";"                   { return HS_SEMICOLON;}
    "["                   { return HS_LEFT_BRACKET; }
    "]"                   { return HS_RIGHT_BRACKET; }
    "`"                   { return HS_BACKQUOTE; }
    "{"                   { return HS_LEFT_BRACE; }
    "}"                   { return HS_RIGHT_BRACE; }

    // reservedop
    ".."                  { return HS_DOT_DOT; }
    "::"                  { return HS_COLON_COLON; }
    ":"                   { return HS_COLON; }
    "="                   { return HS_EQUAL; }
    "\\"                  { return HS_BACKSLASH; }
    "|"                   { return HS_VERTICAL_BAR; }
    "<-"                  { return HS_LEFT_ARROW; }
    "->"                  { return HS_RIGHT_ARROW; }
    "@"                   { return HS_AT; }
    "~"                   { return HS_TILDE; }
    "=>"                  { return HS_DOUBLE_RIGHT_ARROW; }

.                         { return com.intellij.psi.TokenType.BAD_CHARACTER; }
}
