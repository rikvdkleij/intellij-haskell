lazy val commonSettings = Seq(
  version := "1.0.0-beta7",
  scalaVersion := "2.12.1"
)

val guava = "com.google.guava" % "guava" % "20.0"
val scalaTest = "org.scalatest" % "scalatest_2.12" % "3.0.1" % "test"
val sprayJson = "io.spray" %% "spray-json" % "1.3.2"
val snakeYaml = "org.yaml" % "snakeyaml" % "1.17"

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

ideaBuild in ThisBuild := "163.10154.41"