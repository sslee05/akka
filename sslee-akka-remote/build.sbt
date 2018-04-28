name := "sslee-akka-remote"

version := "1.0"

scalaVersion := "2.12.4"

lazy val akkaVersion = "2.5.3"
lazy val akkaHttpVersion = "10.1.0"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
  "org.scalatest" %% "scalatest" % "3.0.1" % "test",
  "ch.qos.logback" % "logback-classic" % "1.2.3",

  //akka remote
  "com.typesafe.akka" %% "akka-remote" % akkaVersion,
  //akka remote testkit
  "com.typesafe.akka" %% "akka-multi-node-testkit" % akkaVersion % "test",
  
  //akka http
  "com.typesafe.akka" %% "akka-http-core" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion
)

lazy val root = (project in file("."))
  .enablePlugins(MultiJvmPlugin)
  .configs(MultiJvm)
  
enablePlugins(JavaAppPackaging)

//if you need a server with autostart support
//enablePlugins(JavaServerAppPackaging)

fork in run := true
