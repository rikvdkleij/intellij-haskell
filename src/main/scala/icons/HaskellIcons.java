package icons;

import com.intellij.openapi.util.IconLoader;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public interface HaskellIcons {
  @NotNull Icon HaskellLogo = IconLoader.getIcon("/icons/haskell@16.png");
  @NotNull Icon HaskellFileLogo = IconLoader.getIcon("/icons/hs.png");
  @NotNull Icon HaskellSmallBlueLogo = IconLoader.getIcon("/icons/haskell-blue@16.png");

  @NotNull Icon Module = IconLoader.getIcon("/icons/module.png");
  @NotNull Icon Data = IconLoader.getIcon("/icons/data.png");
  @NotNull Icon NewType = IconLoader.getIcon("/icons/newtype.png");
  @NotNull Icon Type = IconLoader.getIcon("/icons/type.png");
  @NotNull Icon Class = IconLoader.getIcon("/icons/class.png");
  @NotNull Icon Default = IconLoader.getIcon("/icons/default_declaration.png");
  @NotNull Icon TypeFamily = IconLoader.getIcon("/icons/type_family.png");
  @NotNull Icon TypeInstance = IconLoader.getIcon("/icons/type_instance.png");
  @NotNull Icon TypeSignature = IconLoader.getIcon("/icons/type_signature.png");
  @NotNull Icon Instance = IconLoader.getIcon("/icons/instance.png");
  @NotNull Icon Foreign = IconLoader.getIcon("/icons/foreign.png");

  @NotNull Icon CabalLogo = IconLoader.getIcon("/icons/cabal.png");

  @NotNull Icon AlexLogo = IconLoader.getIcon("/icons/haskell@16.png");
}
