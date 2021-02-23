package intellij.haskell.cabal;

import com.intellij.lang.Language;
import com.intellij.openapi.fileTypes.LanguageFileType;
import icons.HaskellIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class CabalFileType extends LanguageFileType {

    public static final CabalFileType INSTANCE = new CabalFileType(CabalLanguage.Instance);

    protected CabalFileType(@NotNull Language language) {
        super(language);
    }

    @NotNull
    @Override
    public String getName() {
        return "Cabal";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Cabal file (Haskell package description)";
    }

    @NotNull
    @Override
    public String getDefaultExtension() {
        return "cabal";
    }

    @Nullable
    @Override
    public Icon getIcon() {
        return HaskellIcons.CabalLogo;
    }
}
