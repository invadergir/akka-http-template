name := "akka-http-template"

version := "0.1"

organization := "com.example"

scalaVersion := "2.13.8"

lazy val akkaVer = "2.6.19"
lazy val akkaHttpVer = "10.2.9"
lazy val scalaTestVer = "3.2.12"
lazy val circeVer = "0.14.1"

// Always fork the jvm (test and run)
fork := true

// Allow CTRL-C to cancel running tasks without exiting SBT CLI.
Global / cancelable := true

// Needed for akka-http-json:
resolvers += Resolver.bintrayRepo("hseeberger", "maven")

libraryDependencies ++= Seq(
  
  // akka
  "com.typesafe.akka" %% "akka-actor-typed" % akkaVer,
  "com.typesafe.akka" %% "akka-stream" % akkaVer,
  "com.typesafe.akka" %% "akka-http" % akkaHttpVer,

  // For JSON parsing
  "io.circe" %% "circe-core" % circeVer,
  "io.circe" %% "circe-generic" % circeVer,
  "io.circe" %% "circe-parser" % circeVer,

  // This integrates the chosen json lib into akka-http (can be *-json4s, others as well):
  "de.heikoseeberger" %% "akka-http-circe" % "1.39.2",

  // test akka
  "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVer % Test,
  "com.typesafe.akka" %% "akka-testkit" % akkaVer % Test,
  "com.typesafe.akka" %% "akka-stream-testkit" % akkaVer % Test,

  // config
  "com.github.pureconfig" %% "pureconfig" % "0.17.1",

  // logging
  "ch.qos.logback" % "logback-classic" % "1.2.10",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.4",

  // testing
  "org.scalactic" %% "scalactic" % scalaTestVer % Test,
  "org.scalatest" %% "scalatest" % scalaTestVer % Test,
)

// Print full stack traces in tests:
Test / testOptions += Tests.Argument("-oF")

// Assembly stuff (for fat jar)
assembly / mainClass := Some("com.example.akkahttptemplate.Main")
assembly / assemblyJarName := "akka-http-template.jar"

// Some stuff to import in tho console
console / initialCommands := """

  // project stuff
  import com.example.akkahttptemplate._
"""

