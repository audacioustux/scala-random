val scala3Version = "3.3.0"
val AkkaVersion = "2.8.5"
val LogbackVersion = "1.4.11"

lazy val root = project
  .in(file("."))
  .settings(
    name := "assignment3",
    version := "0.1.0-SNAPSHOT",
    scalaVersion := scala3Version,
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
      "ch.qos.logback" % "logback-classic" % LogbackVersion
    )
  )
