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

package intellij.haskell.code;

import com.intellij.codeInsight.CodeInsightSettings;
import com.intellij.codeInsight.editorActions.TypedHandlerDelegate;
import com.intellij.codeInsight.highlighting.BraceMatcher;
import com.intellij.codeInsight.highlighting.BraceMatchingUtil;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.highlighter.HighlighterIterator;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.text.CharArrayCharSequence;
import intellij.haskell.HaskellFile;
import org.jetbrains.annotations.NotNull;

/**
 * Credits to Erlang plugin for this code that automatically closes paired braces.
 */
public class HaskellTypedHandler extends TypedHandlerDelegate {

    @Override
    public Result charTyped(char c, Project project, @NotNull Editor editor, @NotNull PsiFile file) {
        if (!(file instanceof HaskellFile)) return super.charTyped(c, project, editor, file);

        if ((c != '{' && c != '-' && c != '#') ||
                !CodeInsightSettings.getInstance().AUTOINSERT_PAIR_BRACKET) {
            return Result.CONTINUE;
        }
        insertMatchedEndComment(project, editor, file, c);
        return Result.CONTINUE;
    }

    /**
     * this is almost complete c'n'p from TypedHandler,
     * This code should be generalized into BraceMatchingUtil to support custom matching braces for plugin developers
     *
     * @see com.intellij.codeInsight.editorActions.TypedHandler
     * @see com.intellij.codeInsight.highlighting.BraceMatchingUtil
     */
    private static void insertMatchedEndComment(Project project, Editor editor, PsiFile file, char c) {
        if (!(file instanceof HaskellFile)) return;

        PsiDocumentManager.getInstance(project).commitAllDocuments();

        FileType fileType = file.getFileType();
        int offset = editor.getCaretModel().getOffset();
        HighlighterIterator iterator = ((EditorEx) editor).getHighlighter().createIterator(offset);
        boolean atEndOfDocument = offset == editor.getDocument().getTextLength();

        if (!atEndOfDocument) iterator.retreat();
        if (iterator.atEnd()) return;
        BraceMatcher braceMatcher = BraceMatchingUtil.getBraceMatcher(fileType, iterator);
        if (iterator.atEnd()) return;
        IElementType braceTokenType = iterator.getTokenType();
        final CharSequence fileText = editor.getDocument().getCharsSequence();
        if (!braceMatcher.isLBraceToken(iterator, fileText, fileType)) return;

        if (!iterator.atEnd()) {
            iterator.advance();

            if (!iterator.atEnd()) {
                if (!BraceMatchingUtil.isPairedBracesAllowedBeforeTypeInFileType(braceTokenType, iterator.getTokenType(), fileType)) {
                    return;
                }
                if (BraceMatchingUtil.isLBraceToken(iterator, fileText, fileType)) {
                    return;
                }
            }

            iterator.retreat();
        }

        int lparenOffset = BraceMatchingUtil.findLeftmostLParen(iterator, braceTokenType, fileText, fileType);
        if (lparenOffset < 0) lparenOffset = 0;

        iterator = ((EditorEx) editor).getHighlighter().createIterator(lparenOffset);

        if (!BraceMatchingUtil.matchBrace(fileText, fileType, iterator, true, true)) {
            // Some other mechanism has put the closing '}' in the document already.
            editor.getDocument().insertString(offset, new CharArrayCharSequence(c));
        }
    }
}
