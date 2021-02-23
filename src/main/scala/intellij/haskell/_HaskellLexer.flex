package intellij.haskell;
import com.intellij.lexer.*;
import com.intellij.psi.tree.IElementType;
import static intellij.haskell.psi.HaskellTypes.*;

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

    private int optionsGhcStart;
    private int optionsGhcDepth;

    private int haddockStart;
    private int haddockDepth;

    private int qqStart;
    private int qqDepth;
%}

%xstate NCOMMENT, NHADDOCK, QQ, OPTIONS_GHC

newline             = \r|\n|\r\n
unispace            = \x05
white_char          = [\ \t\f\x0B\ \x0D ] | {unispace}    // second "space" is probably ^M, I could not find other solution then justing pasting it in to prevent bad character.
directive           = "#"{white_char}*("if"|"ifdef"|"ifndef"|"define"|"elif"|"else"|"error"|"endif"|"include"|"undef")("\\" (\r|\n|\r\n) | [^\r\n])*
include_directive   = "#"{white_char}*"include"{white_char}*\"({small}|{large}|{digit}|{dot})+\"
white_space         = {white_char}+

underscore          = "_"
small               = [a-z] | {underscore} | [\u03B1-\u03C9] | 𝑖 | 𝕧 | µ | ¬
large               = [A-Z] | [\u0391-\u03A9] | ℝ | ℂ | ℕ | ℤ | ℚ

digit               = [0-9] | [\u2070-\u2079] | [\u2080-\u2089]
decimal             = [-+]?({underscore}*{digit}+)+

hexit               = [0-9A-Fa-f]
hexadecimal         = 0[xX]({underscore}*{hexit}+)+

octit               = [0-7]
octal               = 0[oO]({underscore}*{octit}+)+

float               = [-+]?(({underscore}*[0-9]+)+(\.({underscore}*[0-9]+)+)?|\ \.({underscore}*[0-9]+)+)([eE][-+]?[0-9]+)?

gap                 = \\({white_char}|{newline})*\\
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
star                = "*" | "★"
plus                = "+"
dot                 = "." | "∘"
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

colon_colon         = "::" | "∷"
left_arrow          = "<-" | "←"
right_arrow         = "->" | "→"
double_right_arrow  = "=>" | "⇒"

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
double_quotes       = "\""

forall              = "∀"

symbol_no_dot       = {equal} | {at} | {backslash} | {vertical_bar} | {tilde} | {exclamation_mark} | {hash} | {dollar} | {percentage} | {ampersand} | {star} |
                        {plus} | {slash} | {lt} | {gt} | {question_mark} | {caret} | {dash} | [\u2201-\u22FF]


symbol              = {symbol_no_dot} | {dot}

base_var_id         = {small} ({small} | {large} | {digit} | {quote})*
var_id              = {question_mark}? {base_var_id} | {hash} {base_var_id} | {base_var_id} {hash}
varsym_id           = (({symbol_no_dot} | {left_arrow} | {right_arrow} | {double_right_arrow}) ({symbol} | {colon})+) |
                        {symbol_no_dot} ({symbol} | {colon})*

con_id              = {large} ({small} | {large} | {digit} | {quote})* {hash}?
consym_id           = {quote}? {colon} ({symbol} | {colon})*


pragma_start        = {left_brace}{dash}{hash}
pragma_end          = {hash}{dash}{right_brace}

// Accept also * after -- because of TypeOperators
comment             = {dash}{dash}{dash}*[^\r\n\!\#\$\%\&\⋆\+\.\/\<\=\>\?\@\*][^\r\n]* | {dash}{dash}{white_char}* | "\\begin{code}"
ncomment_start      = {left_brace}{dash}
ncomment_end        = {dash}{right_brace}
haddock             = {dash}{dash}{white_char}[\^\|][^\r\n]* ({newline}+{white_char}*{comment})*
nhaddock_start      = {left_brace}{dash}{white_char}?{vertical_bar}

%%

<NHADDOCK> {
    {ncomment_start} {
        haddockDepth++;
    }

    <<EOF>> {
        yybegin(YYINITIAL);
        zzStartRead = haddockStart;
        return HS_NOT_TERMINATED_COMMENT;
    }

    {ncomment_end} {
        if (haddockDepth > 0) {
            haddockDepth--;
        }
        else {
             yybegin(YYINITIAL);
             zzStartRead = haddockStart;
             return HS_NHADDOCK;
        }
    }

    .|{white_char}|{newline} {}
}

{nhaddock_start} {
    yybegin(NHADDOCK);
    haddockDepth = 0;
    haddockStart = getTokenStart();
}


<NCOMMENT> {
    {ncomment_start} {
        commentDepth++;
    }

    <<EOF>> {
        yybegin(YYINITIAL);
        zzStartRead = commentStart;
        return HS_NOT_TERMINATED_COMMENT;
    }

    {ncomment_end} {
        if (commentDepth > 0) {
            commentDepth--;
        }
        else {
             yybegin(YYINITIAL);
             zzStartRead = commentStart;
             return HS_NCOMMENT;
        }
    }

    .|{white_char}|{newline} {}
}

{ncomment_start} {
    yybegin(NCOMMENT);
    commentDepth = 0;
    commentStart = getTokenStart();
}


<OPTIONS_GHC> {
    <<EOF>> {
        yybegin(YYINITIAL);
        return com.intellij.psi.TokenType.BAD_CHARACTER;
    }

    {pragma_end} {
        yybegin(YYINITIAL);
        return HS_PRAGMA_END;
    }

    "," {
        return HS_PRAGMA_SEP;
    }

    {character_literal}   { return HS_CHARACTER_LITERAL; }
    {string_literal}      { return HS_STRING_LITERAL; }
    {newline}             { return HS_NEWLINE; }
    {dash}                { return HS_DASH; }
    {hash}                { return HS_HASH; }
    {white_space}         { return com.intellij.psi.TokenType.WHITE_SPACE; }

    ([a-zA-Z0-9_=\(\):<>*/\|\'\!\?\.+\^&%$@\[\];,~\\`\"\{\}]|[\u2200-\u22FF]|[\u2190-\u21FF]|[\u0370-\u03FF]) + {
        return HS_ONE_PRAGMA;
    }

    #[^-]+ {
        return HS_ONE_PRAGMA;
    }
}

{pragma_start} {
    yybegin(OPTIONS_GHC);
    return HS_PRAGMA_START;
}


<QQ> {
    {left_bracket} ({var_id}|{con_id}|{dot}|{white_char}|{varsym_id})* {vertical_bar} {
        qqDepth++;
    }

    <<EOF>> {
        yybegin(YYINITIAL);
        zzStartRead = qqStart;
        return HS_NOT_TERMINATED_QQ_EXPRESSION;
    }

    {vertical_bar} {right_bracket} {
        if (qqDepth > 0) {
            qqDepth--;
        }
        else {
             yybegin(YYINITIAL);
             zzStartRead = qqStart;
             return HS_QUASIQUOTE;
        }
    }

    {right_bracket} {
            if (qqDepth > 0) {
                qqDepth--;
            }
            else {
                 yybegin(YYINITIAL);
                 zzStartRead = qqStart;
                 return HS_LIST_COMPREHENSION;
            }
        }

    {left_bracket} {
        qqDepth++;
      }

    .|{white_char}|{newline} {}
}

{left_bracket} ({var_id}|{con_id}|{dot}|{white_char}|{varsym_id})* {vertical_bar} {
    yybegin(QQ);
    qqDepth = 0;
    qqStart = getTokenStart();
}

    {newline}             { return HS_NEWLINE; }

    {haddock}             { return HS_HADDOCK; }

    {comment}             { return HS_COMMENT; }
    {white_space}         { return com.intellij.psi.TokenType.WHITE_SPACE; }

    {include_directive}   { return HS_INCLUDE_DIRECTIVE; }
    {directive}           { return HS_DIRECTIVE; }

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
    {var_id}              { return HS_VAR_ID; }
    {con_id}              { return HS_CON_ID; }

    {character_literal}   { return HS_CHARACTER_LITERAL; }
    {string_literal}      { return HS_STRING_LITERAL; }

    // reservedop and no symbol, except dot_dot because that one is handled as symbol
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

    // symbol identifiers
    {varsym_id}           { return HS_VARSYM_ID; }
    {consym_id}           { return HS_CONSYM_ID; }

    {dot}                 { return HS_DOT; }

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

    {forall}              { return HS_FORALL; }

    {double_quotes}       {return HS_DOUBLE_QUOTES; }

    [^]                   { return com.intellij.psi.TokenType.BAD_CHARACTER; }
