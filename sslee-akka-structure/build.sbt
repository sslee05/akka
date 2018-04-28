name := "sslee-akka-structure"

version := "1.0"

scalaVersion := "2.12.4"

lazy val akkaVersion = "2.5.3"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
  "org.scalatest" %% "scalatest" % "3.0.1" % "test",
  
  //org.joda.time
  "com.github.nscala-time" %% "nscala-time" % "2.18.0"
)

enablePlugins(JavaAppPackaging)
