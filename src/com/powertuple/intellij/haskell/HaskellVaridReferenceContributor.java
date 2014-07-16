package com.powertuple.intellij.haskell;

import com.intellij.openapi.util.TextRange;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.*;
import com.intellij.util.ProcessingContext;
import com.powertuple.intellij.haskell.psi.HaskellVarid;
import org.jetbrains.annotations.NotNull;

public class HaskellVaridReferenceContributor extends PsiReferenceContributor {
    @Override
    public void registerReferenceProviders(PsiReferenceRegistrar registrar) {
        registrar.registerReferenceProvider(PlatformPatterns.psiElement(HaskellVarid.class),
                new PsiReferenceProvider() {
                    @NotNull
                    @Override
                    public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
                        throw new RuntimeException("dada" );

//                        PsiLiteralExpression literalExpression = (PsiLiteralExpression) element;
//                        String text = (String) literalExpression.getValue();
//                        if (text != null) {
//                            return new PsiReference[]{new HaskellVaridReference(element, new TextRange(8, text.length() + 1))};
//                        }
//                        return new PsiReference[0];
                    }
                });
    }
}