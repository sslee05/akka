name := "sslee-akka-router"

version := "1.0"

scalaVersion := "2.12.4"

lazy val akkaVersion = "2.5.12"
lazy val akkaHttpVersion = "10.1.1"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
  "org.scalatest" %% "scalatest" % "3.0.1" % "test",
  "ch.qos.logback" % "logback-classic" % "1.2.3",

  //akka remote
  "com.typesafe.akka" %% "akka-remote" % akkaVersion,
  "com.typesafe.akka" %% "akka-multi-node-testkit" % akkaVersion % "test",

  //akka http
  "com.typesafe.akka" %% "akka-http-core" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion

  
)

lazy val root = (project in file("."))
	.enablePlugins(MultiJvmPlugin)
	.configs(MultiJvm)

enablePlugins(JavaAppPackaging)

fork in run := true
