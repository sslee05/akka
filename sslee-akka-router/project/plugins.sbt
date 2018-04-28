resolvers += Classpaths.typesafeReleases

//addSbtPlugin("io.spray" % "sbt-revolver" % "0.9.1")

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.6")

addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.3.3")

addSbtPlugin("com.typesafe.sbt" % "sbt-multi-jvm" % "0.4.0")

//sbt 1.0 start-script into native-packager
//addSbtPlugin("com.typesafe.sbt" % "sbt-start-script" % "0.10.0")

addSbtPlugin("com.heroku" % "sbt-heroku" % "2.1.0")
