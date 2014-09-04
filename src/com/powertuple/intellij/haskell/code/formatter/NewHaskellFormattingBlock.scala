package com.powertuple.intellij.haskell.code.formatter

import java.util

import com.intellij.formatting._
import com.intellij.formatting.alignment.AlignmentStrategy
import com.intellij.lang.ASTNode
import com.intellij.psi.formatter.common.AbstractBlock
import com.intellij.psi.{PsiElement, TokenType}
import com.intellij.util.containers.ContainerUtil
import com.powertuple.intellij.haskell.psi.{HaskellConstr, HaskellTypes}
import org.jetbrains.annotations.Nullable

import scala.collection.JavaConversions._

class NewHaskellFormattingBlock(node: ASTNode, spacingBuilder: SpacingBuilder, defaultAlignmentStrategy: AlignmentStrategy) extends AbstractBlock(node, null, null) {

  private var subBlocks: Seq[Block] = _

  override def buildChildren(): util.List[Block] = {
    if (subBlocks == null) {
      subBlocks = buildSubBlocks
    }
    subBlocks
  }

  private def buildSubBlocks: Seq[Block] = {
    //    val blocks: List[Block] = Seq[Block]()

    val baseAlignment: Alignment = Alignment.createAlignment(true)
    val baseAlignment2: Alignment = Alignment.createAlignment(true)
    val alignmentStrategy: AlignmentStrategy = createOrGetAlignmentStrategy

      var child: ASTNode = myNode.getFirstChildNode
//      while (child != null) {
//        {
//          if (!shouldCreateBlockFor(child)) continue //todo: continue is not supported
//          blocks.add(createChildBlock(myNode, child, chopDownIfLongWrap, baseAlignment, baseAlignment2, alignmentStrategy, -1))
//        }
//        child = child.getTreeNext
//      }
    Seq()

  }

  private def shouldCreateBlockFor(node: ASTNode): Boolean = {
    node.getTextRange.getLength != 0 && node.getElementType != TokenType.WHITE_SPACE
  }

  @Nullable
  private def createOrGetAlignmentStrategy: AlignmentStrategy = {
    val psi: PsiElement = getNode.getPsi

    if (psi.isInstanceOf[HaskellConstr]) {
      AlignmentStrategy.createAlignmentPerTypeStrategy(ContainerUtil.list(HaskellTypes.HS_VERTICAL_BAR), HaskellTypes.HS_CONSTRS, true)
    } else {
      defaultAlignmentStrategy
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

}

object IndentProcessor {

  def getChildIndent(node: ASTNode): Indent = {
    Indent.getNormalIndent
  }
}