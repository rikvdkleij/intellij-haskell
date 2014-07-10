# IntelliJ plugin for Haskell

First, this plugin is work-in-progress!

Some months ago I started his project because I was learning Haskell and I was missing the nice features of IntelliJ IDEA.

First approach was to use default way of creating IntelliJ plugin by defining grammar and lexer according to [Haskell report]
(http://www.haskell.org/onlinereport/haskell2010/haskellch10.html)
That did not workout because I could not define all the recursiveness.

Decided to use basic grammar and lexer definitions just for tokenizing Haskell code (e.g. for syntax highlighting). 
Other Haskell support by help from external tools as ghc-mod(i).

In the meantime also Atsky started to create [Haskell-idea-plugin](https://github.com/Atsky/haskell-idea-plugin) based on ideah plugin in Kotlin. First I saw clear difference
 in approach (besides language) but it looks like we are eventually using to same approach to support Haskell code in IntelliJ. 
 
This plugin is written in Java/Scala and is mentioned not to support GHC/Cabal directly. My idea is to support sandbox projects
and doing the initial/basic Haskell configuration in terminal. This plugin will rely on external tools (mainly ghc-mod(i)) for Haskell language support in IntelliJ IDEA.
The user interface component in this project for setting the paths to external tools is based on Haskell-idea-plugin. I have no experience in creating IntelliJ
plugins so it's 'inspiring' to look to code of other plugins. Especially thanks to developers of the Erlang plugin :-)
