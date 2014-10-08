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

package com.powertuple.intellij.haskell.psi.impl;

import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class ResolveUtil {
    private ResolveUtil() {
    }

    public static boolean processChildren(@NotNull PsiElement element,
                                          @NotNull PsiScopeProcessor processor,
                                          @NotNull ResolveState substitutor,
                                          @Nullable PsiElement lastParent,
                                          @NotNull PsiElement place) {
        PsiElement run = lastParent == null ? element.getLastChild() : lastParent.getPrevSibling();
        while (run != null) {
            if (PsiTreeUtil.findCommonParent(place, run) != run && !run.processDeclarations(processor, substitutor, null, place)) {
                return false;
            }
            run = run.getPrevSibling();
        }
        return true;
    }
}
