val scala3Version = "3.3.0"
val AkkaVersion = "2.8.5"
val AkkaStreamAlpakkaCsvVersion = "6.0.2"

lazy val root = project
  .in(file("."))
  .settings(
    name := "assignment1",
    version := "0.1.0-SNAPSHOT",
    scalaVersion := scala3Version,
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
      "com.lightbend.akka" %% "akka-stream-alpakka-csv" % AkkaStreamAlpakkaCsvVersion
    )
  )
