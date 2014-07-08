/*
 * Copyright 2014 Rik van der Kleij

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

package com.powertuple.intellij.haskell.highlighter;

import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import com.intellij.openapi.options.colors.ColorDescriptor;
import com.intellij.openapi.options.colors.ColorSettingsPage;
import com.intellij.util.containers.ContainerUtil;
import com.powertuple.intellij.haskell.HaskellIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Map;

import static com.powertuple.intellij.haskell.highlighter.HaskellSyntaxHighlighter.*;

public class HaskellColorSettingsPage implements ColorSettingsPage {
    private static final AttributesDescriptor[] ATTRS = new AttributesDescriptor[]{
            new AttributesDescriptor("Illegal character", ILLEGAL),
            new AttributesDescriptor("Comment", COMMENT),
            new AttributesDescriptor("String", STRING),
            new AttributesDescriptor("Number", NUMBER),
            new AttributesDescriptor("Keyword", KEYWORD),
            new AttributesDescriptor("Operator", OPERATOR),
            new AttributesDescriptor("Parentheses", PARENTHESES),
            new AttributesDescriptor("Brace", BRACE),
            new AttributesDescriptor("Bracket", BRACKET),
            new AttributesDescriptor("Symbol", SYMBOL),
            new AttributesDescriptor("Constructor", CONSTRUCTOR),
    };

    private static Map<String, TextAttributesKey> ATTRIBUTES_KEY_MAP = ContainerUtil.newHashMap();

    static {
    }

    @NotNull
    public String getDisplayName() {
        return "Haskell";
    }

    public Icon getIcon() {
        return HaskellIcons.HASKELL_LOGO;
    }

    @NotNull
    public AttributesDescriptor[] getAttributeDescriptors() {
        return ATTRS;
    }

    @NotNull
    public ColorDescriptor[] getColorDescriptors() {
        return ColorDescriptor.EMPTY_ARRAY;
    }

    @NotNull
    public SyntaxHighlighter getHighlighter() {
        return new HaskellSyntaxHighlighter();
    }

    @NotNull
    public String getDemoText() {
        return "module ModuleName\n" +
                "import ImportModuleName\n" +
                "\"string literal\"\n" +
                "'char'\n" +
                "x = (456,434)\n" +
                "-- line comment\n" +
                "{- nested \n" +
                "comment -}\n" +
                "data Bool = True | False\n" +
                "let l = [1,2] ";
    }

    @NotNull
    public Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() {
        return ATTRIBUTES_KEY_MAP;
    }
}