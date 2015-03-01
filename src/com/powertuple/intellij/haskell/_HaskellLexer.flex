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

%{
    private int commentStart;
    private int commentDepth;
%}

%xstate NCOMMENT, TEX

control_character   = [\000 - \037]
newline             = \r|\n|\r\n
unispace            = \x05
white_char          = [ \t\f] | {control_character} | {unispace}
directive           = "#"("if"|"ifdef"|"ifndef"|"define"|"elif"|"else"|"error"|"endif"|"include"|"undef")[^\r\n]*
white_space         = {white_char}+ | {directive}

small               = [a-z_]          // ignoring any unicode lowercase letter for now
large               = [A-Z]           // ignoring any unicode uppercase letter for now

digit               = [0-9]           // ignoring any unicode decimal digit for now
decimal             = [-+]?{digit}+

hexit               = [0-9A-Fa-f]
hexadecimal         = 0[xX]{hexit}+

octit               = [0-7]
octal               = 0[oO]{octit}+

float               = [-+]?([0-9]+(\\.[0-9]*)?|\\.[0-9]+)([eE][-+]?[0-9]+)?

pragma_start        = "{-#"
pragma_end          = "#-}"

comment             = ("--"[^\r\n]* | "\\begin{code}")

ncomment_start      = "{-"
ncomment_end        = "-}"

gap                 = \\{white_char}*\\
cntrl               = {large} | [@\[\\\]\^_]
charesc             = [abfnrtv\\\"\'&]
ascii               = ("^"{cntrl})|(NUL)|(SOH)|(STX)|(ETX)|(EOT)|(ENQ)|(ACK)|(BEL)|(BS)|(HT)|(LF)|(VT)|(FF)|(CR)|(SO)|(SI)|(DLE)|(DC1)|(DC2)|(DC3)|(DC4)|(NAK)|(SYN)|(ETB)|(CAN)|(EM)|(SUB)|(ESC)|(FS)|(GS)|(RS)|(US)|(SP)|(DEL)
escape              = \\({charesc}|{ascii}|({digit}+)|(o({octit}+))|(x({hexit}+)))

character_literal   = (\'([^\'\\\n]|{escape})\')
string_literal      = \"([^\"\\\n]|{escape}|{gap})*(\"|\n)


// ascSymbol except reservedop
exclamation_mark    = "!"
hash                = "#"
dollar              = "$"
percentage          = "%"
ampersand           = "&"
star                = "*"
plus                = "+"
dot                 = "."
slash               = "/"
lt                  = "<"
gt                  = ">"
question_mark       = "?"
caret               = "^"
dash                = "-"

// symbol and reservedop
equal               = "="
at                  = "@"
backslash           = "\\"
vertical_bar        = "|"
tilde               = "~"
colon               = ":"

// reservedop and not symbol, '..' is handled as two dots as symbol, see also special symbol (..)
colon_colon         = "::"
left_arrow          = "<-" | "\u2190"
right_arrow         = "->" | "\u2192"
double_right_arrow  = "=>" | "\u21D2"

 // special
left_paren          = "("
right_paren         = ")"
comma               = ","
semicolon           = ";"
left_bracket        = "["
right_bracket       = "]"
backquote           = "`"
left_brace          = "{"
right_brace         = "}"

quote               = "'"

symbol_no_colon_dot = {equal} | {at} | {backslash} | {vertical_bar} | {tilde} | {exclamation_mark} | {hash} | {dollar} | {percentage} | {ampersand} | {star} |
                        {plus} | {slash} | {lt} | {gt} | {question_mark} | {caret} | {dash}

var_id              = {small} ({small} | {large} | {digit} | {quote})* {hash}*
varsym_id           = {symbol_no_colon_dot} ({symbol_no_colon_dot} | {dot} | {colon})*

con_id              = {large} ({small} | {large} | {digit} | {quote})* {hash}*
consym_id           = {quote}? {colon} ({symbol_no_colon_dot} | {dot} | {colon})*

quasi_quote_v_start = {left_bracket} {var_id} {vertical_bar}
quasi_quote_e_start = {left_bracket} "e"? {vertical_bar}
quasi_quote_d_start = {left_bracket} "d" {vertical_bar}
quasi_quote_t_start = {left_bracket} "t" {vertical_bar}
quasi_quote_p_start = {left_bracket} "p" {vertical_bar}
quasi_quote_end     = {vertical_bar} {right_bracket}

shebang_line        = {hash} {exclamation_mark} [^\r\n]*

%%

<TEX> {
    [^\\]+            { return HS_NCOMMENT; }
    "\\begin{code}"   { yybegin(YYINITIAL); return HS_NCOMMENT; }
    \\+*              { return HS_NCOMMENT; }
}

<NCOMMENT> {
    {ncomment_start} ({newline}| {white_char} | {vertical_bar} | {small} | {large} | {digit} | {dash})? {
        commentDepth++;
    }

    <<EOF>> {
        int state = yystate();
        yybegin(YYINITIAL);
        zzStartRead = commentStart;
        return HS_NCOMMENT;
    }

    {ncomment_end} {newline}? {
        if (commentDepth > 0) {
            commentDepth--;
        }
        else {
             int state = yystate();
             yybegin(YYINITIAL);
             zzStartRead = commentStart;
             return HS_NCOMMENT;
        }
    }

    .|{white_char}|{newline} {}
}

{ncomment_start} ({vertical_bar} | {newline} | {white_char} | {small} | {large} | {digit} | {dash}) {
    yybegin(NCOMMENT);
    commentDepth = 0;
    commentStart = getTokenStart();
}

    {newline}             { return HS_NEWLINE; }
    {comment}             { return HS_COMMENT; }
    {white_space}         { return com.intellij.psi.TokenType.WHITE_SPACE; }

    {pragma_start}        { return HS_PRAGMA_START; }
    {pragma_end}          { return HS_PRAGMA_END; }

    {ncomment_start}      { return HS_NCOMMENT_START; }
    {ncomment_end}        { return HS_NCOMMENT_END; }

    // not listed as reserved identifier but have meaning in certain context,
    // let's say specialreservedid
    "type family"         { return HS_TYPE_FAMILY; }
    "type instance"       { return HS_TYPE_INSTANCE; }
    "foreign import"      { return HS_FOREIGN_IMPORT; }
    "foreign export"      { return HS_FOREIGN_EXPORT; }

    // reservedid
    "case"                { return HS_CASE; }
    "class"               { return HS_CLASS; }
    "data"                { return HS_DATA; }
    "default"             { return HS_DEFAULT; }
    "deriving"            { return HS_DERIVING; }
    "do"                  { return HS_DO; }
    "else"                { return HS_ELSE; }
//    "foreign"             { return HS_FOREIGN; } used together with import and export, see specialreservedid
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

    // identifiers
    {var_id}              { return HS_VARID_ID; }
    {con_id}              { return HS_CONID_ID; }

    {character_literal}   { return HS_CHARACTER_LITERAL; }
    {string_literal}      { return HS_STRING_LITERAL; }

    // reservedop and no symbol, except dot_dot because this one is handled as symbol
    {colon_colon}         { return HS_COLON_COLON; }
    {left_arrow}          { return HS_LEFT_ARROW; }
    {right_arrow}         { return HS_RIGHT_ARROW; }
    {double_right_arrow}  { return HS_DOUBLE_RIGHT_ARROW; }

    // number
    {decimal}             { return HS_DECIMAL; }
    {hexadecimal}         { return HS_HEXADECIMAL; }
    {octal}               { return HS_OCTAL; }
    {float}               { return HS_FLOAT; }

    // symbol and reservedop
    {equal}               { return HS_EQUAL; }
    {at}                  { return HS_AT; }
    {backslash}           { return HS_BACKSLASH; }
    {vertical_bar}        { return HS_VERTICAL_BAR; }
    {tilde}               { return HS_TILDE; }

    // symbols
    {dot}                 { return HS_DOT; }

    // symbol identifiers
    {varsym_id}           { return HS_VARSYM_ID; }
    {consym_id}           { return HS_CONSYM_ID; }

    // special
    {left_paren}          { return HS_LEFT_PAREN; }
    {right_paren}         { return HS_RIGHT_PAREN; }
    {comma}               { return HS_COMMA; }
    {semicolon}           { return HS_SEMICOLON;}
    {left_bracket}        { return HS_LEFT_BRACKET; }
    {right_bracket}       { return HS_RIGHT_BRACKET; }
    {backquote}           { return HS_BACKQUOTE; }
    {left_brace}          { return HS_LEFT_BRACE; }
    {right_brace}         { return HS_RIGHT_BRACE; }

    {quote}               { return HS_QUOTE; }

    "\\end{code}"         { yybegin(TEX); return HS_NCOMMENT; }
    "\\section"           { yybegin(TEX); return HS_NCOMMENT; }

    {quasi_quote_e_start} { return HS_QUASI_QUOTE_E_START; }
    {quasi_quote_d_start} { return HS_QUASI_QUOTE_D_START; }
    {quasi_quote_t_start} { return HS_QUASI_QUOTE_T_START; }
    {quasi_quote_p_start} { return HS_QUASI_QUOTE_P_START; }
    {quasi_quote_v_start} { return HS_QUASI_QUOTE_V_START; }
    {quasi_quote_end}     { return HS_QUASI_QUOTE_END; }

    {shebang_line}        { return HS_SHEBANG_LINE; }

.                         { return com.intellij.psi.TokenType.BAD_CHARACTER; }
