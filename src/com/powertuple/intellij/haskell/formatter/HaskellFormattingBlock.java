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

package com.powertuple.intellij.haskell.formatter;

import com.intellij.formatting.*;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.TokenType;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.psi.formatter.FormatterUtil;
import com.intellij.psi.impl.source.tree.FileElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.powertuple.intellij.haskell.formatter.settings.HaskellCodeStyleSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.powertuple.intellij.haskell.psi.HaskellTypes.*;

/**
 * Formatter which use blank line as signal to back indent.
 * <p/>
 * TODO: Refactor this horrible code!! Scala??
 */
public class HaskellFormattingBlock implements ASTBlock {

    @NotNull
    protected final ASTNode node;
    @Nullable
    protected final Wrap wrap;
    @Nullable
    protected final Alignment alignment;
    private final CommonCodeStyleSettings settings;
    private final HaskellCodeStyleSettings haskellSettings;
    private final SpacingBuilder spacingBuilder;
    private List<Block> subBlocks;
    private Boolean incomplete;

    private int tabCounter;
    private int indentCounter;
    private static final TokenSet RESERVED_ELEMENTS_TO_INDENT = TokenSet.create(HS_VERTICAL_BAR);
    private static final TokenSet RESERVED_IDS_TO_INDENT = TokenSet.create(HS_WHERE, HS_THEN, HS_DO, HS_CASE);
    private static final TokenSet RESERVED_IDS_TO_BACK_INDENT = TokenSet.create(HS_ELSE);
    private static final TokenSet INDENT_PREV_ELEMENTS = TokenSet.create(HS_DO, HS_WHERE, HS_THEN, HS_ELSE, HS_OF);
    private static final TokenSet START_DEFINITION_ELEMENTS = TokenSet.create(HS_MODULE , HS_DATA, HS_INSTANCE, HS_CLASS,
            HS_IMPORT, HS_COMMENT, HS_NCOMMENT);

    public HaskellFormattingBlock(@NotNull ASTNode node,
                                  @NotNull CommonCodeStyleSettings settings,
                                  @NotNull HaskellCodeStyleSettings haskellSettings,
                                  @NotNull SpacingBuilder spacingBuilder,
                                  @Nullable Wrap wrap,
                                  int indentCounter,
                                  int tabCounter,
                                  @Nullable Alignment alignment) {
        this.node = node;
        this.wrap = wrap;
        this.alignment = alignment;
        this.settings = settings;
        this.haskellSettings = haskellSettings;
        this.spacingBuilder = spacingBuilder;
        this.indentCounter = indentCounter;
        this.tabCounter = tabCounter;
    }

    @NotNull
    @Override
    public ASTNode getNode() {
        return node;
    }

    @NotNull
    @Override
    public TextRange getTextRange() {
        return node.getTextRange();
    }

    @NotNull
    @Override
    public List<Block> getSubBlocks() {
        if (!(node instanceof FileElement)) {
            return Collections.emptyList();
        }

        if (subBlocks == null) {
            subBlocks = buildSubBlocks();
        }
        return new ArrayList<Block>(subBlocks);
    }

    private List<Block> buildSubBlocks() {
        final List<Block> blocks = new ArrayList<Block>();

        ASTNode prevNode = null;
        Alignment alignment = null;

        boolean startNewlineInDefinition = false;
        boolean startOfNewDefinition = false;
        boolean notIndentAgain = false;
        boolean newLine = false;
        boolean blankLine = false;

        for (ASTNode child = node.getFirstChildNode(); child != null; child = child.getTreeNext()) {
            if (child.getElementType() == HS_NEWLINE) {
                if (newLine) {
                    blankLine = true;
                } else {
                    newLine = true;
                }
            }

            if (typeExistsFor(child, START_DEFINITION_ELEMENTS) || (blankLine && child.getElementType() == HS_START_DEFINITION)) {
                startOfNewDefinition = true;
                indentCounter = 0;
                tabCounter = 0;
                alignment = null;
                notIndentAgain = false;
            }

            if (!startOfNewDefinition && newLine) {
                startNewlineInDefinition = true;
            }

            if (child.getElementType() == HS_NEWLINE || child.getElementType() == TokenType.WHITE_SPACE) {
                continue;
            }


            if (isReservedIdToIndent(child) && startNewlineInDefinition && !startOfNewDefinition) {
                doWhereSpecialCase(child);
                if (typeExistsFor(child, HS_WHERE)) {
                    if (indentCounter > 0) {
                        indentCounter--;
                    }
                }
                alignment = null;
            } else if (isReservedIdToBackIndent(child) && startNewlineInDefinition) {
                if (indentCounter > 0) {
                    indentCounter -= 1;
                } else {
                    indentCounter = 0;
                }
                alignment = null;
            } else if (isReservedElementToIndent(child) && startNewlineInDefinition) {
                if (!notIndentAgain) {
                    indentCounter += 1;
                    notIndentAgain = true;
                }
                notIndentAgain = true;
            } else if (prevNode != null) {
                if (typeExistsFor(prevNode, HS_EQUAL) && startNewlineInDefinition) {
                    indentCounter += 1;
                    alignment = Alignment.createAlignment(true);
                } else if (indentBecausePrevElement(prevNode) && !startOfNewDefinition && startNewlineInDefinition) {
                    doWhereSpecialCase(prevNode);
                    alignment = Alignment.createAlignment(true);
                } else if (indentBecausePrevElement(prevNode) && !startOfNewDefinition && !startNewlineInDefinition) {
                    alignment = Alignment.createAlignment(true);
                } else if (typeExistsFor(prevNode, TokenSet.create(HS_LEFT_PAREN, HS_DOLLAR)) && startNewlineInDefinition) {
                    indentCounter += 1;
                    alignment = Alignment.createAlignment(true);
                }
            }

            if (blankLine) {
                notIndentAgain = false;
                alignment = null;
                if (tabCounter > 0) {
                    tabCounter -= 1;
                } else if (indentCounter > 0) {
                    indentCounter -= 1;
                }
            }

            blocks.add(new HaskellFormattingBlock(child, settings, haskellSettings, spacingBuilder, getWrap(), indentCounter, tabCounter, alignment));

            newLine = false;
            blankLine = false;
            startOfNewDefinition = false;
            startNewlineInDefinition = false;
            prevNode = child;
        }

        return Collections.unmodifiableList(blocks);
    }

    private void doWhereSpecialCase(ASTNode node) {
        if (typeExistsFor(node, HS_DO)) {
            if (haskellSettings.INDENT_DO_WITH_TAB_SIZE) {
                tabCounter += 1;
            } else {
                indentCounter += 1;
            }
        } else if (typeExistsFor(node, HS_WHERE)) {
            if (haskellSettings.INDENT_WHERE_WITH_TAB_SIZE) {
                tabCounter += 1;
            } else {
                indentCounter += 1;
            }
        } else {
            indentCounter += 1;
        }
    }

    private boolean isReservedElementToIndent(ASTNode node) {
        return node.findChildByType(RESERVED_ELEMENTS_TO_INDENT) != null;
    }

    private boolean isReservedIdToIndent(ASTNode node) {
        return node.findChildByType(RESERVED_IDS_TO_INDENT) != null;
    }

    private boolean isReservedIdToBackIndent(ASTNode node) {
        return node.findChildByType(RESERVED_IDS_TO_BACK_INDENT) != null;
    }

    private boolean indentBecausePrevElement(ASTNode node) {
        return node.findChildByType(INDENT_PREV_ELEMENTS) != null;
    }

    private boolean typeExistsFor(ASTNode node, IElementType elementType) {
        return node.getElementType() == elementType || node.findChildByType(elementType) != null;
    }

    private boolean typeExistsFor(ASTNode node, TokenSet elementTypes) {
        return elementTypes.contains(node.getElementType()) || node.findChildByType(elementTypes) != null;
    }

    private int getTabSize() {
        return settings.getIndentOptions().TAB_SIZE;
    }

    private int getIndentSize() {
        return settings.getIndentOptions().INDENT_SIZE;
    }

    @Override
    @Nullable
    public Indent getIndent() {
        return Indent.getSpaceIndent((indentCounter * getIndentSize()) + (tabCounter * getTabSize()));
    }

    @Nullable
    @Override
    public Wrap getWrap() {
        return wrap;
    }

    @Nullable
    @Override
    public Alignment getAlignment() {
        return alignment;
    }

    @Nullable
    @Override
    public Spacing getSpacing(@Nullable Block child1, @NotNull Block child2) {
        return spacingBuilder.getSpacing(this, child1, child2);
    }

    @NotNull
    @Override
    public ChildAttributes getChildAttributes(int newChildIndex) {
        Block block = getSubBlocks().get(newChildIndex - 1);
        return new ChildAttributes(block.getIndent(), block.getAlignment());
    }

    @Override
    public boolean isIncomplete() {
        if (incomplete == null) {
            incomplete = FormatterUtil.isIncomplete(getNode());
        }
        return incomplete;
    }

    @Override
    public boolean isLeaf() {
        return true;
    }

    @Override
    public String toString() {
        return node.getText() + " " + getTextRange();
    }
}
