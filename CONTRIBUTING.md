
# How to build project
1. Clone this project;
1. Go to root of project;
1. Run task `sbt updateIdea` from the shell;
1. Install/enable the following plugins in IntelliJ: Plugin Devkit, Grammar-Kit and PsiViewer;
1. Import this project as an sbt project in IntelliJ;
1. Be sure `JVM SDK` inside `Languages & Frameworks`>`Scala Compiler Server` is set to `1.8`, since the Scala compiler version (2.13.0) which this plugin is currently using is not compatible with Java 7 or lower, Java 9 is not yet supported;
1. Select `Build`>`Build Project`;


# How to prepare plugin for deployment
1. Right click on top of `intellij-haskell.iml` inside `intellij-haskell` folder;
1. Select `Import module`;
1. Be sure `unmanaged-jars` dependency is set to `provided` inside `Project structure`>`Project settings`>`Modules`>`Dependencies` (btw, setting `provided` inside sbt file gives error); 
1. Right click on top of `intellij-haskell` plugin module and select `Prepare Plugin Module 'intellij-haskell' for deployment`; 


# How to run/debug plugin inside IntelliJ
1. Create Project SDK in `Project structure`>`Project settings`>`Project` by using the `New` button and selecting `Intellij Platform Plugin SDK` and setting the path to the IntelliJ folder;
1. Set Plugin SDK settings right inside `Project structure`>`Platform settings`>`SDKs`. For example to, set  SDK home path to `idea/142.5239.7` inside project root folder;
1. Set `Module-SDK` right for `intellij-haskell` plugin module inside `Project structure`>`Project structure`>`Project settings`>`Modules`; 
1. To run plugin inside IntelliJ, first run configuration has to be created. Navigate to `Run`>`Edit configurations` and create `plugin` configuration for `intellij-haskell`;


# Development remarks
1. After making changes to `_HaskellLexer.flex`, run `Run Flex Generator`. This will generate `_HaskellLexer.java`;
1. After making changes to `haskell.bnf`, run `Generate Parser Code`. This will generate parser Java files in `gen` directory;
1. Never touch the generated code. They should be committed as-is.
1. Add `sources.zip` inside `idea`>[`idea build #`] to `Project structure`>`Project settings`>`Modules`>`Dependencies`>`unmanaged-jars` to see IntelliJ sources;
