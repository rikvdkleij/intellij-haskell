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

    private int haddockStart;
    private int haddockDepth;

    private int qqStart;
    private int qqDepth;
%}

%xstate NCOMMENT, NHADDOCK, QQ

control_character   = [\000 - \037]
newline             = \r|\n|\r\n
unispace            = \x05
white_char          = [\ \t\f\x0B\ \x0D] | {control_character} | {unispace}   // second "space" is probably ^M, I could not find other solution then justing pasting it in to prevent bad character.
directive           = "#"{white_char}*("if"|"ifdef"|"ifndef"|"define"|"elif"|"else"|"error"|"endif"|"include"|"undef")  ("\\" (\r|\n|\r\n) | [^\r\n])*
white_space         = {white_char}+

small               = [a-z_] | "α" | "β"          // ignoring any more unicode lowercase letter for now
large               = [A-Z]           // ignoring any unicode uppercase letter for now

digit               = [0-9]           // ignoring any unicode decimal digit for now
decimal             = [-+]?{digit}+

hexit               = [0-9A-Fa-f]
hexadecimal         = 0[xX]{hexit}+

octit               = [0-7]
octal               = 0[oO]{octit}+

float               = [-+]?([0-9]+(\\.[0-9]*)?|\\.[0-9]+)([eE][-+]?[0-9]+)?

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
dot_dot             = ".."

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
                        {plus} | {slash} | {lt} | {gt} | {question_mark} | {caret} | {dash} | "⊜" | "≣" | "≤" | "≥"


var_id              = {question_mark}? {small} ({small} | {large} | {digit} | {quote})* {hash}*
varsym_id           = {symbol_no_colon_dot} ({symbol_no_colon_dot} | {dot} | {colon})*

con_id              = {large} ({small} | {large} | {digit} | {quote})* {hash}*
consym_id           = {quote}? {colon} ({symbol_no_colon_dot} | {dot} | {colon})*

shebang_line        = {hash} {exclamation_mark} [^\r\n]*

pragma_start        = "{-#"
pragma_end          = "#-}"

comment             = {dash}{dash}[^\r\n]* | "\\begin{code}"
ncomment_start      = "{-"
ncomment_end        = "-}"
haddock             = {dash}{dash}\ [\^\|][^\r\n]* ({newline} {white_char}* {comment})*
nhaddock_start      = "{-|"

%%

<NHADDOCK> {
    {nhaddock_start} {
        haddockDepth++;
    }

    <<EOF>> {
        int state = yystate();
        yybegin(YYINITIAL);
        zzStartRead = haddockStart;
        return HS_NHADDOCK;
    }

    {ncomment_end} {
        if (haddockDepth > 0) {
            haddockDepth--;
        }
        else {
             int state = yystate();
             yybegin(YYINITIAL);
             zzStartRead = haddockStart;
             return HS_NHADDOCK;
        }
    }

    .|{white_char}|{newline} {}
}

{nhaddock_start}({white_char} | {newline} | "-" | [^#\-\}]) {
    yybegin(NHADDOCK);
    haddockDepth = 0;
    haddockStart = getTokenStart();
}


<NCOMMENT> {
    {ncomment_start} {
        commentDepth++;
    }

    <<EOF>> {
        int state = yystate();
        yybegin(YYINITIAL);
        zzStartRead = commentStart;
        return HS_NCOMMENT;
    }

    {ncomment_end} {
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

{ncomment_start}({white_char} | {newline} | "-" | [^#\-\}]) {
    yybegin(NCOMMENT);
    commentDepth = 0;
    commentStart = getTokenStart();
}

<QQ> {
    {left_bracket} ({var_id}|{con_id}|{dot})* {vertical_bar} {
        qqDepth++;
    }

    <<EOF>> {
        int state = yystate();
        yybegin(YYINITIAL);
        zzStartRead = qqStart;
        return HS_QUASIQUOTE;
    }

    {vertical_bar} {right_bracket} {
        if (qqDepth > 0) {
            qqDepth--;
        }
        else {
             int state = yystate();
             yybegin(YYINITIAL);
             zzStartRead = qqStart;
             return HS_QUASIQUOTE;
        }
    }

    .|{white_char}|{newline} {}
}

{left_bracket} ({var_id}|{con_id}|{dot})* {vertical_bar} {
    yybegin(QQ);
    qqDepth = 0;
    qqStart = getTokenStart();
}

    {newline}             { return HS_NEWLINE; }

    {haddock}             { return HS_HADDOCK; }
    {comment}             { return HS_COMMENT; }
    {white_space}         { return com.intellij.psi.TokenType.WHITE_SPACE; }

    {ncomment_start}      { return HS_NCOMMENT_START; }
    {ncomment_end}        { return HS_NCOMMENT_END; }
    {pragma_start}        { return HS_PRAGMA_START; }
    {pragma_end}          { return HS_PRAGMA_END; }

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

    {dot_dot}             { return HS_DOT_DOT; }

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

    {shebang_line}        { return HS_SHEBANG_LINE; }

    {directive}           { return HS_DIRECTIVE; }

    [^]                   { return com.intellij.psi.TokenType.BAD_CHARACTER; }
