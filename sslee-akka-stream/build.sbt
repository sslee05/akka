name := "sslee-akka-stream"

version := "1.0"

scalaVersion := "2.12.4"

lazy val akkaVersion = "2.5.12"
lazy val akkaHttpVersion = "10.1.1"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  "ch.qos.logback" % "logback-classic" % "1.2.3", 

  //akka-http
  "com.typesafe.akka" %% "akka-http-core" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-xml" % akkaHttpVersion,
  //"com.typesafe.akka" %s "akka-http-experimental" % "2.4.11",
  //"com.typesafe.akka" %% "akka-http-spray-json-experimental" % "2.4.11",
  "com.typesafe.akka" %% "akka-parsing" % akkaHttpVersion,
  
  //test
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % "test",
  "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion % "test",
  "org.scalatest" %% "scalatest" % "3.0.1" % "test"
  
)
