package intellij.haskell.alex.highlighting

import java.util

import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.options.colors.{AttributesDescriptor, ColorDescriptor, ColorSettingsPage}
import icons.HaskellIcons
import javax.swing._

/**
  * @author ice1000
  */
object AlexColorSettingsPage {
  private val DESCRIPTORS = Array[AttributesDescriptor](
    new AttributesDescriptor("Strings", AlexSyntaxHighlighter.STRINGS),
    new AttributesDescriptor("Rules", AlexSyntaxHighlighter.RULES),
    new AttributesDescriptor("Token sets", AlexSyntaxHighlighter.TOKEN_SETS),
    new AttributesDescriptor("Keyword", AlexSyntaxHighlighter.KEYWORD),
    new AttributesDescriptor("Semicolon", AlexSyntaxHighlighter.SEMICOLON),
    new AttributesDescriptor("Braces", AlexSyntaxHighlighter.BRACES),
    new AttributesDescriptor("Parenthesis", AlexSyntaxHighlighter.PARENTHESIS)
  )
}

/**
  * @author ice1000
  */
class AlexColorSettingsPage extends ColorSettingsPage {
  override def getIcon: Icon = {
    HaskellIcons.AlexLogo
  }

  override def getHighlighter = {
    new AlexSyntaxHighlighter
  }

  override def getDemoText: String = {
    """{
      | a piece of haskell code here
      |}
      |%wrapper "alexUserMonadState"
      |
      |$token_sets = [a-zA-Z]
      |@token_rule = $token_sets+
      |
      |tokens :-
      |<state> {
      |  "wow" { haskell code }
      |  \n+[a-z]? { bla }
      |  () ;
      |}
      |@token_rule { haskell code }
      |
      |{
      |haskell code
      |}
      |""".stripMargin
  }

  override def getAdditionalHighlightingTagToDescriptorMap: util.Map[String, TextAttributesKey] = null

  override def getAttributeDescriptors: Array[AttributesDescriptor] = {
    AlexColorSettingsPage.DESCRIPTORS
  }

  override def getColorDescriptors: Array[ColorDescriptor] = {
    ColorDescriptor.EMPTY_ARRAY
  }

  override def getDisplayName = {
    "Alex"
  }
}