name := "sslee-akka-cluster"

version := "1.0"

scalaVersion := "2.12.6"

lazy val akkaVersion = "2.5.12"
lazy val akkaHttpVersion = "10.1.1"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % "test",
  "org.scalatest" %% "scalatest" % "3.0.5" % "test",

  //akka-remote, akk-cluster
  "com.typesafe.akka" %% "akka-remote" % akkaVersion,
  "com.typesafe.akka" %% "akka-cluster" % akkaVersion,
  "com.typesafe.akka" %% "akka-multi-node-testkit" % akkaVersion,
  
  //akka-http
  "com.typesafe.akka" %% "akka-http-core" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-xml" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-parsing" % akkaHttpVersion
  
)

lazy val root = (project in file("."))
  .enablePlugins(MultiJvmPlugin)
  .configs(MultiJvm)

enablePlugins(JavaAppPackaging)

fork in run := true
