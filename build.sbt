name := "akka-http-template"

version := "0.1"

organization := "com.example"

scalaVersion := "2.12.6"

lazy val akkaVer = "2.5.12"
lazy val akkaHttpVer = "10.1.1"
lazy val scalaTestVer = "3.0.4"
lazy val json4SVer = "3.6.11"

// Always fork the jvm (test and run)
fork := true

// Allow CTRL-C to cancel running tasks without exiting SBT CLI.
Global / cancelable := true

// Needed for akka-http-json4s:
resolvers += Resolver.bintrayRepo("hseeberger", "maven")

libraryDependencies ++= Seq(
  
  // akka
  "com.typesafe.akka" %% "akka-http"   % akkaHttpVer,
  "com.typesafe.akka" %% "akka-http-spray-json"   % akkaHttpVer,
  //"com.typesafe.akka" %% "akka-http-xml"   % akkaHttpVer,
  "com.typesafe.akka" %% "akka-stream" % akkaVer,

  // For JSON parsing (see https://github.com/json4s/json4s)
  "org.json4s" %%  "json4s-jackson" % json4SVer,
  "org.json4s" %%  "json4s-ext" % json4SVer,  

  // This integrates json4s into akka-http:
  "de.heikoseeberger" %% "akka-http-json4s" % "1.21.0",

  // test akka
  "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVer % Test,
  "com.typesafe.akka" %% "akka-testkit" % akkaVer % Test,
  "com.typesafe.akka" %% "akka-stream-testkit" % akkaVer % Test,

  // config
  "com.github.pureconfig" %% "pureconfig" % "0.9.1",

  // logging
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0",

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

