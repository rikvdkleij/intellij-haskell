/*
 * Copyright 2014-2020 Rik van der Kleij
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

package intellij.haskell.psi.impl

import com.intellij.navigation.ItemPresentation
import com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry
import com.intellij.psi.{PsiElement, PsiReference}
import com.intellij.util.ArrayUtil
import icons.HaskellIcons
import intellij.haskell.HaskellFileType
import intellij.haskell.psi._
import intellij.haskell.util.StringUtil
import javax.swing._

import scala.jdk.CollectionConverters._

object HaskellPsiImplUtil {

  def getName(qVarCon: HaskellQVarCon): String = {
    qVarCon.getText
  }

  def toString(varid: HaskellVarid): String = {
    s"HaskellVarid(${varid.getElementType})"
  }

  def toString(varid: HaskellVarsym): String = {
    s"HaskellVarsym(${varid.getElementType})"
  }

  def toString(varid: HaskellConid): String = {
    s"HaskellVarid(${varid.getElementType})"
  }

  def toString(varid: HaskellModid): String = {
    s"HaskellVarid(${varid.getElementType})"
  }

  def getIdentifierElement(qVarCon: HaskellQVarCon): HaskellNamedElement = {
    Option(qVarCon.getVarid).orElse(Option(qVarCon.getQCon).map(_.getConid)).orElse(Option(qVarCon.getConsym)).orElse(Option(qVarCon.getVarsym)).
      getOrElse(throw new IllegalStateException(s"Identifier for ${qVarCon.getText} should exist"))
  }

  def getName(varCon: HaskellVarCon): String = {
    varCon.getText
  }

  def getIdentifierElement(varCon: HaskellVarCon): HaskellNamedElement = {
    Option(varCon.getConid).
      orElse(Option(varCon.getConsym)).
      orElse(Option(varCon.getVarid)).
      orElse(Option(varCon.getVarsym)).
      getOrElse(throw new IllegalStateException(s"Identifier for ${varCon.getText} should exist"))
  }

  def getName(qName: HaskellQName): String = {
    Option(qName.getVarCon).map(_.getName).orElse(Option(qName.getQVarCon).map(_.getName)).getOrElse(qName.getText)
  }

  def getIdentifierElement(qName: HaskellQName): HaskellNamedElement = {
    Option(qName.getVarCon).map(_.getIdentifierElement).
      orElse(Option(qName.getQVarCon).map(_.getIdentifierElement)).
      getOrElse(throw new IllegalStateException(s"Identifier for ${qName.getText} should exist"))
  }

  def getQualifierName(qName: HaskellQName): Option[String] = {
    Option(qName.getQVarCon).flatMap(qvc =>
      Option(qvc.getQualifier).map(_.getName).
        orElse(Option(qvc.getQCon).flatMap(qc => Option(qc.getQConQualifier1).orElse(Option(qc.getQConQualifier2)).orElse(Option(qc.getQConQualifier3)).map(_.getText))))
  }

  def getName(modid: HaskellModid): String = {
    modid.getText
  }

  def getNameIdentifier(modid: HaskellModid): HaskellNamedElement = {
    modid
  }

  def setName(modid: HaskellModid, newName: String): PsiElement = {
    val newModid = HaskellElementFactory.createModid(modid.getProject, newName)
    newModid.foreach(modid.replace)
    modid
  }

  def getName(varid: HaskellVarid): String = {
    varid.getText
  }

  def getNameIdentifier(varId: HaskellVarid): HaskellNamedElement = {
    varId
  }

  def setName(varid: HaskellVarid, newName: String): PsiElement = {
    val newVarid = HaskellElementFactory.createVarid(varid.getProject, newName)
    newVarid.foreach(varid.replace)
    varid
  }

  def getName(varsym: HaskellVarsym): String = {
    varsym.getText
  }

  def getNameIdentifier(varsym: HaskellVarsym): HaskellNamedElement = {
    varsym
  }

  def setName(varsym: HaskellVarsym, newName: String): PsiElement = {
    val newVarsym = HaskellElementFactory.createVarsym(varsym.getProject, newName)
    newVarsym.foreach(varsym.replace)
    varsym
  }

  def getName(conid: HaskellConid): String = {
    conid.getText
  }

  def getNameIdentifier(conid: HaskellConid): HaskellNamedElement = {
    conid
  }

  def setName(conid: HaskellConid, newName: String): PsiElement = {
    val newConid = HaskellElementFactory.createConid(conid.getProject, newName)
    newConid.foreach(conid.replace)
    conid
  }

  def getName(consym: HaskellConsym): String = {
    consym.getText
  }

  def getNameIdentifier(consym: HaskellConsym): HaskellNamedElement = {
    consym
  }

  def setName(consym: HaskellConsym, newName: String): PsiElement = {
    val newConsym = HaskellElementFactory.createConsym(consym.getProject, newName)
    newConsym.foreach(consym.replace)
    consym
  }

  def getName(qualifier: HaskellQualifier): String = {
    qualifier.getText
  }

  def getNameIdentifier(qualifier: HaskellQualifier): HaskellNamedElement = {
    qualifier
  }

  def setName(qualifier: HaskellQualifier, newName: String): PsiElement = {
    val newQualifier = HaskellElementFactory.createQualifier(qualifier.getProject, removeFileExtension(newName))
    newQualifier.foreach(qualifier.replace)
    qualifier
  }

  def getName(qConQualifier: HaskellQualifierElement): String = {
    qConQualifier.getText
  }

  def getNameIdentifier(qConQualifier: HaskellQualifierElement): HaskellNamedElement = {
    qConQualifier
  }

  def setName(qualifier: HaskellQualifierElement, newName: String): PsiElement = {
    val newQualifier = HaskellElementFactory.createQConQualifier(qualifier.getProject, newName)
    newQualifier.foreach(qualifier.replace)
    qualifier
  }

  def getReference(element: PsiElement): PsiReference = {
    ArrayUtil.getFirstElement(ReferenceProvidersRegistry.getReferencesFromProviders(element))
  }

  private abstract class HaskellItemPresentation(haskellElement: PsiElement) extends ItemPresentation {

    def getLocationString: String = {
      val psiFile = haskellElement.getContainingFile.getOriginalFile
      HaskellPsiUtil.findModuleDeclaration(psiFile).flatMap(_.getModuleName).getOrElse("Unknown module")
    }

    def getIcon(unused: Boolean): Icon = {
      findIcon(haskellElement)
    }

    protected def findIcon(element: PsiElement): Icon = {
      import icons.HaskellIcons._
      element match {
        case _: HaskellTypeDeclaration => Type
        case _: HaskellDataDeclaration => Data
        case _: HaskellNewtypeDeclaration => NewType
        case _: HaskellClassDeclaration => Class
        case _: HaskellInstanceDeclaration => Instance
        case _: HaskellDefaultDeclaration => Default
        case _: HaskellTypeSignature => HaskellSmallBlueLogo
        case _: HaskellForeignDeclaration => Foreign
        case _: HaskellTypeFamilyDeclaration => TypeFamily
        case _: HaskellTypeInstanceDeclaration => TypeInstance
        case _: HaskellModuleDeclaration => Module
        case _ => HaskellSmallBlueLogo
      }
    }
  }

  def getPresentation(namedElement: HaskellNamedElement): ItemPresentation = {

    new HaskellItemPresentation(namedElement) {

      def getPresentableText: String = {
        namedElement.getName
      }

      override def getIcon(unused: Boolean): Icon = {
        HaskellIcons.HaskellSmallBlueLogo
      }
    }
  }

  def getPresentation(declarationElement: HaskellDeclarationElement): ItemPresentation = {

    new HaskellItemPresentation(declarationElement) {
      def getPresentableText: String = {
        getDeclarationText(declarationElement)
      }
    }
  }

  def getItemPresentableText(element: PsiElement): String = {
    HaskellPsiUtil.findNamedElement(element) match {
      case Some(namedElement) =>
        HaskellPsiUtil.findHighestDeclarationElement(element) match {
          case Some(de) => getDeclarationText(de)
          case _ => HaskellPsiUtil.findExpression(namedElement).map(_.getText).getOrElse(namedElement.getName)
        }
      case _ => element.getText
    }
  }

  private def getDeclarationText(declarationElement: HaskellDeclarationElement): String = {
    declarationElement match {
      case md: HaskellModuleDeclaration => s"module  ${md.getModid.getName}"
      case de => StringUtil.sanitizeDeclaration(de.getText)
    }
  }

  def getName(declarationElement: HaskellDeclarationElement): String = {
    declarationElement.getPresentation.getPresentableText
  }

  def getIdentifierElements(typeSignature: HaskellTypeSignature): Seq[HaskellNamedElement] = {
    typeSignature.getQNamesList.asScala.flatMap(_.getQNameList.asScala).map(_.getIdentifierElement).toSeq
  }

  def getIdentifierElements(dataDeclaration: HaskellDataDeclaration): Seq[HaskellNamedElement] = {
    val constrs = dataDeclaration.getConstrList.asScala
    dataDeclaration.getSimpletype.getIdentifierElements ++
      dataDeclaration.getTypeSignatureList.asScala.flatMap(_.getIdentifierElements) ++
      constrs.flatMap(constr => Option(constr.getConstr1)).flatMap(c => Option(c.getQName).map(_.getIdentifierElement) ++
        c.getFielddeclList.asScala.flatMap(_.getQNames.getQNameList.asScala.headOption.map(_.getIdentifierElement))) ++
      constrs.flatMap(constr => Option(constr.getConstr3)).flatMap(_.getTtypeList.asScala.headOption.flatMap(_.getQNameList.asScala.headOption.map(_.getIdentifierElement))) ++
      constrs.flatMap(constr => Option(constr.getConstr2)).flatMap(c => c.getQNameList.asScala.map(_.getIdentifierElement).find(e => e.isInstanceOf[HaskellConsym] || e.isInstanceOf[HaskellConid]).toSeq ++
        c.getTtypeList.asScala.headOption.flatMap(_.getQNameList.asScala.headOption.map(_.getIdentifierElement)))
  }

  def getIdentifierElements(typeDeclaration: HaskellTypeDeclaration): Seq[HaskellNamedElement] = {
    typeDeclaration.getSimpletype.getIdentifierElements
  }

  def getIdentifierElements(newtypeDeclaration: HaskellNewtypeDeclaration): Seq[HaskellNamedElement] = {
    val fielddecl = Option(newtypeDeclaration.getNewconstr.getNewconstrFielddecl)
    newtypeDeclaration.getSimpletype.getIdentifierElements ++
      //      fielddecl.flatMap(_.getTtype.getQNameList.asScala.headOption).map(_.getIdentifierElement)).getOrElse(Seq()) ++
      fielddecl.map(_.getQNameList.asScala.map(_.getIdentifierElement)).getOrElse(Seq()) ++
      newtypeDeclaration.getNewconstr.getQNameList.asScala.headOption.map(_.getIdentifierElement).toSeq
  }

  def getIdentifierElements(classDeclaration: HaskellClassDeclaration): Seq[HaskellNamedElement] = {
    val cdecls = Option(classDeclaration.getCdecls).map(_.getCdeclList.asScala).getOrElse(Seq())
    classDeclaration.getQNameList.asScala.headOption.map(_.getIdentifierElement).toSeq ++
      cdecls.flatMap(cd => Option(cd.getTypeSignature).map(_.getIdentifierElements).getOrElse(Seq())) ++
      cdecls.flatMap(cd => Option(cd.getCdeclDataDeclaration).toSeq.flatMap(_.getQNameList.asScala.map(_.getIdentifierElement)))
  }

  def getIdentifierElements(instanceDeclaration: HaskellInstanceDeclaration): Seq[HaskellNamedElement] = {
    Option(instanceDeclaration.getQName).map(_.getIdentifierElement).toSeq ++
      Option(instanceDeclaration.getInst).map(_.getInstvarList.asScala.
        flatMap(v => Option(v.getQName).map(_.getIdentifierElement).orElse(Option(v.getTtype).flatMap(_.getQNameList.asScala.headOption.map(_.getIdentifierElement))))).getOrElse(Seq()) ++
      Option(instanceDeclaration.getInst).map(_.getGtyconList.asScala.flatMap(c => Option(c.getQName).map(_.getIdentifierElement))).getOrElse(Seq())
  }

  def getIdentifierElements(typeFamilyDeclaration: HaskellTypeFamilyDeclaration): Seq[HaskellNamedElement] = {
    val familyType = typeFamilyDeclaration.getTypeFamilyType
    familyType.getQNameList.asScala.map(_.getIdentifierElement).toSeq ++
      familyType.getQNamesList.asScala.flatMap(_.getQNameList.asScala.map(_.getIdentifierElement))
  }

  def getIdentifierElements(derivingDeclaration: HaskellDerivingDeclaration): Seq[HaskellNamedElement] = {
    Seq(derivingDeclaration.getQName.getIdentifierElement)
  }

  def getIdentifierElements(typeInstanceDeclaration: HaskellTypeInstanceDeclaration): Seq[HaskellNamedElement] = {
    Seq()
  }

  def getIdentifierElements(simpleType: HaskellSimpletype): Seq[HaskellNamedElement] = {
    simpleType.getQNameList.asScala.map(_.getIdentifierElement).toSeq ++ {
      Option(simpleType.getTtype) match {
        case Some(t) => t.getQNameList.asScala.map(_.getIdentifierElement)
        case None => simpleType.getQNameList.asScala.map(_.getIdentifierElement)
      }
    }.filter(e => e.isInstanceOf[HaskellConid] || e.isInstanceOf[HaskellConsym])
  }

  def getIdentifierElements(defaultDeclaration: HaskellDefaultDeclaration): Seq[HaskellNamedElement] = {
    Seq()
  }

  def getIdentifierElements(foreignDeclaration: HaskellForeignDeclaration): Seq[HaskellNamedElement] = {
    Seq()
  }

  def getIdentifierElements(moduleDeclaration: HaskellModuleDeclaration): Seq[HaskellNamedElement] = {
    Seq(moduleDeclaration.getModid)
  }

  def getModuleName(importDeclaration: HaskellImportDeclaration): Option[String] = {
    Option(importDeclaration.getModid).map(_.getName)
  }

  def getModuleName(declarationElement: HaskellDeclarationElement): Option[String] = {
    Option(declarationElement.getPresentation).map(_.getLocationString)
  }

  def getModuleName(moduleDeclaration: HaskellModuleDeclaration): Option[String] = {
    Some(moduleDeclaration.getModid.getName)
  }

  def getDataTypeConstructor(dataConstructorDeclaration: HaskellDataConstructorDeclarationElement): HaskellNamedElement = {
    dataConstructorDeclaration.getIdentifierElements.head
  }

  def removeFileExtension(name: String): String = {
    val fileExtension = "." + HaskellFileType.INSTANCE.getDefaultExtension
    if (name.endsWith(fileExtension)) {
      name.replaceFirst(fileExtension, "")
    } else {
      name
    }
  }
}
