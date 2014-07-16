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
import com.intellij.psi.PsiElement;
import com.powertuple.intellij.haskell.psi.HaskellElementFactory;
import com.powertuple.intellij.haskell.psi.HaskellTokenType;
import com.powertuple.intellij.haskell.psi.HaskellTypes;
import com.powertuple.intellij.haskell.psi.HaskellVarid;

public class HaskellPsiImplUtil {

    public static String getName(HaskellVarid element) {
        ASTNode keyNode = element.getNode().findChildByType(HaskellTypes.HS_VAR_ID);
        if (keyNode != null) {
            return keyNode.getText();
        } else {
            return null;
        }
    }

    public static PsiElement setName(HaskellVarid element, String newName) {
        ASTNode keyNode = element.getNode().findChildByType(HaskellTypes.HS_VAR_ID);
        if (keyNode != null) {
            HaskellVarid property = HaskellElementFactory.createVar(element.getProject(), newName);
            ASTNode newKeyNode = property.getFirstChild().getNode();
            element.getNode().replaceChild(keyNode, newKeyNode);
        }
        return element;
    }

    public static PsiElement getNameIdentifier(HaskellVarid haskellVarid) {
        ASTNode keyNode = haskellVarid.getNode().findChildByType(HaskellTypes.HS_VAR_ID);
        if (keyNode != null) {
            return keyNode.getPsi();
        } else {
            return null;
        }

    }
}