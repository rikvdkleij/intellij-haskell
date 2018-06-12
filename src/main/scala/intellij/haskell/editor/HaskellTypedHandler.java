/*
 * Copyright 2012-2014 Sergey Ignatov
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

package intellij.haskell.editor;

import com.intellij.codeInsight.CodeInsightSettings;
import com.intellij.codeInsight.editorActions.TypedHandlerDelegate;
import com.intellij.openapi.application.TransactionGuard;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.util.text.CharArrayCharSequence;
import intellij.haskell.HaskellFile;
import org.jetbrains.annotations.NotNull;

/**
 * Credits to Erlang plugin for the initial code that automatically closes paired braces.
 */
public class HaskellTypedHandler extends TypedHandlerDelegate {

    @NotNull
    @Override
    public Result charTyped(char c, @NotNull Project project, @NotNull Editor editor, @NotNull PsiFile psiFile) {
        if (!(psiFile instanceof HaskellFile)) return super.charTyped(c, project, editor, psiFile);

        if ((c != '-' && c != '#') || !CodeInsightSettings.getInstance().AUTOINSERT_PAIR_BRACKET) {
            return Result.CONTINUE;
        }

        TransactionGuard.getInstance().submitTransactionAndWait(() -> insertMatchedEndComment(project, editor, psiFile, c));
        return Result.CONTINUE;
    }

    /**
     * This is originally copied from TypedHandler,
     *
     * @see com.intellij.codeInsight.editorActions.TypedHandler
     */
    private static void insertMatchedEndComment(Project project, Editor editor, PsiFile psiFile, char c) {
        if (!(psiFile instanceof HaskellFile)) return;

        PsiDocumentManager.getInstance(project).commitAllDocuments();

        int offset = editor.getCaretModel().getOffset();
        final CharSequence fileText = editor.getDocument().getCharsSequence();

        if ((offset > 1 && c == '-' && fileText.charAt(offset - 2) == '{') || ((offset > 2 && c == '#' && fileText.charAt(offset - 3) == '{'))) {
            editor.getDocument().insertString(offset, new CharArrayCharSequence(c));
        }
    }
}
