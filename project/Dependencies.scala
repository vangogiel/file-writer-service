import sbt._

object Dependencies {
  val scalaTestVersion = "3.2.9"
  val scalaTestPlusVersion = "5.1.0"
  val catsVersion = "2.3.0"
  val AkkaVersion = "2.6.20"
  val doobieVersion = "0.13.3"

  object Provided {
    val macwire: ModuleID = "com.softwaremill.macwire" %% "macros" % "2.5.8" % "provided"
  }

  object Compile {
    val akkaActor = "com.typesafe.akka" %% "akka-actor" % AkkaVersion
    val confluentReleases = "confluent" at "https://packages.confluent.io/maven/"
    val playJsonDerivedCodecs: ModuleID = "org.julienrf" %% "play-json-derived-codecs" % "7.0.0"
    val logStashEncoder = "net.logstash.logback" % "logstash-logback-encoder" % "6.4"
    val logback = "ch.qos.logback" % "logback-classic" % "1.4.3"
    val catsEffect: ModuleID = "org.typelevel" %% "cats-effect" % catsVersion
    val catsLawEffect: ModuleID = "org.typelevel" %% "cats-effect-laws" % catsVersion
  }

  object Test {
    val scalaTest = "org.scalatest" %% "scalatest" % scalaTestVersion % "test"
    val scalaTestPlays = "org.scalatestplus.play" %% "scalatestplus-play" % scalaTestPlusVersion % "test"
    val scalaMock = "org.scalamock" %% "scalamock" % "4.4.0" % "test"
    val akkaActorTypedTestKit = "com.typesafe.akka" %% "akka-actor-testkit-typed" % AkkaVersion % "test"
  }
}
