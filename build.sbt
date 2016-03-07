lazy val commonSettings = Seq(
  organization := "com.powertuple",
  version := "0.95",
  scalaVersion := "2.11.7"
)

val scalaTest = "org.scalatest" % "scalatest_2.11" % "2.2.4" % "test"
val sprayJson = "io.spray" %% "spray-json" % "1.3.2"

lazy val intellijHaskell = (project in file(".")).
  enablePlugins(SbtIdeaPlugin).
  settings(commonSettings: _*).
  settings(
    name := "IntelliJ Haskell",
    javacOptions in Global ++= Seq("-source", "1.6", "-target", "1.6"),
    scalacOptions in Global += "-target:jvm-1.6",
    libraryDependencies += scalaTest,
    libraryDependencies += sprayJson,
    unmanagedSourceDirectories in Compile += baseDirectory.value / "gen",
    unmanagedJars in Compile += baseDirectory.value / "idea"
  )

ideaBuild in ThisBuild := "143.1184.17"
