package intellij.haskell.cabal.highlighting;

import intellij.haskell.HaskellIcons;
import intellij.haskell.cabal.highlighting.CabalSyntaxHighlighter;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import com.intellij.openapi.options.colors.ColorDescriptor;
import com.intellij.openapi.options.colors.ColorSettingsPage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Map;

/**
 * Cabal color selector tab in IntelliJ -> Preferences -->
 */
public class CabalColorSettingsPage implements ColorSettingsPage {
    private static final AttributesDescriptor[] DESCRIPTORS = new AttributesDescriptor[] {
            new AttributesDescriptor("Keys", CabalSyntaxHighlighter.KEY),
            new AttributesDescriptor("Colon", CabalSyntaxHighlighter.COLON),
            new AttributesDescriptor("Sections", CabalSyntaxHighlighter.CONFIG),
            new AttributesDescriptor("Comments", CabalSyntaxHighlighter.COMMENT)
    };

    @Nullable
    @Override
    public Icon getIcon() {
        return HaskellIcons.CabalLogo();
    }

    @NotNull
    @Override
    public SyntaxHighlighter getHighlighter() {
        return new CabalSyntaxHighlighter();
    }

    @NotNull
    @Override
    public String getDemoText() {
        return  "-- Cabal example colors\n"+
                "name:                cab-ex\n" +
                "version:             0.1.0.0\n" +
                "synopsis:            Cabal example\n" +
                "author:              Joe Ninja\n" +
                "\n" +
                "executable cab-ex\n" +
                "  main-is:             Main.hs\n" +
                "  build-depends:       base >=4.7 && <4.8\n" +
                "  default-language:    Haskell2010\n";
    }

    @Nullable
    @Override
    public Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() {
        return null;
    }

    @NotNull
    @Override
    public AttributesDescriptor[] getAttributeDescriptors() {
        return DESCRIPTORS;
    }

    @NotNull
    @Override
    public ColorDescriptor[] getColorDescriptors() {
        return ColorDescriptor.EMPTY_ARRAY;
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Cabal";
    }
}
