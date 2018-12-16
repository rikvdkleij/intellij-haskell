lazy val commonSettings = Seq(
  version := "1.0.0-beta37",
  scalaVersion := "2.12.7"
)

val scalaTest = "org.scalatest" %% "scalatest" % "3.0.4" % Test
val sprayJson = "io.spray" %% "spray-json" % "1.3.4"
val snakeYaml = "org.yaml" % "snakeyaml" % "1.19"
val scaffeine = "com.github.blemale" %% "scaffeine" % "2.5.0"
val directories = "io.github.soc" % "directories" % "10"

lazy val intellijHaskell = (project in file(".")).
  enablePlugins(SbtIdeaPlugin).
  settings(commonSettings: _*).
  settings(
    name := "IntelliJ Haskell",
    javacOptions in Global ++= Seq("-source", "1.8", "-target", "1.8"),
    scalacOptions in Global ++= Seq("-target:jvm-1.8", "-deprecation"),
    libraryDependencies += scalaTest,
    libraryDependencies += sprayJson,
    libraryDependencies += snakeYaml,
    libraryDependencies += scaffeine,
    libraryDependencies += directories,
    unmanagedSourceDirectories in Compile += baseDirectory.value / "gen"
  )


ideaBuild in ThisBuild := "183.4588.61"
