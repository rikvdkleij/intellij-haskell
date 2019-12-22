addSbtPlugin("org.jetbrains" % "sbt-idea-plugin" % "3.3.4")

resolvers += Resolver.url("jetbrains-bintray",
  url("https://dl.bintray.com/jetbrains/sbt-plugins/"))(Resolver.ivyStylePatterns)
