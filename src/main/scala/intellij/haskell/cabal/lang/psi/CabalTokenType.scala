package intellij.haskell.cabal.lang.psi

import com.intellij.psi.tree.IElementType

import intellij.haskell.cabal.CabalLanguage

class CabalTokenType(debugName: String)
  extends IElementType(debugName, CabalLanguage.Instance)

class CabalSymbolTokenType(debugName: String) extends CabalTokenType(debugName)
class CabalOperatorTokenType(debugName: String) extends CabalTokenType(debugName)
class CabalComparatorTokenType(debugName: String) extends CabalTokenType(debugName)
class CabalLogicalTokenType(debugName: String) extends CabalTokenType(debugName)

class CabalWordLikeTokenType(debugName: String) extends CabalTokenType(debugName)
class CabalIdentTokenType(debugName: String) extends CabalWordLikeTokenType(debugName)
class CabalNumericTokenType(debugName: String) extends CabalWordLikeTokenType(debugName)
class CabalFieldKeyTokenType(debugName: String) extends CabalIdentTokenType(debugName)
class CabalStanzaKeyTokenType(debugName: String) extends CabalIdentTokenType(debugName)
class CabalStanzaArgTokenType(debugName: String) extends CabalIdentTokenType(debugName)

trait CabalFuncLikeTokenType

class CabalFuncNameTokenType(debugName: String)
  extends CabalIdentTokenType(debugName)
  with CabalFuncLikeTokenType

class CabalFlagKeywordTokenType(debugName: String)
  extends CabalStanzaKeyTokenType(debugName)
  with CabalFuncLikeTokenType

class CabalLayoutTokenType(debugName: String) extends CabalTokenType(debugName)
