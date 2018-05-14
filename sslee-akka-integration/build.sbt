name := "sslee-akka-integration"

version := "1.0"

scalaVersion := "2.12.4"

lazy val akkaVersion = "2.5.12"
lazy val akkaHttpVersion = "10.1.1"
lazy val camelVersion = "2.21.0"
lazy val activemqVersion = "5.12.3"


libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  "ch.qos.logback" % "logback-classic" % "1.2.3",

  //parser & etc util
  "org.scala-lang.modules" %% "scala-xml" % "1.1.0",
  "net.liftweb" %% "lift-json" % "3.2.0",
  "commons-io" % "commons-io" % "2.6",
  
  //test
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % "test",
  "org.scalatest" %% "scalatest" % "3.0.1" % "test",
  "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % "test",

  //camel
  "com.typesafe.akka" %% "akka-camel"  % akkaVersion,
  "org.apache.camel"  %  "camel-mina2" % camelVersion,
  "org.apache.camel"  %  "camel-jetty" % camelVersion,
  "org.apache.activemq"  %  "activemq-camel" % activemqVersion,
  "org.apache.activemq"  %  "activemq-core"  % "5.7.0",

  //akka-http
  "com.typesafe.akka" %% "akka-http-core" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-xml" % akkaHttpVersion
)
