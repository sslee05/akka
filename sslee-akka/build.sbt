//name := "akka-quickstart-scala"
name := "sslee-akka"

version := "1.0"

scalaVersion := "2.12.4"

lazy val akkaVersion = "2.5.3"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-parsing" % "10.1.0",
  "com.typesafe.akka" %% "akka-http" % "10.1.0",
  "com.typesafe.akka" %% "akka-http-core" % "10.1.0",
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.0",
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
  "com.typesafe.akka" %% "akka-slf4j"      % akkaVersion,
  "ch.qos.logback"    %  "logback-classic" % "1.2.3",
  "org.scalatest" %% "scalatest" % "3.0.1" % "test"
)

//herokuAppName in Compile := "floating-hamlet-24542"

enablePlugins(JavaAppPackaging)
