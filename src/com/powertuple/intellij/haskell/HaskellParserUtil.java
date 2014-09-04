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

package com.powertuple.intellij.haskell;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.parser.GeneratedParserUtilBase;
import com.intellij.psi.TokenType;
import com.powertuple.intellij.haskell.psi.HaskellTypes;

public class HaskellParserUtil extends GeneratedParserUtilBase {
    public static boolean notReservedop(PsiBuilder builder, int level) {
//        return !(HaskellParserDefinition.RESERVED_OPERATORS().contains(builder.rawLookup(0)) && (builder.rawLookup(1) == TokenType.WHITE_SPACE || builder.rawLookup(1) == HaskellTypes.HS_RIGHT_PAREN));
//        return !(builder.rawLookup(0) == HaskellTypes.HS_DOT_DOT && builder.rawLookup(1) == HaskellTypes.HS_RIGHT_PAREN);
        return true;
    }

    public static boolean notDashes(PsiBuilder builder, int level) {
        return !(builder.rawLookup(0) == HaskellTypes.HS_DASH && builder.rawLookup(1) == HaskellTypes.HS_DASH);
    }

    public static boolean containsSpaces(PsiBuilder builder, int level) {
        return (builder.rawLookup(0) == HaskellTypes.HS_NEWLINE && builder.rawLookup(1) == TokenType.WHITE_SPACE) ||
                builder.rawLookup(0) == HaskellTypes.HS_NEWLINE && builder.rawLookup(1) == HaskellTypes.HS_NEWLINE && builder.rawLookup(2) == TokenType.WHITE_SPACE;
    }
}
