package intellij.haskell

import com.intellij.testFramework.ParsingTestCase

class HaskellParsingTest extends ParsingTestCase("", "hs", new HaskellParserDefinition) {
  override def getTestDataPath: String = "src/test/testData/parsing-hs"

  def testPragma(): Unit = {
    doTest(true, true)
  }

  def testComplicatedPragma(): Unit = {
    doTest(true, true)
  }

  def testSimpleLayout(): Unit = {
    doTest(true, true)
  }

  def testOneLineLetIn(): Unit = {
    doTest(true, true)
  }

  def testDoLayout(): Unit = {
    doTest(true, true)
  }

  def testCaseOf(): Unit = {
    doTest(true, true)
  }

  def testImports(): Unit = {
    doTest(true, true)
  }
}
