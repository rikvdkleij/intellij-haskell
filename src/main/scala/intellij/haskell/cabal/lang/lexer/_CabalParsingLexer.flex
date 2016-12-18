package intellij.haskell.cabal.lang.lexer;

import java.util.regex.*;

import com.intellij.lexer.*;
import com.intellij.psi.tree.IElementType;

import intellij.haskell.cabal.lang.psi.CabalTypes;

%%

%{
  public _CabalParsingLexer() { this((java.io.Reader)null); }

  /** This should match the newline indent rule defined in our Flex lexer. */
  public static Pattern NEWLINE_INDENT_REGEX = Pattern.compile(
    "(\\r|\\n|\\r\\n)(( |\\t)*)", Pattern.MULTILINE
  );

  protected int currentLineIndent = 0;
  protected int indentLevel = 0;
%}

%public
%class _CabalParsingLexer
%implements FlexLexer
%function advance
%type IElementType
%unicode
%ignorecase
%eof{ return;
%eof}

CRLF=\n|\r|\r\n
WHITE_SPACE=[\ \t\f]
NOT_WHITE_SPACE=[^\ ]
KEY=[A-Za-z_][A-Za-z_0-9\-]*
DIGIT=[0-9]
COMMENT="--" [^\r\n]*

%state MAIN, INDENT

%%

// This entry point is the start of any line.  We always go to MAIN immediately
// after any of these rules, reason being that we need to know when we are
// at the start of a line so we know when to lex a comment.  The ony exceptions
// are when we lex a COMMENT or CRLF.
<YYINITIAL> {
  {WHITE_SPACE}* {COMMENT} { return CabalTypes.COMMENT; }
  {CRLF}*  { return CabalTypes.WHITE_SPACE; }
  [^]     { yypushback(yylength()); yybegin(MAIN); return CabalTypes.WHITE_SPACE; }
}

// Entry point after we've
<MAIN> {
  ":"   { return CabalTypes.COLON; }
  "("   { return CabalTypes.LPAREN; }
  ")"   { return CabalTypes.RPAREN; }
  "["   { return CabalTypes.LBRACKET; }
  "]"   { return CabalTypes.RBRACKET; }
  "{"   { return CabalTypes.LBRACE; }
  "}"   { return CabalTypes.RBRACE; }
  "=="  { return CabalTypes.EQ; }
  ">"   { return CabalTypes.GT; }
  ">="  { return CabalTypes.GTE; }
  "<"   { return CabalTypes.LT; }
  "<="  { return CabalTypes.LTE; }
  "&&"  { return CabalTypes.AND; }
  "||"  { return CabalTypes.OR; }
  "-"   { return CabalTypes.DASH; }
  "."   { return CabalTypes.DOT; }
  ","   { return CabalTypes.COMMA; }
  "!"   { return CabalTypes.BANG; }
  \t    { return CabalTypes.TAB; }

  // Keywords
  "as" { return CabalTypes.AS; }
  "with" { return CabalTypes.WITH; }
  "true" { return CabalTypes.TRUE; }
  "false" { return CabalTypes.FALSE; }
  "if" { return CabalTypes.IF; }
  "else" { return CabalTypes.ELSE; }
  "flag" { return CabalTypes.FLAG; }
  "os" { return CabalTypes.OS; }
  "arch" { return CabalTypes.ARCH; }
  "impl" { return CabalTypes.IMPL; }

  // Stanza names
  "library" { return CabalTypes.LIBRARY_KEY; }
  "executable" { return CabalTypes.EXECUTABLE_KEY; }
  "test-suite" { return CabalTypes.TEST_SUITE_KEY; }
  "benchmark" { return CabalTypes.BENCHMARK_KEY; }
  "source-repository" { return CabalTypes.SOURCE_REPO_KEY; }

  // Field names
  "name" { return CabalTypes.NAME_KEY; }
  "version" { return CabalTypes.VERSION_KEY; }
  "cabal-version" { return CabalTypes.CABAL_VERSION_KEY; }
  "build-type" { return CabalTypes.BUILD_TYPE_KEY; }
  "license" { return CabalTypes.LICENSE_KEY; }
  "license-file" { return CabalTypes.LICENSE_FILE_KEY; }
  "license-files" { return CabalTypes.LICENSE_FILES_KEY; }
  "copyright" { return CabalTypes.COPYRIGHT_KEY; }
  "author" { return CabalTypes.AUTHOR_KEY; }
  "maintainer" { return CabalTypes.MAINTAINER_KEY; }
  "stability" { return CabalTypes.STABILITY_KEY; }
  "homepage" { return CabalTypes.HOMEPAGE_KEY; }
  "bug-reports" { return CabalTypes.BUG_REPORTS_KEY; }
  "package-url" { return CabalTypes.PACKAGE_URL_KEY; }
  "synopsis" { return CabalTypes.SYNOPSIS_KEY; }
  "description" { return CabalTypes.DESCRIPTION_KEY; }
  "category" { return CabalTypes.CATEGORY_KEY; }
  "tested-with" { return CabalTypes.TESTED_WITH_KEY; }
  "data-files" { return CabalTypes.DATA_FILES_KEY; }
  "data-dir" { return CabalTypes.DATA_DIR_KEY; }
  "extra-source-files" { return CabalTypes.EXTRA_SOURCE_FILES_KEY; }
  "extra-doc-files" { return CabalTypes.EXTRA_DOC_FILES_KEY; }
  "extra-tmp-files" { return CabalTypes.EXTRA_TMP_FILES_KEY; }
  "default" { return CabalTypes.DEFAULT_KEY; }
  "manual" { return CabalTypes.MANUAL_KEY; }
  "type" { return CabalTypes.TYPE_KEY; }
  "main-is" { return CabalTypes.MAIN_IS_KEY; }
  "location" { return CabalTypes.LOCATION_KEY; }
  "branch" { return CabalTypes.BRANCH_KEY; }
  "tag" { return CabalTypes.TAG_KEY; }
  "subdir" { return CabalTypes.SUBDIR_KEY; }
  "build-depends" { return CabalTypes.BUILD_DEPENDS_KEY; }
  "other-modules" { return CabalTypes.OTHER_MODULES_KEY; }
  "default-language" { return CabalTypes.DEFAULT_LANGUAGE_KEY; }
  "other-languages" { return CabalTypes.OTHER_LANGUAGES_KEY; }
  "default-extensions" { return CabalTypes.DEFAULT_EXTENSIONS_KEY; }
  "other-extensions" { return CabalTypes.OTHER_EXTENSIONS_KEY; }
  "hs-source-dir" { return CabalTypes.HS_SOURCE_DIR_KEY; }
  "hs-source-dirs" { return CabalTypes.HS_SOURCE_DIRS_KEY; }
  "extensions" { return CabalTypes.EXTENSIONS_KEY; }
  "build-tools" { return CabalTypes.BUILD_TOOLS_KEY; }
  "buildable" { return CabalTypes.BUILDABLE_KEY; }
  "ghc-options" { return CabalTypes.GHC_OPTIONS_KEY; }
  "ghc-prof-options" { return CabalTypes.GHC_PROF_OPTIONS_KEY; }
  "ghc-shared-options" { return CabalTypes.GHC_SHARED_OPTIONS_KEY; }
  "includes" { return CabalTypes.INCLUDES_KEY; }
  "install-includes" { return CabalTypes.INSTALL_INCLUDES_KEY; }
  "include-dirs" { return CabalTypes.INCLUDE_DIRS_KEY; }
  "c-sources" { return CabalTypes.C_SOURCES_KEY; }
  "js-sources" { return CabalTypes.JS_SOURCES_KEY; }
  "extra-libraries" { return CabalTypes.EXTRA_LIBRARIES_KEY; }
  "extra-ghci-libraries" { return CabalTypes.EXTRA_GHCI_LIBRARIES_KEY; }
  "extra-lib-dirs" { return CabalTypes.EXTRA_LIB_DIRS_KEY; }
  "cc-options" { return CabalTypes.CC_OPTIONS_KEY; }
  "cpp-options" { return CabalTypes.CPP_OPTIONS_KEY; }
  "ld-options" { return CabalTypes.LD_OPTIONS_KEY; }
  "pkgconfig-depends" { return CabalTypes.PKGCONFIG_DEPENDS_KEY; }
  "frameworks" { return CabalTypes.FRAMEWORKS_KEY; }
  "required-signatures" { return CabalTypes.REQUIRED_SIGNATURES_KEY; }
  "exposed-modules" { return CabalTypes.EXPOSED_MODULES_KEY; }
  "exposed" { return CabalTypes.EXPOSED_KEY; }
  "reexported-modules" { return CabalTypes.REEXPORTED_MODULES_KEY; }

  "x-" {KEY} { return CabalTypes.CUSTOM_KEY; }
  {KEY} { return CabalTypes.UNKNOWN_KEY; }

  {DIGIT}+ ("." {DIGIT}+)* (".*"?)  { return CabalTypes.NUMBERS; }
  {WHITE_SPACE}+  { return CabalTypes.WHITE_SPACE; }

  // Newline indent rule, this should match the NEWLINE_INDENT_REGEX pattern.
  {CRLF} {WHITE_SPACE}* {
    final Matcher m = NEWLINE_INDENT_REGEX.matcher(yytext());
    if (!m.matches()) throw new AssertionError("NEWLINE_INDENT_REGEX did not match!");
    final String indent = m.group(2);
    yypushback(indent.length());
    yybegin(INDENT);
    return CabalTypes.EOL;
  }

  [^]   { return CabalTypes.OTHER_CHAR; }
}

// This section is only entered after the newline indent rule.
<INDENT> {
  // Comments shouldn't affect the indentation.
  {WHITE_SPACE}* {COMMENT}  { return CabalTypes.COMMENT; }

  // A pure whitespace line can be disregarded.
  {WHITE_SPACE}* {CRLF} { currentLineIndent = 0; return CabalTypes.WHITE_SPACE; }

  // This rule only consumes zero or one whitespaces and returns an INDENT or DEDENT token.
  // The rule will be continually applied until there are zero whitespaces.
  {WHITE_SPACE}* {NOT_WHITE_SPACE} {
    final int numWhitespace = yylength() - 1;

    if (currentLineIndent == 0) {
      if (numWhitespace == indentLevel) {
        // Consume all except the NON_WHITE_SPACE char
        yypushback(1);
        yybegin(YYINITIAL);
        //return CabalTypes.LINE_START;
        return CabalTypes.WHITE_SPACE;
      }
      if (numWhitespace > indentLevel) {
        // Consume up to the indentLevel + 1 for the INDENT
        currentLineIndent = indentLevel + 1;
        yypushback(yylength() - (indentLevel + 1));
        return CabalTypes.INDENT;
      }
      if (numWhitespace < indentLevel) {
        --indentLevel;
        yypushback(yylength());
        return CabalTypes.DEDENT;
      }
      // Shouldn't happen since we checked ==, >, and <
      throw new AssertionError(
        "Unexpected case: numWhitespace: " + numWhitespace + "; "
          + "indentLevel: " + indentLevel
      );
    }

    // If no more whitespace, start the line.
    if (numWhitespace == 0) {
      indentLevel = currentLineIndent;
      currentLineIndent = 0;
      yypushback(1);
      yybegin(YYINITIAL);
      return CabalTypes.WHITE_SPACE;
      //return CabalTypes.LINE_START;
    }

    ++currentLineIndent;
    // Consume 1 whitespace char.
    yypushback(yylength() - 1);
    return CabalTypes.INDENT;
  }
}
