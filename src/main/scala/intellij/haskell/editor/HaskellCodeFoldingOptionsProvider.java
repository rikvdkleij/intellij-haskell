package intellij.haskell.editor;

import com.intellij.application.options.editor.CodeFoldingOptionsProvider;
import com.intellij.openapi.options.BeanConfigurable;

public class HaskellCodeFoldingOptionsProvider extends BeanConfigurable<HaskellFoldingSettings.State> implements CodeFoldingOptionsProvider {
    public HaskellCodeFoldingOptionsProvider() {
        super(HaskellFoldingSettings.getInstance().getState(), "Haskell");
        HaskellFoldingSettings settings = HaskellFoldingSettings.getInstance();
        checkBox("File header", settings::isCollapseFileHeader, value -> settings.getState().COLLAPSE_FILE_HEADER = value);
        checkBox("Imports", settings::isCollapseImports, value -> settings.getState().COLLAPSE_IMPORTS = value);
        checkBox("Top-level expression", settings::isCollapseTopLevelExpression, value -> settings.getState().COLLAPSE_TOP_LEVEL_EXPRESSION = value);
    }
}