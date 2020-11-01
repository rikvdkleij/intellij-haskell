# ![logo](logo/icon_intellij_haskell_32.png) IntelliJ plugin for Haskell

# [![Join the chat at https://gitter.im/intellij-haskell/Lobby](https://badges.gitter.im/intellij-haskell/Lobby.svg)](https://gitter.im/intellij-haskell/Lobby?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge) [![paypal](https://www.paypalobjects.com/en_US/i/btn/btn_donateCC_LG.gif)](https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=H6QKQKFRKJBF2&item_name=IntelliJ-Haskell&currency_code=EUR) [![Codeship Status for rikvdkleij/intellij-haskell](https://app.codeship.com/projects/171b1960-3d31-0138-a733-12543791c0bd/status?branch=master)](https://app.codeship.com/projects/387346)

When I was learning Haskell, I missed the nice features of IntelliJ IDEA. My first approach
was to use the default way of creating an IntelliJ plugin by defining a grammar and a lexer according to
[Haskell report](http://www.haskell.org/onlinereport/haskell2010/haskellch10.html). That didn't work out because I could not define all the recursion. 
Then I decided to use grammar and lexer definitions only for tokenizing and parsing Haskell code, and not for syntax checking the code. This is needed for syntax highlighting, all kinds of navigation, and so on.
Further Haskell language support is provided with the help of external tools.

This plugin depends mainly on Stack. It can create new Stack projects and import existing Stack projects.

GHC 8.2.2 and later is supported.

Any feedback is welcomed!!

# GitHub Sponsors
![King Lounge](logo/king.png)  
### King lounge
* @Nexmean
* @kor85

___
![Duke Lounge](logo/duke.png) 
### Duke lounge
* @seanjensengrey
* @paweln1986
* @0xd34df00d
* @ditansu

___

### Loyal supporter
* @gnoireaux
___

# Sponsors
* @develop7
* @liff
* @stefanscheidt
* @aryairani
* @lachezar
* Value Object Comm.V
* Simon Looschen
* Титоренко Алексей
* @declension
* Uri Brecher
* Ушаков Сергей
* Sjur Millidahl
* Bogard Guillaume Romain
* @enolive
* Amr Sabry
* Alex Wied

# Installing the plugin
You can install this plugin using the [Jetbrains plugin repository](https://plugins.jetbrains.com/idea/plugin/8258-intellij-haskell):
  `Settings`/`Plugins`/`Browse repositories`/IntelliJ-Haskell`

# Installing the latest beta of the plugin
To try out the latest beta version one can install the plugin by adding `https://plugins.jetbrains.com/plugins/alpha/8258` as a custom plugin repository in `Settings`/`Plugins`/`Browse repositories`/`Manage repositories`.
 
An alternative way to install the latest beta version is to download `IntelliJ-haskell.zip` from [releases](https://github.com/rikvdkleij/intellij-haskell/releases) and apply `Install plugin from disk` in `Settings`/`Plugins`.


# Features
- Syntax highlighting;
- Error/warning highlighting;
- Haskell Problems View. This tool window displays GHC messages for the currently edited files;
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
- Navigate to an identifier (called `Navigate`>` Symbol` in IntelliJ menu);
- Goto instance declaration (called `Navigate`>`Instance Declaration` in IntelliJ menu);
- Navigate to declaration or identifier powered by Hoogle (called `Navigate`>`Navigation by Hoogle` in IntelliJ menu);
- Inspection by HLint;
- Quick fixes for HLint suggestions;
- Show error action to view formatted messages. Useful in case message consists of multiple lines (Ctrl-F10, Meta-F10 on Mac OSX);
- Intention actions to add language extension (depends on compiler error), add top-level type signature (depends on compiler warning);
- Intention action to select which module to import if the identifier is not in scope;
- Default code formatting by `ormolu`. Alternatively by `stylish-Haskell`.
- Code completion for project module names, language extensions, and package names in Cabal file;
- Running REPL, tests, and executables via `Run Configurations`;
- Smart code completion on typed holes (since GHC 8.4);



# Getting started
- If you don't already have IntelliJ, [download it](https://www.jetbrains.com/idea/download/) - the Community Edition is sufficient.
- Install this plugin using the [Jetbrains plugin repository](https://plugins.jetbrains.com/idea/plugin/8258-intellij-haskell): `Settings`/`Plugins`/`Browse repositories`/IntelliJ-Haskell`. Make sure no other Haskell plugin is installed in IntelliJ;
- Install the latest version of [Stack](https://github.com/commercialhaskell/stack); use `stack upgrade` to confirm you are on the latest version.
- Setup the project:
  - Make sure your Stack project builds without errors. Preferably by using: `stack build --test --haddock --no-haddock-hyperlink-source`;
  - After your project is built successfully, import an existing project by:
    - Inside IntelliJ use `File`>`New`>`Project from Existing Sources...` from the IntelliJ menu;
    - In the `Welcome to IntelliJ IDEA` dialog use `Open or Import Project`; 
  - In the `New Project` wizard select `Import project from external model` and check `Haskell Stack`;
  - On the next page of wizard configure `Project SDK` by configuring `Haskell Tool Stack` by selecting a path to `stack` binary, e.g. `/usr/local/bin/stack` (you can use `which stack` on Linux or macOS or `where stack` on windows to find the path);
  - Finish wizard and project will be opened;
  - Wizard will automatically configure which folders are sources, test and which to exclude;
  - Plugin will automatically build Haskell Tools (HLint, Hoogle, Ormolu, and Stylish Haskell) to prevent incompatibility issues
  - Check `Project structure`>`Project settings`>`Modules` which folders to exclude (like `.stack-work` and `dist`) and which folders are `Source` and `Test` (normally `src` and `test`);
  - Plugin will automatically download library sources. They will be added as source libraries to module(s).
  - After changing the Cabal file and/or `stack.yaml` use `Haskell`>`Haskell`>`Update Settings and Restart REPLs` to download missing library sources and update the project settings;
  - The `Event Log` will display what's going on in the background. Useful when something fails. It's disabled by default. 
    It can be enabled by checking the `Haskell Log` checkbox in the `Event Log`>`Settings` or `Settings`>`Appearance & Behavior`>`Notifications`;    


# Remarks
1. IntelliJ's Build action is not (yet) implemented. Project is built when the project is opened and when needed, e.g. when library code is changed and the user navigates to test code;
2. `About Haskell Project` in `Help` menu shows which Haskell GHC/tools are used by the plugin for the project;
3. GHC depends on `libtinfo-dev`. On Ubuntu you can install it with `sudo apt-get install libtinfo-dev`;
4. Haskell tools depend on `libgmp3-dev zlib1g-dev`. On Ubuntu you can install them with `sudo apt-get install libgmp3-dev zlib1g-dev`;
5. Cabal's internal libraries are not (yet) supported;
6. Cabal's common stanzas are not (yet) supported;
7. The Haskell tools are built in an IntelliJ sandbox with LTS-16. So they have no dependency on Stackage resolvers in your projects. After Stackage LTS-13 minor updates one can use `Haskell`>`Update Haskell tools`;
8. Stack REPLs are running in the background. You can restart them by `Haskell`>`Update Settings and Restart REPLs`.

If you want to contribute to this project, read [the contributing guideline](CONTRIBUTING.md).
