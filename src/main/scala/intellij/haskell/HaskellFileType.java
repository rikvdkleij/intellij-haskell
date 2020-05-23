package intellij.haskell;

import com.intellij.lang.Language;
import com.intellij.openapi.fileTypes.LanguageFileType;
import icons.HaskellIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class HaskellFileType extends LanguageFileType {

    public static final HaskellFileType INSTANCE = new HaskellFileType(HaskellLanguage.Instance);

    protected HaskellFileType(@NotNull Language language) {
        super(language);
    }

    @NotNull
    @Override
    public String getName() {
        return "Haskell";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Haskell file";
    }

    @NotNull
    @Override
    public String getDefaultExtension() {
        return "hs";
    }

    @Nullable
    @Override
    public Icon getIcon() {
        return HaskellIcons.HaskellLogo;
    }
}
