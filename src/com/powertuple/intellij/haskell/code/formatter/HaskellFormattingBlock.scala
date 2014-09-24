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

package com.powertuple.intellij.haskell.code.formatter

import java.util

import com.intellij.formatting._
import com.intellij.lang.ASTNode
import com.intellij.psi.TokenType
import com.intellij.psi.formatter.common.AbstractBlock
import com.intellij.psi.tree.IElementType
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
    def getBlocks(child: ASTNode, blocks: mutable.Seq[Block]): Seq[Block] = {
      if (child == null) {
        blocks
      } else if (shouldCreateBlockFor(child)) {
        getBlocks(child.getTreeNext, blocks.:+(createChildBlock(node, child, alignments)))
      } else {
        getBlocks(child.getTreeNext, blocks)
      }
    }
    getBlocks(child, mutable.Seq())
  }

  def createChildBlock(parent: ASTNode, child: ASTNode, alignments: Seq[Alignment]): Block = {
    new HaskellFormattingBlock(child, getAlignment(parent, child, alignments), spacingBuilder, this)
  }

  private def getAlignment(parent: ASTNode, child: ASTNode, alignments: Seq[Alignment]): Option[Alignment] = {
    val childType: IElementType = child.getElementType

    childType match {
      case HS_LEFT_PAREN | HS_LEFT_BRACE | HS_LEFT_BRACKET | HS_COMMA => Some(alignments(0))
      case HS_VERTICAL_BAR | HS_EQUAL => Some(alignments(1))
      case HS_EXPORT_1 | HS_EXPORT_2 | HS_EXPORT_3 => Some(alignments(2))
      case HS_LINE_EXPRESSION => Some(alignments(3))
      case HS_IF | HS_THEN | HS_ELSE => Some(alignments(4))
      case HS_DO | HS_WHERE | HS_EQUAL | HS_LET => Some(alignments(5))
      case HS_EXPORTS | HS_WHERE => Some(alignments(6))
      case _ => None
    }
  }

  private def shouldCreateBlockFor(node: ASTNode): Boolean = {
    node.getTextRange.getLength != 0 && node.getElementType != TokenType.WHITE_SPACE
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
}

object IndentProcessor {

  def getChildIndent(child: ASTNode): Indent = {
    val childType = child.getElementType

    childType match {
      case HS_LEFT_PAREN | HS_LEFT_BRACE | HS_LEFT_BRACKET => Indent.getNormalIndent(false)
      case HS_DO | HS_WHERE | HS_IF | HS_THEN | HS_CASE => Indent.getNormalIndent(false)
      case HS_CDECL | HS_IDECLS => Indent.getNormalIndent(false)
      case HS_EQUAL | HS_DOLLAR => Indent.getContinuationIndent(true)
      case HS_VERTICAL_BAR => Indent.getNormalIndent(false)
      case HS_LINE_EXPRESSION if child.getTreeParent.getElementType != HS_FIRST_LINE_EXPRESSION => Indent.getNormalIndent(false)
      case _ => Indent.getNoneIndent
    }
  }
}