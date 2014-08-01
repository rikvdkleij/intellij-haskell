package com.powertuple.intellij.haskell

import com.intellij.codeInsight.completion._
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.patterns.PlatformPatterns
import com.intellij.util.ProcessingContext
import com.powertuple.intellij.haskell.psi.{HaskellTokenType, HaskellCon, HaskellVar}

class HaskellCompletionContributor extends CompletionContributor {

private final val ReservedIdNames =  HaskellParserDefinition.RESERVED_IDS.getTypes.map(_.asInstanceOf[HaskellTokenType].getName)

  extend(CompletionType.BASIC, PlatformPatterns.psiElement(), new CompletionProvider[CompletionParameters] {
    def addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
     val psiElement = parameters.getPosition
      val s = psiElement.getParent match {
        case e: HaskellVar => "blabla"
        case e: HaskellCon => "oef"
      }
      println("pa si: " + psiElement.getParent.getPrevSibling)
      println("si: " + psiElement.getPrevSibling)

      ReservedIdNames.foreach(k => result.addElement(LookupElementBuilder.create(k).withTypeText("Reserved id")))
//      result.addElement(LookupElementBuilder.create(s))
    }
  })

  override def beforeCompletion(context: CompletionInitializationContext): Unit = {
    context.setDummyIdentifier("a")
  }

}
