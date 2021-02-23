package intellij.haskell.editor;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;

@State(name = "HaskellFoldingSettings", storages = {
        @Storage("editor.xml"),
        @Storage(value = "editor.codeinsight.xml", deprecated = true),
}, reportStatistic = true)
public class HaskellFoldingSettings implements PersistentStateComponent<HaskellFoldingSettings.State> {
    private final HaskellFoldingSettings.State myState = new State();

    public static HaskellFoldingSettings getInstance() {
        return ServiceManager.getService(HaskellFoldingSettings.class);
    }

    public boolean isCollapseImports() {
        return myState.COLLAPSE_IMPORTS;
    }

    public boolean isCollapseFileHeader() {
        return myState.COLLAPSE_FILE_HEADER;
    }

    public boolean isCollapseTopLevelExpression() {
        return myState.COLLAPSE_TOP_LEVEL_EXPRESSION;
    }

    @Override
    @NotNull
    public State getState() {
        return myState;
    }

    @Override
    public void loadState(@NotNull State state) {
        XmlSerializerUtil.copyBean(state, myState);
    }

    public static final class State {
        public boolean COLLAPSE_IMPORTS = false;
        public boolean COLLAPSE_FILE_HEADER = false;
        public boolean COLLAPSE_TOP_LEVEL_EXPRESSION = false;
    }
}