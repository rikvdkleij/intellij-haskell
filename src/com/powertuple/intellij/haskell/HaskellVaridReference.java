package com.powertuple.intellij.haskell;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.powertuple.intellij.haskell.psi.HaskellVarid;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class HaskellVaridReference extends PsiReferenceBase<PsiElement> implements PsiPolyVariantReference {
    private String key;

    public HaskellVaridReference(@NotNull PsiElement element, TextRange textRange) {
        super(element, textRange);
        key = element.getText().substring(textRange.getStartOffset(), textRange.getEndOffset());
    }

    @NotNull
    @Override
    public ResolveResult[] multiResolve(boolean incompleteCode) {
        Project project = myElement.getProject();
        final List<HaskellVarid> properties = HaskellUtil.findProperties(project, key);
        List<ResolveResult> results = new ArrayList<ResolveResult>();
        for (HaskellVarid property : properties) {
            results.add(new PsiElementResolveResult(property));
        }
        return results.toArray(new ResolveResult[results.size()]);
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        ResolveResult[] resolveResults = multiResolve(false);
        return resolveResults.length == 1 ? resolveResults[0].getElement() : null;
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        Project project = myElement.getProject();
        List<HaskellVarid> properties = HaskellUtil.findProperties(project);
        List<LookupElement> variants = new ArrayList<LookupElement>();
        for (final HaskellVarid property : properties) {
            if (property.getName() != null && property.getName().length() > 0) {
                variants.add(LookupElementBuilder.create(property).
                                withIcon(HaskellIcons.HASKELL_SMALL_LOGO).
                                withTypeText(property.getContainingFile().getName())
                );
            }
        }
        return variants.toArray();
    }
}