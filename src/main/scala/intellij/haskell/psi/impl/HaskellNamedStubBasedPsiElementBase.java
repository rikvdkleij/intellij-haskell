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

package intellij.haskell.psi.impl;

import com.intellij.extapi.psi.StubBasedPsiElementBase;
import com.intellij.lang.ASTNode;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.StubElement;
import intellij.haskell.psi.HaskellNamedElement;
import org.jetbrains.annotations.NotNull;

public abstract class HaskellNamedStubBasedPsiElementBase<T extends StubElement<?>> extends StubBasedPsiElementBase<T> implements HaskellNamedElement {

    HaskellNamedStubBasedPsiElementBase(@NotNull T stub, IStubElementType nodeType) {
        super(stub, nodeType);
    }

    HaskellNamedStubBasedPsiElementBase(ASTNode node) {
        super(node);
    }
}
