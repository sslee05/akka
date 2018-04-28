name := "sslee-akka-conf"

version := "1.0"

scalaVersion := "2.12.4"

//IDE에서 이미 main/resources 를 classpath에 설정 했기 때문에 따로 build.sbt에 다음과 같이 설정 하지 않아도 된다.
//아래의 설정은 만약 IDE에 설정 file들이 main/resources에 classpath로 되어 있지 않을 경우
//conf에  설정 file을 복사해서 stage시나 assembly시에 추가 하라는 것 이다.
//scriptClasspath += "../conf"

lazy val akkaVersion = "2.5.3"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
  "org.scalatest" %% "scalatest" % "3.0.1" % "test",
  "ch.qos.logback" % "logback-classic" % "1.2.3"
)

//독립실행 application이라는 사실을 알린다.
enablePlugins(JavaAppPackaging)
