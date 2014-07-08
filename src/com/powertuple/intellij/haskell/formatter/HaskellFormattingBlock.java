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
 * Formatter which use blank line as start of new definition.
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

    private boolean indentWithTabSize;
    private int indentCounter;
    private static final TokenSet ReservedIdsToIndent = TokenSet.create(HS_VERTICAL_BAR);
    public static final TokenSet RESERVED_IDS_TO_INDENT = TokenSet.create(HS_WHERE, HS_THEN, HS_DO);
    public static final TokenSet RESERVED_IDS_TO_BACK_INDENT = TokenSet.create(HS_ELSE);
    private final TokenSet INDENT_PREV_RESERVED_IDS = TokenSet.create(HS_WHERE, HS_DO, HS_THEN, HS_ELSE);

    public HaskellFormattingBlock(@NotNull ASTNode node,
                                  @NotNull CommonCodeStyleSettings settings,
                                  @NotNull HaskellCodeStyleSettings haskellSettings,
                                  @NotNull SpacingBuilder spacingBuilder,
                                  @Nullable Wrap wrap,
                                  int indentCounter,
                                  boolean indentWithTabSize,
                                  @Nullable Alignment alignment) {
        this.node = node;
        this.wrap = wrap;
        this.alignment = alignment;
        this.settings = settings;
        this.haskellSettings = haskellSettings;
        this.spacingBuilder = spacingBuilder;
        this.indentCounter = indentCounter;
        this.indentWithTabSize = indentWithTabSize;
    }

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
        if (subBlocks == null) {
            subBlocks = buildSubBlocks();
        }
        return new ArrayList<Block>(subBlocks);
    }

    private List<Block> buildSubBlocks() {
        final List<Block> blocks = new ArrayList<Block>();

        if (!(node instanceof FileElement)) {
            return Collections.unmodifiableList(blocks);
        }

        ASTNode prevNode = null;
        Alignment alignment = null;

        boolean startNewlineInDefinition = false;
        boolean startOfNewDefinition = false;
        boolean notIndentAgain = false;

        for (ASTNode child = node.getFirstChildNode(); child != null; child = child.getTreeNext()) {
            if (child.getElementType() == HS_NEWLINE) {
                if (startNewlineInDefinition) {
                    startOfNewDefinition = true;
                    indentCounter = 0;
                    alignment = null;
                    notIndentAgain = false;
                } else {
                    startNewlineInDefinition = true;
                }
                continue;
            } else if (child.getElementType() == TokenType.WHITE_SPACE) {
                continue;
            }

            indentWithTabSize = false;

            if (isReservedIdToIndent(child) && startNewlineInDefinition && !startOfNewDefinition) {
                if (typeExistsFor(child, HS_WHERE)) {
                    if (indentCounter > 0) {
                        indentCounter--;
                    }
                    if (haskellSettings.INDENT_WHERE_WITH_TAB_SIZE) {
                        indentWithTabSize = true;
                    } else {
                        indentCounter += 1;
                    }
                } else {
                    indentCounter += 1;
                }
                alignment = null;
                notIndentAgain = false;
            } else if (isReservedIdToBackIndent(child) && startNewlineInDefinition) {
                if (indentCounter > 0) {
                    indentCounter -= 1;
                } else {
                    indentCounter = 0;
                }
                alignment = null;
                notIndentAgain = false;
            } else if (isReservedOpToIndent(child) && startNewlineInDefinition && !notIndentAgain) {
                indentCounter += 1;
                notIndentAgain = true;
            } else if (prevNode != null) {
                if (typeExistsFor(prevNode, HS_EQUAL) && startNewlineInDefinition) {
                    indentCounter += 1;
                    alignment = Alignment.createAlignment(true);
                    notIndentAgain = false;
                } else if (indentBecausePrevReservedId(prevNode) && !startOfNewDefinition && startNewlineInDefinition) {
                    indentCounter += 1;
                    alignment = Alignment.createAlignment(true);
                    notIndentAgain = false;
                } else if ((prevNode.getElementType() == HS_LEFT_PAREN) && startNewlineInDefinition) {
                    indentCounter += 1;
                    alignment = Alignment.createAlignment(true);
                    notIndentAgain = false;
                }
            }

            blocks.add(new HaskellFormattingBlock(child, settings, haskellSettings, spacingBuilder, getWrap(), indentCounter, indentWithTabSize, alignment));

            startOfNewDefinition = false;
            startNewlineInDefinition = false;
            prevNode = child;
        }
        return Collections.unmodifiableList(blocks);
    }

    private boolean isReservedOpToIndent(ASTNode node) {
        return node.findChildByType(ReservedIdsToIndent) != null;
    }

    private boolean isReservedIdToIndent(ASTNode node) {
        return node.findChildByType(RESERVED_IDS_TO_INDENT) != null;
    }

    private boolean isReservedIdToBackIndent(ASTNode node) {
        return node.findChildByType(RESERVED_IDS_TO_BACK_INDENT) != null;
    }

    private boolean indentBecausePrevReservedId(ASTNode node) {
        return node.findChildByType(INDENT_PREV_RESERVED_IDS) != null;
    }

    private boolean typeExistsFor(ASTNode node, IElementType elementType) {
        return node.findChildByType(elementType) != null;
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
        return Indent.getSpaceIndent(indentCounter * getIndentSize() + (indentWithTabSize ? getTabSize() : 0));
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
