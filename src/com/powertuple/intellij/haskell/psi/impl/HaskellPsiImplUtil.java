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

package com.powertuple.intellij.haskell.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ArrayUtil;
import com.powertuple.intellij.haskell.HaskellIcons;
import com.powertuple.intellij.haskell.psi.*;
import com.powertuple.intellij.haskell.util.HaskellFindUtil$;
import org.jetbrains.annotations.Nullable;
import scala.Option;

import javax.swing.*;

public class HaskellPsiImplUtil {

    public static String getName(HaskellVar haskellVar) {
        return findFirstVarIdTokenChildPsiElementName(haskellVar);
    }

    public static PsiElement setName(HaskellVar haskellVar, String newName) {
        ASTNode keyNode = haskellVar.getNode().findChildByType(HaskellTypes.HS_VAR_ID);
        if (keyNode != null) {
            HaskellVar property = HaskellElementFactory.createVar(haskellVar.getProject(), newName);
            ASTNode newKeyNode = property.getFirstChild().getNode();
            haskellVar.getNode().replaceChild(keyNode, newKeyNode);
        }
        return haskellVar;
    }

    public static PsiElement getNameIdentifier(HaskellVar haskellVar) {
        return findFirstVarIdTokenChildPsiElement(haskellVar);
    }

    public static PsiReference getReference(HaskellVar haskellVar) {
        return ArrayUtil.getFirstElement(ReferenceProvidersRegistry.getReferencesFromProviders(haskellVar));
    }

    public static String getIdentifier(HaskellStartTypeSignature startTypeSignature) {
        return getHaskellVarName(startTypeSignature);
    }

    public static String getIdentifier(HaskellStartDefinition startDefinition) {
        return getHaskellVarName(startDefinition);
    }

    public static String getIdentifier(HaskellStartDataDeclaration startDataDeclaration) {
        PsiElement psiElement = findLastConIdTokenPsiElement(startDataDeclaration);
        return psiElement != null ? psiElement.getText() : null;
    }

    public static ItemPresentation getPresentation(final HaskellVar element) {
        return new ItemPresentation() {
            @Nullable
            @Override
            public String getPresentableText() {
                Option<String> typeSignature = HaskellFindUtil$.MODULE$.findTypeSignature(element);
                return typeSignature.isDefined() ? typeSignature.get() : element.getName();
            }

            @Nullable
            @Override
            public String getLocationString() {
                return element.getContainingFile().getName();
            }

            @Nullable
            @Override
            public Icon getIcon(boolean unused) {
                return HaskellIcons.HASKELL_SMALL_LOGO;
            }
        };
    }

    public static HaskellCon getNameIdentifier(HaskellStartDataDeclaration startDataDeclaration) {
        return findLastConIdTokenPsiElement(startDataDeclaration);
    }

    public static HaskellVar getNameIdentifier(HaskellStartTypeSignature startTypeSignature) {
        return getHaskellVar(startTypeSignature);
    }

    public static HaskellVar getNameIdentifier(HaskellStartDefinition startDefinition) {
        return getHaskellVar(startDefinition);
    }

    private static PsiElement findFirstVarIdTokenChildPsiElement(HaskellCompositeElement compositeElement) {
        ASTNode keyNode = compositeElement.getNode().findChildByType(HaskellTypes.HS_VAR_ID);
        return keyNode != null ? keyNode.getPsi() : null;
    }

    private static String findFirstVarIdTokenChildPsiElementName(HaskellCompositeElement compositeElement) {
        PsiElement psiElement = findFirstVarIdTokenChildPsiElement(compositeElement);
        return psiElement != null ? psiElement.getText() : null;
    }

    private static String getHaskellVarName(HaskellStartDeclarationElement startDeclarationElement) {
        HaskellVar haskellVar = getHaskellVar(startDeclarationElement);
        return haskellVar != null ? haskellVar.getName() : null;
    }

    private static HaskellVar getHaskellVar(HaskellStartDeclarationElement startDeclarationElement) {
        return PsiTreeUtil.findChildOfType(startDeclarationElement, HaskellVar.class);
    }

    private static HaskellCon findLastConIdTokenPsiElement(HaskellCompositeElement compositeElement) {
        ASTNode keyNode = compositeElement.getNode().getLastChildNode();
        return keyNode != null ? (HaskellCon) keyNode.getPsi() : null;
    }
}