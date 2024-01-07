val scala3Version = "3.3.0"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .settings(
    name := "assignment02",
    version := "0.0.1",
    scalaVersion := scala3Version,
    libraryDependencies ++= Seq(
      guice
    )
  )
