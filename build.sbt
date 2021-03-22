
ThisBuild / organization := "de.jvr"
ThisBuild / scalaVersion := "2.13.3"
ThisBuild / version      := "0.106-SNAPSHOT"


lazy val javaFXModules = Seq("base", "controls", "fxml", "graphics", "media", "swing", "web")

lazy val root = (project in file("."))
.settings(
	name := "Pricecompare",
	logLevel := Level.Warn,

	mainClass in Compile := Some("de.jvr.pricecompare.Pricecompare"),
	fork := true,
	scalacOptions ++= Seq("-deprecation", "-feature", "-language:postfixOps","-Ywarn-unused","-Yrangepos"),
	javaOptions += "-Dconfig.override_with_env_vars=true",
	addCompilerPlugin(scalafixSemanticdb),
	assemblyMergeStrategy in assembly := {
	 case PathList("META-INF", xs @ _*) => MergeStrategy.discard
	 case x => MergeStrategy.first
	},
	testFrameworks += new TestFramework("utest.runner.Framework"),
	testFrameworks := {
	  testFrameworks.value.filterNot(_.toString.contains("scalatest"))
	},
	
	libraryDependencies ++= Seq(
		"org.scalafx" 								%% "scalafx" 						% "latest.integration",
		"com.typesafe.akka"						%% "akka-actor"         % "latest.integration",
		"com.typesafe.akka"						%% "akka-stream"				% "latest.integration",
		"com.typesafe" 								% "config" 							% "1.4.1",
		"com.typesafe.scala-logging"	%% "scala-logging"			% "latest.integration",
		"org.scala-lang.modules" 			%% "scala-xml" 					% "latest.integration",
		"com.github.pathikrit"				%% "better-files-akka"  % "latest.integration",
		"org.log4s"										%% "log4s" 							% "latest.integration",
		"ch.qos.logback" 							% "logback-classic" 		% "1.2.3",
		"org.slf4j"										% "slf4j-api"						% "1.7.30",
		"com.lihaoyi" 								%% "sourcecode" 				% "0.1.9",
		"com.typesafe.akka" 					%% "akka-testkit" 			% "latest.integration"	% "test",
		"com.lihaoyi"									%% "utest"							% "0.7.7"								% Test
	),
	
	libraryDependencies ++= javaFXModules.map( m => "org.openjfx" % s"javafx-$m" % "latest.integration")
)


