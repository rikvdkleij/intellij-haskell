/*
 * Copyright 2014-2017 Rik van der Kleij
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

package intellij.haskell.psi;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.parser.GeneratedParserUtilBase;
import com.intellij.psi.TokenType;

import static intellij.haskell.psi.HaskellTypes.*;

public class HaskellParserUtil extends GeneratedParserUtilBase {

    public static boolean containsSpaces(PsiBuilder builder, int level) {
        return (builder.rawLookup(0) == HS_NEWLINE && (builder.rawLookup(1) == TokenType.WHITE_SPACE || builder.rawLookup(1) == HS_COMMENT) || builder.rawLookup(1) == HS_NCOMMENT || builder.rawLookup(1) == HS_DIRECTIVE || builder.rawLookup(1) == HS_HADDOCK || builder.rawLookup(1) == HS_NHADDOCK) ||
                (builder.rawLookup(0) == HS_NEWLINE && builder.rawLookup(1) == HS_NEWLINE && builder.rawLookup(2) == TokenType.WHITE_SPACE) ||
                (builder.rawLookup(0) == HS_NEWLINE && builder.rawLookup(1) == HS_NEWLINE && builder.rawLookup(2) == HS_NEWLINE && builder.rawLookup(3) == TokenType.WHITE_SPACE) ||
                (builder.rawLookup(0) == HS_NEWLINE && builder.rawLookup(1) == HS_NEWLINE && builder.rawLookup(2) == HS_NEWLINE && builder.rawLookup(3) == HS_NEWLINE && builder.rawLookup(4) == TokenType.WHITE_SPACE) ||
                (builder.rawLookup(0) == HS_NEWLINE && builder.rawLookup(1) == HS_NEWLINE && builder.rawLookup(2) == HS_NEWLINE && builder.rawLookup(3) == HS_NEWLINE && builder.rawLookup(4) == HS_NEWLINE && builder.rawLookup(5) == TokenType.WHITE_SPACE) ||
                (builder.rawLookup(0) == HS_NEWLINE && builder.rawLookup(1) == HS_NEWLINE && builder.rawLookup(2) == HS_NEWLINE && builder.rawLookup(3) == HS_NEWLINE && builder.rawLookup(4) == HS_NEWLINE && builder.rawLookup(5) == HS_NEWLINE && builder.rawLookup(6) == TokenType.WHITE_SPACE) ||
                (builder.rawLookup(0) == HS_NEWLINE && builder.rawLookup(1) == HS_NEWLINE && builder.rawLookup(2) == HS_NEWLINE && builder.rawLookup(3) == HS_NEWLINE && builder.rawLookup(4) == HS_NEWLINE && builder.rawLookup(5) == HS_NEWLINE && builder.rawLookup(6) == HS_NEWLINE && builder.rawLookup(7) == TokenType.WHITE_SPACE);
    }

    public static boolean noSpaceAfterQualifier(PsiBuilder builder, int level) {
        return (builder.rawLookup(0) == HS_CON_ID && builder.rawLookup(1) == HS_DOT);
    }
}
