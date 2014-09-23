/*
 * Copyright 2014 Rik van der Kleij
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.powertuple.intellij.haskell.highlighter

import javax.swing._

import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.options.colors.{AttributesDescriptor, ColorDescriptor, ColorSettingsPage}
import com.powertuple.intellij.haskell.HaskellIcons
import com.powertuple.intellij.haskell.highlighter.HaskellSyntaxHighlighter._
import org.jetbrains.annotations.NotNull

import scala.collection.JavaConversions._

object HaskellColorSettingsPage {
  private final val Attrs = Array[AttributesDescriptor](new AttributesDescriptor("Illegal character", Illegal), new AttributesDescriptor("Comment", Comment), new AttributesDescriptor("String", String), new AttributesDescriptor("Number", Number), new AttributesDescriptor("Keyword", Keyword), new AttributesDescriptor("Operator", Operator), new AttributesDescriptor("Parentheses", Parentheses), new AttributesDescriptor("Brace", Brace), new AttributesDescriptor("Bracket", Bracket), new AttributesDescriptor("Variable", Variable), new AttributesDescriptor("Constructor", Constructor))
  private final val AttributesKeyMap = Map[String, TextAttributesKey]()
}

class HaskellColorSettingsPage extends ColorSettingsPage {

  @NotNull
  def getDisplayName: String = {
    "Haskell"
  }

  def getIcon: Icon = {
    HaskellIcons.HaskellLogo
  }

  @NotNull
  def getAttributeDescriptors: Array[AttributesDescriptor] = {
    HaskellColorSettingsPage.Attrs
  }

  @NotNull
  def getColorDescriptors: Array[ColorDescriptor] = {
    ColorDescriptor.EMPTY_ARRAY
  }

  @NotNull
  def getHighlighter: SyntaxHighlighter = {
    new HaskellSyntaxHighlighter
  }

  @NotNull
  def getDemoText: String = {
    "module ModuleName\n" +
        "import ImportModuleName\n" +
        "\"string literal\"\n" +
        "'c'\n" +
        "x = (456,434)\n" +
        "-- line comment\n" +
        "{- nested \n" +
        "comment -}\n" +
        "data Bool = True | False\n" +
        "let l = [1,2] "
  }

  @NotNull
  def getAdditionalHighlightingTagToDescriptorMap: java.util.Map[String, TextAttributesKey] = {
    HaskellColorSettingsPage.AttributesKeyMap
  }
}