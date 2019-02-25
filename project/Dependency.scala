import sbt._
/** dependencies */
object Dependency {
  val akkaStreamsV  = "2.5.14"
  val scalaTestV    = "3.0.4"
  
  val akkaStreams         = "com.typesafe.akka"   %% "akka-stream"          % akkaStreamsV

  // test dependencies
  val scalaTest           = "org.scalatest"       %% "scalatest"            % scalaTestV % Test
}
