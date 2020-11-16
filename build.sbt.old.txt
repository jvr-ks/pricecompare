name := "Pricecompare"

version := "0.102-SNAPSHOT"

scalaVersion := "2.13.1"

mainClass in Compile := Some("de.jvr.pricecompare.Pricecompare")

resolvers += "Maven" at "https://repo1.maven.org/maven2/"

resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

resolvers += "Typesafe Releases" at "https://repo.typesafe.com/typesafe/releases/"

// https://mvnrepository.com/artifact/org.scala-lang.modules/scala-xml
libraryDependencies += "org.scala-lang.modules" %% "scala-xml" % "latest.integration"

// https://mvnrepository.com/artifact/org.scalafx/scalafx
libraryDependencies += "org.scalafx" %% "scalafx" % "latest.integration"

// https://mvnrepository.com/artifact/com.typesafe.akka/akka-actor

libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "latest.integration"

// https://mvnrepository.com/artifact/com.typesafe.akka/akka-actor-typed_2.13/2.6.6 new actor API
// libraryDependencies += "com.typesafe.akka" %% "akka-actor-typed" % "latest.integration"

// https://mvnrepository.com/artifact/com.typesafe.akka/akka-testkit
libraryDependencies += "com.typesafe.akka" %% "akka-testkit" % "latest.integration" % "test"

// https://github.com/Log4s/log4s
libraryDependencies += "org.log4s" %% "log4s" % "latest.integration"

// https://mvnrepository.com/artifact/ch.qos.logback/logback-classic
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3"


// https://mvnrepository.com/artifact/org.jsoup/jsoup
libraryDependencies += "org.jsoup" % "jsoup" % "latest.integration"

// https://mvnrepository.com/artifact/org.slf4j/slf4j-log4j12
//libraryDependencies += "org.slf4j" % "slf4j-log4j12" % "1.7.25"


// http://ammonite.io/#Ammonite-REPL
// https://github.com/lihaoyi/ammonite
// libraryDependencies += "com.lihaoyi" % "ammonite" % "latest.integration" cross CrossVersion.full

// https://github.com/lihaoyi/utest
libraryDependencies += "com.lihaoyi" %% "utest" % "latest.integration" % "test"

// http://ammonite.io/#Ammonite-Ops
libraryDependencies += "com.lihaoyi" %% "ammonite-ops" % "latest.integration"

// https://github.com/lightbend/config
libraryDependencies += "com.typesafe" % "config" % "latest.integration"

// https://mvnrepository.com/artifact/org.openjfx
lazy val javaFXModules = Seq("base", "controls", "fxml", "graphics", "media", "swing", "web")
libraryDependencies ++= javaFXModules.map( m =>
  "org.openjfx" % s"javafx-$m" % "latest.integration"
)

testFrameworks += new TestFramework("utest.runner.Framework")

testFrameworks := {
  testFrameworks.value.filterNot(_.toString.contains("scalatest"))
}


fork := true

scalacOptions ++= Seq("-deprecation", "-feature", "-language:postfixOps")

//javaOptions += "-Dconfig.trace=loads"
javaOptions += "-Dconfig.override_with_env_vars=true"

assemblyMergeStrategy in assembly := {
 case PathList("META-INF", xs @ _*) => MergeStrategy.discard
 case x => MergeStrategy.first
}



