# IntelliJ plugin for Haskell

When I was learning Haskell, I missed the nice features of IntelliJ IDEA. My first approach
was to use default way of creating an IntelliJ plugin by defining a grammar and a lexer according to
[Haskell report](http://www.haskell.org/onlinereport/haskell2010/haskellch10.html). That didn't work out because I could not define all 
the recursion. 
Then I decided to use grammar and lexer definitions only for tokenizing and parsing Haskell code, and not for syntax checking the code. This is needed for syntax highlighting, all kinds of navigation and so on.
Further Haskell language support is provided with the support of external tools: ghc-mod and haskell-docs.

This plugin is written mainly in Scala and is not meant to support GHC/Cabal/Stack directly. It supports Cabal sandbox and Stack projects
and expects that builds and such are done on the command line.

Any feedback is welcome!!


# Features
- Syntax highlighting (which can be customized);
- Error/warning highlighting;
- Find usages of identifiers;
- Resolve references to identifiers (also to library code if library source code is added to project and resolves inside import declaration);
- Code completion by resolving references;
- In-place rename identifiers (in which global rename first shows preview so refactoring scope can be adjusted).
- View type info from (selected) expression;
- View expression info;
- View quick documentation;
- View quick definition;
- Structure view;
- Navigate to declaration (called `Class` in IntelliJ menu);
- Navigate to identifier (called `Symbol` in IntelliJ menu);
- Code completion by looking to import declarations;
- Simple form of code formatting;
- Inspection by HLint;
- Quick fixes for HLint suggestions;
- View error, action to view formatted message from ghc-mod. Especially useful in case message consists of multiple lines (Ctrl-F10, Meta-F10 on Mac OSX);
- Intention actions (suggested by ghc-mod);
            
A lot of features are implemented with help of ghc-mod!!


# Getting started: 
- Install latest versions of ghc-mod and haskell-docs;
- Set file paths to `ghc-mod`, `hlint`, `cabal` and `haskell-docs` in the menu `Settings`/`Other Settings`/`Haskell`.
- Be sure in `Editor/Filetypes` that `Haskell language file` is registered with pattern `*.hs` and `Literate Haskell language file` with pattern `*.lhs`; 
- First install and build your project. Preferably with Stack. (Make sure that Haddock documentation is generated, see [haskell-docs](https://github.com/chrisdone/haskell-docs)).
- After the project is built, create a Haskell project in IntelliJ by using `File`/`New`/`Project...` from the IntelliJ menu;
- In the `New Project` wizard select `Haskell module` and check `Haskell` in `Additional Libraries and Frameworks`;
- In next page of wizard create `GHC SDK` by selecting path to GHC binaries, e.g. `/usr/local/bin`;
- In last page of wizard select path to root of project;
- Add `Content Root` to Haskell module in `Project structure`/`Project Settings`/`Modules`/`Sources` by selecting root folder of Haskell project;
- Select in `Project structure`/`Project settings`/`Modules` which folders to exclude (like `.cabal-sandbox` and `dist`) and which folders are `Source` and `Test` (normally `src` and `test`).
- Use `Tools`/`Add Haskell package dependencies` to download all sources of dependencies. They will be added as source libraries to module. If you use Cabal 1.22 also test dependencies will be downloaded. 
    This option gives you nice navigation features through libraries. Sources are downloaded to folder `ideaHaskellLib` inside root of project 


# Remarks
- In Navigation dialog dots can not be used. Workaround is by using spaces instead of dots, so when you want to go to `Control.Lens`, type `Control Lens`;
- IntelliJ has a nice terminal plugin, useful for executing the Cabal/Stack commands;
- Developed plugin on Ubuntu;
- Windows is not supported. Maybe it will work okay but I can not test it;
- Created workaround for ghc-mod issue #432;


# How to build project
1. Clone this project;
1. Go to root of project and start sbt;
1. Run task `updateIdea` from the sbt console;
1. Run task `compile` from the sbt console;
1. Install/enable the following plugins in IntelliJ: Plugin Devkit, Grammar-Kit and PsiViewer;
1. Import this project as an sbt project in IntelliJ;
1. Select `Build`/`Make project`;


# How to prepare plugin for deployment
1. Right click on top of `intellij-haskell.iml` inside `intellij-haskell` folder;
1. Select `Import module`;
1. Be sure `unmanaged-jars` dependency is set to `provided` inside `Project structure`/`Project settings`/`Modules`/`Dependencies` (btw, setting `provided` inside sbt file gives error); 
1. Right click on top of `intellij-haskell` plugin module and select `Prepare Plugin Module 'intellij-haskell' for deployment`; 


# How to run/debug plugin inside IntelliJ
1. Set Plugin SDK settings right inside `Project structure`/`Platform settings`/`SDKs`. For example to, set  SDK home path to `idea/142.5239.7` inside project root folder;
1. Set `Module-SDK` right for `intellij-haskell` plugin module inside `Project structure`/`Project structure`/`Project settings`/`Modules`; 
1. To run plugin inside IntelliJ, first run configuration has to be created. Navigate to `Run`/`Edit configurations` and create `plugin` configuration for `intellij-haskell`;


# Development remarks
1. After making changes to `_HaskellLexer.flex`, run `Run Flex Generator`. This will generate `_HaskellLexer.java`;
1. After making changes to `haskell.bnf`, run `Generate Parser Code`. This will generate parser Java files in `gen` directory;
1. Add `sources.zip` inside `idea`/[`idea build #`] to `Project structure`/`Project settings`/`Modules`/`Dependencies`/`unmanaged-jars` to see IntelliJ sources;
