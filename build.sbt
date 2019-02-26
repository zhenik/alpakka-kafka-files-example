resolvers += Resolver.typesafeRepo("releases")
resolvers += "confluent" at "https://packages.confluent.io/maven/"

// Project setup
val rootPackage = "ru.zhenik.kafka.alpakka.consumer"
val projectV = "0.0.1-SNAPSHOT"
val scalaV = "2.12.7"

// https://www.scala-sbt.org/release/docs/Basic-Def-Examples.html
lazy val settings = Seq(
  version := projectV,
  scalaVersion := scalaV,

//  // set the main Scala source directory to be <base>/src
//  scalaSource in Compile := baseDirectory.value / "src/main/scala",
//
//  // set the Scala test source directory to be <base>/test
//  scalaSource in Test := baseDirectory.value / "src/test/scala",
//
//  // append several options to the list of options passed to the Java compiler
//  javacOptions ++= Seq("-source", "1.8", "-target", "1.8"),
//
//  // only use a single thread for building
//  parallelExecution := false,

)

lazy val rootProject = project
  .in(file("."))
  .settings(
    name := "kafka-alpakka-consumer",
    organization := rootPackage,
    version := projectV,
    settings,
    libraryDependencies ++= Seq (
      "com.typesafe.akka"   %% "akka-stream-testkit"              % "2.5.21"        % Test,
      "com.typesafe.akka"   %% "akka-stream"                      % "2.5.21",
      "com.typesafe.akka"   %% "akka-stream-kafka"                % "1.0-RC1",
      "com.lightbend.akka"  %% "akka-stream-alpakka-file"         % "1.0-M2",
      "ch.qos.logback"      % "logback-classic"                   % "1.2.3",
      "com.typesafe.akka"   % "akka-slf4j_2.12"                   % "2.5.21"
    ) 
  )