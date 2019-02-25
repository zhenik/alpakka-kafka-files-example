import scalariform.formatter.preferences._

// Project setup
val rootPackage = "ru.zhenik"
val projectV = "0.0.1-SNAPSHOT"
val scalaV = "2.12.8"

// https://www.scala-sbt.org/release/docs/Basic-Def-Examples.html
lazy val settings = Seq(
  version := projectV,
  scalaVersion := scalaV,

  test in assembly := {},

  // set the main Scala source directory to be <base>/src
  scalaSource in Compile := baseDirectory.value / "src/main/scala",

  // set the Scala test source directory to be <base>/test
  scalaSource in Test := baseDirectory.value / "src/test/scala",

  // append several options to the list of options passed to the Java compiler
  javacOptions ++= Seq("-source", "1.8", "-target", "1.8"),
  
  // only use a single thread for building
  parallelExecution := false,

)

lazy val rootProject = project
  .in(file("."))
  .settings(
    name := "kafka-alpakka-consumer",
    organization := rootPackage,
    version := projectV,
    settings,
    libraryDependencies ++= commonDependencies ++ testDependencies
  )


lazy val commonDependencies = Seq(
  Dependency.akkaStreams
)

lazy val testDependencies = Seq(
  Dependency.scalaTest
)

// code formatter, executed on goal:compile by default
scalariformPreferences := scalariformPreferences.value
  .setPreference(AlignSingleLineCaseStatements, true)
  .setPreference(DoubleIndentConstructorArguments, true)
  .setPreference(DanglingCloseParenthesis, Preserve)