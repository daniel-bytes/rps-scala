lazy val akkaHttpVersion = "10.2.1"
lazy val akkaVersion = "2.5.23"
lazy val circeVersion = "0.12.3"
lazy val scalaTestVersion = "3.2.2"

scalacOptions += "-Ypartial-unification"

lazy val root = (project in file(".")).settings(
  inThisBuild(
    List(
      organization := "com.danielbytes",
      scalaVersion := "2.13.3"
    )
  ),
  name := "rps-scala",
  libraryDependencies ++= Seq(
    "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
    "com.typesafe.akka" %% "akka-http-xml" % akkaHttpVersion,
    "com.typesafe.akka" %% "akka-stream" % akkaVersion,
    "com.typesafe" % "config" % "1.3.2",
    "io.circe" %% "circe-core" % circeVersion,
    "io.circe" %% "circe-generic" % circeVersion,
    "io.circe" %% "circe-parser" % circeVersion,
    "de.heikoseeberger" %% "akka-http-circe" % "1.35.0",
    "org.typelevel" %% "cats-core" % "1.5.0",
    "com.google.api-client" % "google-api-client" % "1.25.0",
    "com.softwaremill.akka-http-session" %% "core" % "0.5.11",
    "com.github.etaty" %% "rediscala" % "1.9.0",
    "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % Test,
    "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
    "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion % Test,
    "org.scalatest" %% "scalatest" % scalaTestVersion % Test
  )
)

enablePlugins(JavaAppPackaging)
