lazy val commonSettings = Seq(
  version := "1.0.0-beta83",
  scalaVersion := "2.13.3"
)

val scalaTest = "org.scalatest" %% "scalatest" % "3.2.0" % Test
val sprayJson = "io.spray" %% "spray-json" % "1.3.5"
val snakeYaml = "org.yaml" % "snakeyaml" % "1.26"
val scaffeine = "com.github.blemale" %% "scaffeine" % "4.0.1"
val directories = "io.github.soc" % "directories" % "12"

intellijPluginName in ThisBuild := "IntelliJ-Haskell"

lazy val intellijHaskell = (project in file(".")).
  enablePlugins(SbtIdeaPlugin).
  settings(commonSettings: _*).
  settings(
    name := "IntelliJ Haskell",
    javacOptions in Global ++= Seq("-source", "1.8", "-target", "1.8"),
    scalacOptions in Global ++= Seq("-target:jvm-1.8", "-deprecation", "-feature"),
    libraryDependencies += scalaTest,
    libraryDependencies += sprayJson,
    libraryDependencies += snakeYaml,
    libraryDependencies += scaffeine,
    libraryDependencies += directories,
    unmanagedSourceDirectories in Compile += baseDirectory.value / "gen"
  )

intellijBuild in ThisBuild := "202.7319.50"

intellijPlugins += "com.intellij.java".toPlugin
