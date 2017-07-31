organization := "com.consideredgames"
name := "vv-lib"

version := "0.1-SNAPSHOT"
isSnapshot := true

scalaVersion := "2.11.11"

scalacOptions += "-feature"
scalacOptions += "-Ylog-classpath"

resolvers += "Artima Maven Repository" at "http://repo.artima.com/releases"
resolvers += Resolver.bintrayRepo("commercetools", "maven")
resolvers += Resolver.bintrayRepo("hseeberger", "maven")
resolvers += "softprops-maven" at "http://dl.bintray.com/content/softprops/maven"

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.1.7"
libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0"

libraryDependencies += "me.lessis" %% "base64" % "0.2.0"

libraryDependencies += "com.typesafe.akka" %% "akka-http" % "10.0.5"
libraryDependencies += "de.heikoseeberger" %% "akka-http-json4s" % "1.14.0"
libraryDependencies += "org.json4s" %% "json4s-native" % "3.5.1"
libraryDependencies += "org.codehaus.jackson" % "jackson-mapper-asl" % "1.9.13"

libraryDependencies += "org.scalaz" %% "scalaz-core" % "7.3.0-M11"

libraryDependencies += "org.scalactic" %% "scalactic" % "3.0.1"
libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.0.1" % "test",
  "org.mockito" % "mockito-core" % "2.6.9" % "test",
  "org.scalamock" %% "scalamock-scalatest-support" % "3.5.0" % "test"
)

libraryDependencies += "org.scala-lang" % "scala-reflect" % "2.11.11"
libraryDependencies += "org.scala-lang.modules" %% "scala-xml" % "1.0.6"
