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
