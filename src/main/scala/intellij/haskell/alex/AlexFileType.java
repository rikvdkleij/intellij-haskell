package intellij.haskell.alex;

import com.intellij.lang.Language;
import com.intellij.openapi.fileTypes.LanguageFileType;
import icons.HaskellIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class AlexFileType extends LanguageFileType {

    public static final AlexFileType INSTANCE = new AlexFileType(AlexLanguage.Instance);

    protected AlexFileType(@NotNull Language language) {
        super(language);
    }

    @NotNull
    @Override
    public String getName() {
        return "Alex";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Alex source file (Haskell lexer generator)";
    }

    @NotNull
    @Override
    public String getDefaultExtension() {
        return "x";
    }

    @Nullable
    @Override
    public Icon getIcon() {
        return HaskellIcons.AlexLogo;
    }
}
