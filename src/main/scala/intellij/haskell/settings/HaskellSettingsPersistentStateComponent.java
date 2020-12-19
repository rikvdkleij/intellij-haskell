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

package intellij.haskell.settings;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import intellij.haskell.GlobalInfo;
import org.jetbrains.annotations.NotNull;

@State(
        name = "HaskellConfiguration",
        storages = {
                @Storage(file = "intellij-haskell.xml")
        }
)
public class HaskellSettingsPersistentStateComponent implements PersistentStateComponent<HaskellSettingsPersistentStateComponent.HaskellSettingsState> {

    private HaskellSettingsState haskellSettingsState = new HaskellSettingsState();

    @NotNull
    public static HaskellSettingsPersistentStateComponent getInstance() {
        final HaskellSettingsPersistentStateComponent haskellSettingsPersistentStateComponent = ServiceManager.getService(HaskellSettingsPersistentStateComponent.class);
        return haskellSettingsPersistentStateComponent != null ? haskellSettingsPersistentStateComponent : new HaskellSettingsPersistentStateComponent();
    }

    @NotNull
    public HaskellSettingsState getState() {
        return haskellSettingsState;
    }

    public void loadState(HaskellSettingsState haskellSettingsState) {
        this.haskellSettingsState = haskellSettingsState;
    }

    static class HaskellSettingsState {
        public Integer replTimeout = 30;
        public String hlintOptions = "";
        public Boolean useSystemGhc = false;
        public Boolean reformatCodeBeforeCommit = false;
        public Boolean optimizeImportsBeforeCommit = false;
        public String newProjectTemplateName = "new-template";
        public String cachePath = GlobalInfo.DefaultCachePath();
        public String hlintPath = "";
        public String hooglePath = "";
        public String ormoluPath = "";
        public String stylishHaskellPath = "";
        public Boolean customTools = false;
        public String extraStackArguments = "";
        public String defaultGhcOptions = "-Wall -Wcompat -Wincomplete-record-updates -Wincomplete-uni-patterns -Wredundant-constraints";
    }
}
