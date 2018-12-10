{
{-# LANGUAGE LambdaCase #-}

module OwO.Syntax.Parser.Lexer where

import           OwO.Syntax.TokenType
import           OwO.Syntax.Position

import           Data.Maybe           (listToMaybe)
import qualified Data.Text            as T
import qualified OwO.Util.StrictMaybe as Strict
import           OwO.Util.Applicative
}

%wrapper "monadUserState"

$digit       = [0-9]
$white_no_nl = $white # \n
$escape      = [ r n b t a \\ \" \' ]
$operator_c  = [ \+ \- \/ \\ \< \> \~ @ \# \$ \% \* \^ \? \, ]
$operator_s  = [ \[ \] \| \= \: \. ]
$comment_c   = [^ \n \- \{ ]

@integer     = $digit+
@identifier  = [A-Za-z][0-9A-Za-z'_]*
@string      = \"([^ \\ \"]|\\$escape)*\"
@character   = \'([^ \\ \']|\\$escape)\'
@operator    = $operator_c ($operator_c | $operator_s)*
@colon_op    = \: ($operator_c | $operator_s)+
@comment_l   = \-\-[^ \n ]*
@comment     = ($comment_c | $white)+

tokens :-

$white_no_nl  ;
@comment_l    { simpleString (CommentToken . T.pack) }

<layout> {
  \n          ;
  \{          { explicitBraceLeft }
  ()          { newLayoutContext }
}

<0> {
  \n          { beginCode bol }
  module      { simple ModuleToken }
  open        { simple OpenToken }
  do          { simple DoToken }
  of          { simple OfToken }
  data        { simple DataToken }
  codata      { simple CodataToken }
  case        { simple CaseToken }
  cocase      { simple CocaseToken }
  import      { simple ImportToken }
  where       { simple WhereToken }
  postulate   { simple PostulateToken }
  instance    { simple InstanceToken }
  infixl      { simple InfixLToken }
  infixr      { simple InfixRToken }
  infix       { simple InfixToken }
  @integer    { simpleString (IntegerToken . read) }
  @identifier { simpleString (IdentifierToken . T.pack) }
  @string     { simpleString (StringToken . T.pack . read) }
  @character  { simpleString (CharToken . read) }
  \<\-        { simple LeftArrowToken }
  \-\>        { simple RightArrowToken }
  @colon_op   { simpleString (OperatorToken . T.pack) }
  \:          { simple ColonToken }
  \;          { simple SemicolonToken }
  \(\|        { simple IdiomBracketLToken }
  \|\)        { simple IdiomBracketRToken }
  \{\|        { simple InstanceArgumentLToken }
  \|\}        { simple InstanceArgumentRToken }
  \[\|        { simple InaccessiblePatternLToken }
  \|\]        { simple InaccessiblePatternRToken }
  \{\-        { pushBlockComment }
  \|          { simple SeparatorToken }
  \(          { simple ParenthesisLToken }
  \)          { simple ParenthesisRToken }
  \{          { simple BraceLToken }
  \}          { simple BraceRToken }
  \[          { simple BracketLToken }
  \]          { simple BracketRToken }
  \=          { simple EqualToken }
  \.          { simple DotToken }
  @operator   { simpleString (OperatorToken . T.pack) }
}

<nestedComment> {
  \{\-        { pushBlockComment }
  \-\}        { popBlockComment }
  \n          ;
  @comment    { simpleString (CommentToken . T.pack) }
  \-?         { simpleString (CommentToken . T.pack) }
  ()          ;
}

<bol> {
  \n          ;
  ()          { doBol }
}

{

beginCode :: Int -> AlexAction PsiToken
beginCode n _ _ = pushLexState n >> alexMonadScan

simple :: TokenType -> AlexAction PsiToken
simple token (pn, _, _, _) size = do
  -- run `pushLexState` when it's `where` or `postulate`
  isStartingNewLayout token `ifM` pushLexState layout
  toMonadPsi' pn size token

explicitBraceLeft :: AlexAction PsiToken
explicitBraceLeft (pn, _, _, _) size = do
  popLexState
  pushLayout NoLayout
  toMonadPsi' pn size BraceLToken

simpleString :: (String -> TokenType) -> AlexAction PsiToken
simpleString f (pn, _, _, s) size =
   toMonadPsi' pn size . f $ take size s

toMonadPsi' :: AlexPosn -> Int -> TokenType -> Alex PsiToken
toMonadPsi' (AlexPn pos line col) = toMonadPsi pos line col

toMonadPsi :: Int -> Int -> Int -> Int -> TokenType -> Alex PsiToken
toMonadPsi pos line col size token = do
  file <- currentFile <$> alexGetUserState
  let start = simplePosition pos line col
  let end   = simplePosition (pos + size) line (col + size)
  pure $ PsiToken
    { tokenType = token
    , location  = locationFromSegment start end file
    }

alexEOF :: Alex PsiToken
alexEOF = getLayout >>= \case
    Nothing         -> java EndOfFileToken
    Just (Layout _) -> popLayout >> java BraceRToken
    Just  NoLayout  -> popLayout >> alexMonadScan
  where
    java token = do
       (pn, _, _, _) <- alexGetInput
       toMonadPsi' pn 0 token

pushBlockComment :: AlexAction PsiToken
pushBlockComment (pn, _, _, s) size = do
  pushLexState nestedComment
  toMonadPsi' pn size $ CommentToken (T.pack $ take size s)

popBlockComment :: AlexAction PsiToken
popBlockComment (pn, _, _, s) size = do
  popLexState
  toMonadPsi' pn size $ CommentToken (T.pack $ take size s)

doBol :: AlexAction PsiToken
doBol (pn@(AlexPn _ _ col), _, _, _) size =
  getLayout >>= \case
    Just (Layout n) -> case col `compare` n of
      LT -> popLayout   >> addToken BraceRToken
      EQ -> popLexState >> addToken SemicolonToken
      GT -> popLexState >> alexMonadScan
    _ -> popLexState >> alexMonadScan
  where
    addToken = toMonadPsi' pn size

newLayoutContext :: AlexAction PsiToken
newLayoutContext (pn@(AlexPn _ _ col), _, _, _) size = do
  popLexState
  pushLayout $ Layout col
  toMonadPsi' pn size BraceLToken

pushLayout :: LayoutContext -> Alex ()
pushLayout lc = do
  s@AlexUserState { layoutStack = lcs } <- alexGetUserState
  alexSetUserState s { layoutStack = lc : lcs }

popLayout :: Alex LayoutContext
popLayout = do
  s <- alexGetUserState
  case layoutStack s of
    []     -> alexError "Layout expected but no layout available"
    l : ls -> do
      alexSetUserState s { layoutStack = ls }
      pure l

getLayout :: Alex (Maybe LayoutContext)
getLayout = do
  AlexUserState { layoutStack = lcs } <- alexGetUserState
  pure $ listToMaybe lcs

pushLexState :: Int -> Alex ()
pushLexState nsc = do
  sc <- alexGetStartCode
  s@AlexUserState { alexStartCodes = scs } <- alexGetUserState
  alexSetUserState s { alexStartCodes = sc : scs }
  alexSetStartCode nsc

popLexState :: Alex Int
popLexState = do
  csc <- alexGetStartCode
  st  <- alexGetUserState
  case alexStartCodes st of
    []     -> alexError "State code expected but no state code available"
    s : ss -> do
      alexSetUserState st { alexStartCodes = ss }
      alexSetStartCode s
      pure csc

}
