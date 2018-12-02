package intellij.haskell.alex;

import com.intellij.lang.Language;
import org.jetbrains.annotations.NotNull;

/**
 * @author ice1000
 */
public class AlexLanguage extends Language {
    public static final AlexLanguage Instance = new AlexLanguage();

    public AlexLanguage() {
        super("Alex", "text/x");
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Alex";
    }

    @Override
    public boolean isCaseSensitive() {
        return true;
    }
}
