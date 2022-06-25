lazy val commonSettings = Seq(
  version := "1.0.0-beta88",
  scalaVersion := "2.13.8"
)

val scalaTest = "org.scalatest" %% "scalatest" % "3.2.12" % Test
val sprayJson = "io.spray" %% "spray-json" % "1.3.6"
val snakeYaml = "org.yaml" % "snakeyaml" % "1.30"
val scaffeine = "com.github.blemale" %% "scaffeine" % "5.1.2"
val directories = "io.github.soc" % "directories" % "12"
val fastparse = "com.lihaoyi" %% "fastparse" % "2.3.3"

(ThisBuild / intellijPluginName) := "IntelliJ-Haskell"

lazy val intellijHaskell = (project in file(".")).
  enablePlugins(SbtIdeaPlugin).
  settings(commonSettings: _*).
  settings(
    name := "IntelliJ Haskell",
    javacOptions in Global ++= Seq("-source", "1.8", "-target", "1.8"),
    scalacOptions in Global ++= Seq("-target:jvm-1.8", "-deprecation", "-feature", "-unchecked"),
    libraryDependencies += scalaTest,
    libraryDependencies += sprayJson,
    libraryDependencies += snakeYaml,
    libraryDependencies += scaffeine,
    libraryDependencies += directories,
    libraryDependencies += fastparse,
    (Compile / unmanagedSourceDirectories) += baseDirectory.value / "gen"
  )

(ThisBuild / intellijBuild) := "212.4746.92"

intellijPlugins += "com.intellij.java".toPlugin
