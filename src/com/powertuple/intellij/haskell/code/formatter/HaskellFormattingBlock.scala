/*
 * Copyright 2015 Rik van der Kleij
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

package com.powertuple.intellij.haskell.code.formatter

import java.util

import com.intellij.formatting._
import com.intellij.lang.ASTNode
import com.intellij.psi.TokenType
import com.intellij.psi.formatter.common.AbstractBlock
import com.intellij.psi.impl.source.tree.TreeUtil
import com.intellij.psi.tree.IElementType
import com.powertuple.intellij.haskell.psi.HaskellTypes
import com.powertuple.intellij.haskell.psi.HaskellTypes._

import scala.annotation.tailrec
import scala.collection.JavaConversions._
import scala.collection.mutable

class HaskellFormattingBlock(node: ASTNode, alignment: Option[Alignment], spacingBuilder: SpacingBuilder, parentBlock: Block) extends AbstractBlock(node, null, alignment.orNull) {

  private var subBlocks: Option[Seq[Block]] = None

  override def buildChildren: util.List[Block] = {
    subBlocks match {
      case Some(b) => b
      case None =>
        val blocks = buildSubBlocks
        subBlocks = Some(blocks)
        blocks
    }
  }

  def buildSubBlocks: util.List[Block] = {
    val alignments = Seq.fill(9)(Alignment.createAlignment(true))

    val child: ASTNode = node.getFirstChildNode

    @tailrec
    def getBlocks(child: ASTNode, previousChild: ASTNode, blocks: mutable.Seq[Block]): Seq[Block] = {
      if (child == null) {
        blocks
      } else if (shouldCreateBlockFor(child, previousChild)) {
        getBlocks(child.getTreeNext, child, blocks.:+(createChildBlock(node, child, alignments)))
      } else {
        getBlocks(child.getTreeNext, child, blocks)
      }
    }
    getBlocks(child, null, mutable.Seq())
  }

  def createChildBlock(parent: ASTNode, child: ASTNode, alignments: Seq[Alignment]): Block = {
    new HaskellFormattingBlock(child, getAlignment(parent, child, alignments), spacingBuilder, this)
  }

  private def getAlignment(parent: ASTNode, child: ASTNode, alignments: Seq[Alignment]): Option[Alignment] = {
    val childType: IElementType = child.getElementType

    childType match {
      case HS_LEFT_PAREN | HS_RIGHT_PAREN | HS_LEFT_BRACE | HS_RIGHT_BRACE | HS_LEFT_BRACKET | HS_RIGHT_BRACKET | HS_COMMA => Some(alignments(0))
      case HS_VERTICAL_BAR | HS_EQUAL => Some(alignments(1))
      case HS_EXPORT => Some(alignments(2))
      case HS_IF | HS_THEN | HS_ELSE => Some(alignments(3))
      case HS_DO | HS_WHERE => Some(alignments(4))
      case HS_EXPORTS | HS_WHERE => Some(alignments(5))
      case HS_LINE_EXPRESSION | HS_LAST_LINE_EXPRESSION => Some(alignments(6))
      case HS_CONSTR_1 | HS_CONSTR_2 | HS_CONSTR_3 | HS_CONSTR_4 => Some(alignments(7))
      case _ => None
    }
  }

  private def shouldCreateBlockFor(node: ASTNode, previousNode: ASTNode): Boolean = {
    if (node.getElementType == HS_MODULE_BODY) {
      node.getChildren(null).length > 0
    }
    else {
      node.getElementType == HS_NEWLINE && previousNode != null && previousNode.getElementType == TokenType.WHITE_SPACE ||
          node.getElementType != TokenType.WHITE_SPACE && node.getElementType != HS_NEWLINE
    }
  }

  override def getIndent: Indent = {
    IndentProcessor.getChildIndent(node)
  }

  override def isLeaf: Boolean = {
    node.getFirstChildNode == null
  }

  override def getSpacing(child1: Block, child2: Block): Spacing = {
    spacingBuilder.getSpacing(this, child1, child2)
  }

  override def getChildAttributes(newChildIndex: Int): ChildAttributes = {
    new ChildAttributes(Indent.getNoneIndent, getFirstChildAlignment)
  }

  private def getFirstChildAlignment: Alignment = {
    getSubBlocks.find(_.getAlignment != null).map(_.getAlignment).orNull
  }
}

object IndentProcessor {

  def getChildIndent(child: ASTNode): Indent = {
    import com.intellij.formatting.Indent._

    val childType = child.getElementType
    childType match {
      case HS_MODULE_BODY | HS_MODULE_DECLARATION | HS_TOP_DECLARATION | HS_IMPORT_DECLARATION | HS_FIRST_LINE_EXPRESSION | HS_LINE_EXPRESSION | HS_LAST_LINE_EXPRESSION => getAbsoluteNoneIndent
      case HS_MODULE => getAbsoluteNoneIndent
      case HS_DO | HS_WHERE | HS_IF | HS_CASE | HS_DERIVING => getNormalIndent
      case HS_EQUAL | HS_QVAR_SYM => getNormalIndent
      case HS_VERTICAL_BAR => getNormalIndent
      case HS_IDECL | HS_CDECL => getNormalIndent
      case HS_CONSTR_1 | HS_CONSTR_2 | HS_CONSTR_3 | HS_CONSTR_4 => getNormalIndent
      case HS_COMMENT | HS_NCOMMENT => getNoneIndent
      case HS_LEFT_PAREN | HS_LEFT_BRACE | HS_LEFT_BRACKET
        if TreeUtil.findParent(child, HaskellTypes.HS_LINE_EXPRESSION) != null |
            TreeUtil.findParent(child, HaskellTypes.HS_MODULE_DECLARATION) != null |
            TreeUtil.findParent(child, HaskellTypes.HS_IMPORT_DECLARATION) != null => getContinuationIndent
      case _ => getNoneIndent
    }
  }
}