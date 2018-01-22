lazy val commonSettings = Seq(
  version := "1.0.0-beta12",
  scalaVersion := "2.12.4"
)

val guava = "com.google.guava" % "guava" % "23.6-jre"
val scalaTest = "org.scalatest" %% "scalatest" % "3.0.4" % Test
val sprayJson = "io.spray" %% "spray-json" % "1.3.4"
val snakeYaml = "org.yaml" % "snakeyaml" % "1.19"

lazy val intellijHaskell = (project in file(".")).
  enablePlugins(SbtIdeaPlugin).
  settings(commonSettings: _*).
  settings(
    name := "IntelliJ Haskell",
    javacOptions in Global ++= Seq("-source", "1.8", "-target", "1.8"),
    scalacOptions in Global ++= Seq("-target:jvm-1.8", "-deprecation"),
    libraryDependencies += guava,
    libraryDependencies += scalaTest,
    libraryDependencies += sprayJson,
    libraryDependencies += snakeYaml,
    unmanagedSourceDirectories in Compile += baseDirectory.value / "gen",
    unmanagedJars in Compile += baseDirectory.value / "idea"
  )

ideaBuild in ThisBuild := "173.4548.2"