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

package com.powertuple.intellij.haskell.settings;

import com.intellij.openapi.components.*;
import org.jetbrains.annotations.NotNull;

@State(
        name = "HaskellConfiguration",
        storages = {
                @Storage(id = "default", file = StoragePathMacros.APP_CONFIG + "/haskell.xml")
        }
)
public class HaskellSettings implements PersistentStateComponent<HaskellSettings.HaskellSettingsState> {

    @NotNull
    public static HaskellSettings getInstance() {
        HaskellSettings persisted = ServiceManager.getService(HaskellSettings.class);
        if (persisted == null) {
            persisted = new HaskellSettings();
        }

        if (persisted.getState().ghcModPath == null) {
            persisted.getState().ghcModPath = "ghc-mod";
        }

        if (persisted.getState().ghcModiPath == null) {
            persisted.getState().ghcModiPath = "ghc-modi";
        }

        if (persisted.getState().hdocsPath == null) {
            persisted.getState().hdocsPath = "hdocs";
        }

        return persisted;
    }

    public static class HaskellSettingsState {
        public String ghcModPath;
        public String ghcModiPath;
        public String hdocsPath;
    }

    private HaskellSettingsState haskellSettingsState = new HaskellSettingsState();

    @NotNull
    public HaskellSettingsState getState() {
        return haskellSettingsState;
    }

    public void loadState(HaskellSettingsState haskellSettingsState) {
        this.haskellSettingsState = haskellSettingsState;
    }
}
