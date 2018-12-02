package intellij.haskell.alex.lang.lexer;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import intellij.haskell.alex.lang.psi.AlexTypes;

import static com.intellij.psi.TokenType.BAD_CHARACTER;
import static com.intellij.psi.TokenType.WHITE_SPACE;

%%

%{
  public _AlexLexer() { this((java.io.Reader)null); }

  private int nestedLeftBraces = 0;
  private boolean afterTokens = false;
  private boolean insideState = false;
%}

%public
%class _AlexLexer
%implements FlexLexer
%function advance
%type IElementType
%unicode
%ignorecase
%eof{ return;
%eof}

%xstate SOMETHING, AFTER_EQ, AFTER_STATE

CRLF=\n|\r|\r\n
WHITE_SPACE=[\ \t\f]
IDENTIFIER=[a-zA-Z_][a-zA-Z_0-9]*
STRING_UNFINISHED=\"([^\"\\]|(\\[^]))*
STRING_FINISHED={STRING_UNFINISHED}\"

%%

<AFTER_EQ> {
  {CRLF} { yybegin(YYINITIAL); return AlexTypes.ALEX_EOL; }
  \${IDENTIFIER} { return AlexTypes.ALEX_DOLLAR_AND_IDENTIFIER; }
  [^\$\r\n]+ { return AlexTypes.ALEX_REGEX_PART_TOKEN; }
  \$ { return AlexTypes.ALEX_REGEX_PART_TOKEN; }
}

<SOMETHING> {
  "{" { nestedLeftBraces++; return AlexTypes.ALEX_SOMETHING_IS_HAPPENING; }
  "}" {
    if (--nestedLeftBraces == 0) {
      yybegin(YYINITIAL);
      return AlexTypes.ALEX_SOMETHING_HAS_ALREADY_HAPPENED;
    }
    return AlexTypes.ALEX_SOMETHING_IS_HAPPENING;
  }
  [^\}\{]+ { return AlexTypes.ALEX_SOMETHING_IS_HAPPENING; }
}

<AFTER_STATE> {
  "{" { insideState = true; yybegin(YYINITIAL); return AlexTypes.ALEX_SOMETHING_IS_GONNA_HAPPEN; }
  {CRLF} { return AlexTypes.ALEX_EOL; }
  {WHITE_SPACE} { return WHITE_SPACE; }
  [^] { return BAD_CHARACTER; }
}

{CRLF} { return AlexTypes.ALEX_EOL; }
{WHITE_SPACE} { return WHITE_SPACE; }
"<" { return AlexTypes.ALEX_STATEFUL_TOKENS_RULE_START; }
">" { yybegin(AFTER_STATE); return AlexTypes.ALEX_STATEFUL_TOKENS_RULE_END; }
tokens { return AlexTypes.ALEX_TOKENS; }
:- { afterTokens = true; return AlexTypes.ALEX_A_SYMBOL_FOLLOWED_BY_TOKENS; }
= { yybegin(AFTER_EQ); return AlexTypes.ALEX_EQUAL; }
"{" { nestedLeftBraces++; yybegin(SOMETHING); return AlexTypes.ALEX_SOMETHING_IS_GONNA_HAPPEN; }
%wrapper { return AlexTypes.ALEX_WRAPPER_TYPE_IS_GONNA_BE_HERE; }
"}" {
  if (insideState) {
    insideState = false;
    return AlexTypes.ALEX_SOMETHING_HAS_ALREADY_HAPPENED;
  } else return BAD_CHARACTER;
}
; { return AlexTypes.ALEX_SEMICOLON; }
{IDENTIFIER} { return AlexTypes.ALEX_HASKELL_IDENTIFIER; }
\${IDENTIFIER} { return AlexTypes.ALEX_DOLLAR_AND_IDENTIFIER; }
\@{IDENTIFIER} { return AlexTypes.ALEX_EMAIL_AND_IDENTIFIER; }

{STRING_FINISHED} { return AlexTypes.ALEX_STRING; }
{STRING_UNFINISHED} { return BAD_CHARACTER; }
[^] { return BAD_CHARACTER; }
