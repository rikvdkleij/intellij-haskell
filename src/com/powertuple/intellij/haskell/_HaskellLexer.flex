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

ControlCharacter = [\000 - \037]
Whitespace = ([ \t\n] | {ControlCharacter})+

SMALL=[a-z_]    // ignoring any unicode lowercase letter for now
LARGE=[A-Z]     // ignoring any unicode uppercase letter for now
DIGIT=[0-9]     // ignoring any unicode decimal digit for now
DECIMAL={DIGIT}+
HEXADECIMAL=0[xX][0-9A-Fa-f]+
OCTAL=0[oO][0-7]+
FLOAT=[-+]?([0-9]+(\\.[0-9]*)?|\\.[0-9]+)([eE][-+]?[0-9]+)?
COMMENT="--"[^\r\n]*

EOL=\r|\n|\r\n
NCOMMENT="{-" (.|{EOL})* "-}"

CHARACTER_LITERAL = \' [^\'\\\r\n]* \'
STRING_LITERAL = \" [^\"\\\r\n]* \"

%%
<YYINITIAL> {
    {COMMENT}             { return HS_COMMENT; }
    {NCOMMENT}            { return HS_NCOMMENT; }

    {Whitespace}          { return com.intellij.psi.TokenType.WHITE_SPACE; }



//    "{"                   { return HS_LEFT_BRACE; }
//    "}"                   { return HS_RIGHT_BRACE; }
//    "["                   { return HS_LEFT_BRACKET; }
//    "]"                   { return HS_RIGHT_BRACKET; }
//    "("                   { return HS_LEFT_PAREN; }
//    ")"                   { return HS_RIGHT_PAREN; }
//    ":"                   { return HS_COLON;}
//    ";"                   { return HS_SEMICOLON;}
//    "."                   { return HS_DOT; }
//    ","                   { return HS_COMMA; }
//    "|"                   { return HS_VERTICAL_BAR;}
    "="                   { return HS_DEFINED_BY; }
    "<-"                  { return HS_DRAW_FROM_OR_MATCHES_OR_IN; }
    "=>"                  { return HS_INSTANCE_CONTEXTS; }


    "<"                  { return HS_LT; }
    ">"                  { return HS_GT; }

    // not listed as reserved identifier but have meaning in certain context
//    "as"                  { return HS_AS; }
//    "hiding"                  { return HS_HIDING; }
//    "qualified"                  { return HS_QUALIFIED; }

    // reserved identifiers
    "case"                { return HS_CASE_KEYWORD; }
    "class"               { return HS_CLASS_KEYWORD; }
    "data"                { return HS_DATA_KEYWORD; }
    "default"             { return HS_DEFAULT_KEYWORD; }
    "deriving"            { return HS_DERIVING_KEYWORD; }
//    "do"                  { return HS_DO_KEYWORD; }
//    "else"                { return HS_ELSE_KEYWORD; }
//    "hiding"              { return HS_HIDING_KEYWORD; }
//    "if"                  { return HS_IF_KEYWORD; }
//    "import"              { return HS_IMPORT_KEYWORD; }
//    "in"                  { return HS_IN_KEYWORD; }
//    "infix"               { return HS_INFIX_KEYWORD; }
//    "infixl"              { return HS_INFIXL_KEYWORD; }
//    "infixr"              { return HS_INFIXR_KEYWORD; }
//    "instance"            { return HS_INSTANCE_KEYWORD; }
//    "let"                 { return HS_LET_KEYWORD; }
    "module"              { return HS_MODULE_KEYWORD; }
//    "newtype"             { return HS_NEWTYPE_KEYWORD; }
//    "of"                  { return HS_OF_KEYWORD; }
//    "then"                { return HS_THEN_KEYWORD; }
//    "type"                { return HS_TYPE_KEYWORD; }
    "where"               { return HS_WHERE_KEYWORD; }

//    "_"                   { return HS___KEYWORD; }

    {CHARACTER_LITERAL}   { return HS_CHARACTER_LITERAL;  }
    {STRING_LITERAL}      { return HS_STRING_LITERAL;  }

    {SMALL}               { return HS_SMALL; }
    {LARGE}               { return HS_LARGE; }
    {DECIMAL}             { return HS_DECIMAL; }
    {HEXADECIMAL}         { return HS_HEXADECIMAL; }
    {OCTAL}               { return HS_OCTAL; }
    {FLOAT}               { return HS_FLOAT; }


    [^] { return com.intellij.psi.TokenType.BAD_CHARACTER; }
}
