addSbtPlugin("org.jetbrains" % "sbt-idea-plugin" % "3.4.1")

resolvers += Resolver.url("jetbrains-bintray",
  url("https://dl.bintray.com/jetbrains/sbt-plugins/"))(Resolver.ivyStylePatterns)
