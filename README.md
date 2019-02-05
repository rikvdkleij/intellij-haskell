# ![logo](logo/icon_intellij_haskell_32.png) IntelliJ plugin for Haskell

[![Join the chat at https://gitter.im/intellij-haskell/Lobby](https://badges.gitter.im/intellij-haskell/Lobby.svg)](https://gitter.im/intellij-haskell/Lobby?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

When I was learning Haskell, I missed the nice features of IntelliJ IDEA. My first approach
was to use default way of creating an IntelliJ plugin by defining a grammar and a lexer according to
[Haskell report](http://www.haskell.org/onlinereport/haskell2010/haskellch10.html). That didn't work out because I could not define all 
the recursion. 
Then I decided to use grammar and lexer definitions only for tokenizing and parsing Haskell code, and not for syntax checking the code. This is needed for syntax highlighting, all kinds of navigation and so on.
Further Haskell language support is provided with the help of external tools.

This plugin depends mainly on Stack and Intero. It can create new Stack projects and import existing Stack projects.
 
Any feedback is welcome!!

# Installing the plugin
You can install this plugin using the [Jetbrains plugin repository](https://plugins.jetbrains.com/idea/plugin/8258-intellij-haskell):
  `Settings`/`Plugins`/`Browse repositories`/`Intellij-Haskell`

# Installing latest beta of the plugin
To try out the latest beta version one can install the plugin by adding `https://plugins.jetbrains.com/plugins/alpha/8258` as Custom plugin repository in `Settings`/`Plugins`/`Browse repositories`/`Manage repositories`.
 
Alternative way to install the latest beta version is to download `intellij-haskell.zip` from [releases](https://github.com/rikvdkleij/intellij-haskell/releases) and apply `Install plugin from disk` in `Settings`/`Plugins`.


# Features
- Syntax highlighting;
- Error/warning highlighting;
- Find usages of identifiers;
- Resolve references to identifiers;
- Code completion;
- In-place rename identifiers;
- View type info from (selected) expression;
- View sticky type info;
- View expression info;
- View quick documentation;
- View quick definition;
- Structure view;
- Goto to declaration (called `Navigate`>`Declaration` in IntelliJ menu);
- Navigate to declaration (called `Navigate`>`Class` in IntelliJ menu);
- Navigate to identifier (called `Navigate`>`Symbol` in IntelliJ menu);
- Goto instance declaration (called `Navigate`>`Instance Declaration` in IntelliJ menu);
- Navigate to declaration or identifier powered by Hoogle (called `Navigate`>`Navigation by Hoogle` in IntelliJ menu);
- Inspection by HLint;
- Quick fixes for HLint suggestions;
- Show error action to view formatted message. Useful in case message consists of multiple lines (Ctrl-F10, Meta-F10 on Mac OSX);
- Intention actions to add language extension (depends on compiler error), add top-level type signature (depends on compiler warning);
- Intention action to select which module to import if identifier is not in scope;
- Code formatting with `hindent`, `stylish-haskell`, or both together. `hindent` can also be used to format a selected code block.
- Code completion for project module names, language extensions and package names in Cabal file;
- Running REPL, tests and executables via `Run Configurations`;
- Haskell Problems View;

### Usage with `hindent`

When used with `hindent`, `intellij-haskell` automatically sets `--indent-size` as a command-line option in `hindent` from the `Indent` option in your project code style settings. It also automatically sets the `--line-length` command-line option from your `Right margin (columns)` code style setting. This means that any `.hindent.yaml` files used for configuration will have these options overridden and may not fully work.


# Getting started
- If you don't already have IntelliJ, [download it](https://www.jetbrains.com/idea/download/) - the Community Edition is sufficient.
- Install this plugin using the [Jetbrains plugin repository](https://plugins.jetbrains.com/idea/plugin/8258-intellij-haskell): `Settings`/`Plugins`/`Browse repositories`/`Intellij-Haskell`. Make sure no other Haskell plugin is installed in IntelliJ;
- Install latest version of [Stack](https://github.com/commercialhaskell/stack); use `stack upgrade` to confirm you are on the latest version.
- Setup the project:
  - Make sure your Stack project builds without errors. Preferably by using: `stack build --test --haddock`;
  - After your project is built successfully, import project in IntelliJ by using `File`>`New`>`Project from Existing Sources...` from the IntelliJ menu;
  - In the `New Project` wizard select `Import project from external model` and check `Haskell Stack`;
  - In next page of wizard configure `Project SDK` by configuring `Haskell Tool Stack` with selecting path to `stack` binary, e.g. `/usr/local/bin/stack`;
  - Finish wizard and project will be opened;
  - Wizard will automatically configure which folders are sources, test and which to exclude;
  - Plugin will automatically build Intero and Haskell Tools (HLint, Hoogle, Hindent and Stylish Haskell) to prevent incompatibility issues
  - Check `Project structure`>`Project settings`>`Modules` which folders to exclude (like `.stack-work` and `dist`) and which folders are `Source` and `Test` (normally `src` and `test`);
  - Plugin will automatically download library sources. They will be added as source libraries to module(s).
  - After changing the Cabal file and/or `stack.yaml` use `Tools`>`Haskell`>`Update Settings and Restart REPLs` to download missing library sources and update the project settings;
  - The `Event Log` will display what's going on in the background. Useful when something fails. It's disabled by default. 
    It can be enabled by checking `Haskell Log` checkbox in the `Event Log`>`Settings` or `Settings`>`Appearance & Behavior`>`Notifications`;    


# Remarks
1. `About Haskell Project` in `Help` menu shows which Haskell GHC/tools are used by plugin for project;
2. Intero depends on `libtinfo-dev`. On Ubuntu you can install it with `sudo apt-get install libtinfo-dev`;
3. Haskell tools depends on `libgmp3-dev zlib1g-dev`. On Ubuntu you can install them with `sudo apt-get install libgmp3-dev zlib1g-dev`;
4. Cabal's internal libraries are not (yet) supported;
5. Cabal's common stanzas are not (yet) supported;
6. The Haskell tools are built in a IntelliJ sandbox with LTS-13. So they have no dependency with Stackage resolvers in your projects. After Stackage LTS-13 minor updates one can use `Tools`>`Update Haskell tools`;
7. Stack REPLs are running in the background. You can restart them by `Tools`>`Update Settings and Restart REPLs`.

# How to build project
1. Clone this project;
1. Go to root of project and start sbt;
1. Run task `updateIdea` from the sbt console;
1. Run task `compile` from the sbt console;
1. Install/enable the following plugins in IntelliJ: Plugin Devkit, Grammar-Kit and PsiViewer;
1. Import this project as an sbt project in IntelliJ;
1. Be sure `JVM SDK` inside `Languages & Frameworks`>`Scala Compiler Server` is set to `1.8`, since the Scala compiler version (2.12.3) which this plugin is currently using is not compatible with Java 7 or lower, Java 9 is not yet supported;
1. Select `Build`>`Build Project`;


# How to prepare plugin for deployment
1. Right click on top of `intellij-haskell.iml` inside `intellij-haskell` folder;
1. Select `Import module`;
1. Be sure `unmanaged-jars` dependency is set to `provided` inside `Project structure`>`Project settings`>`Modules`>`Dependencies` (btw, setting `provided` inside sbt file gives error); 
1. Right click on top of `intellij-haskell` plugin module and select `Prepare Plugin Module 'intellij-haskell' for deployment`; 


# How to run/debug plugin inside IntelliJ
1. Set Plugin SDK settings right inside `Project structure`>`Platform settings`>`SDKs`. For example to, set  SDK home path to `idea/142.5239.7` inside project root folder;
1. Set `Module-SDK` right for `intellij-haskell` plugin module inside `Project structure`>`Project structure`>`Project settings`>`Modules`; 
1. To run plugin inside IntelliJ, first run configuration has to be created. Navigate to `Run`>`Edit configurations` and create `plugin` configuration for `intellij-haskell`;


# Development remarks
1. After making changes to `_HaskellLexer.flex`, run `Run Flex Generator`. This will generate `_HaskellLexer.java`;
1. After making changes to `haskell.bnf`, run `Generate Parser Code`. This will generate parser Java files in `gen` directory;
1. Add `sources.zip` inside `idea`>[`idea build #`] to `Project structure`>`Project settings`>`Modules`>`Dependencies`>`unmanaged-jars` to see IntelliJ sources;
