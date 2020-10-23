lazy val akkaVersion = "2.6.10"
lazy val akkaHttpVersion = "10.2.1"
lazy val akkaHttpCirceVersion = "1.35.0"
lazy val akkaHttpSessionVersion = "0.5.11"
lazy val circeVersion = "0.12.3"
lazy val catsVersion = "2.1.1"
lazy val googleApiClientVersion = "1.25.0"
lazy val logbackVersion = "1.2.3"
lazy val redisScalaVersion = "1.9.0"
lazy val scalaTestVersion = "3.2.2"

lazy val root = (project in file(".")).settings(
  inThisBuild(
    List(
      organization := "dev.danielbytes",
      scalaVersion := "2.13.3"
    )
  ),
  name := "rps-scala",
  libraryDependencies ++= Seq(
    "com.typesafe.akka" %% "akka-stream-typed" % akkaVersion,
    "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
    "com.typesafe.akka" %% "akka-http-xml" % akkaHttpVersion,
    "com.typesafe" % "config" % "1.3.2",
    "io.circe" %% "circe-core" % circeVersion,
    "io.circe" %% "circe-generic" % circeVersion,
    "io.circe" %% "circe-parser" % circeVersion,
    "de.heikoseeberger" %% "akka-http-circe" % akkaHttpCirceVersion,
    "com.softwaremill.akka-http-session" %% "core" % akkaHttpSessionVersion,
    "org.typelevel" %% "cats-core" % catsVersion,
    "com.google.api-client" % "google-api-client" % googleApiClientVersion,
    "ch.qos.logback" % "logback-classic" % logbackVersion,
    "com.github.etaty" %% "rediscala" % redisScalaVersion,
    "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % Test,
    "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
    "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion % Test,
    "org.scalatest" %% "scalatest" % scalaTestVersion % Test,
    "org.scalatest" %% "scalatest-flatspec" % scalaTestVersion % Test
  )
)

enablePlugins(JavaAppPackaging)
