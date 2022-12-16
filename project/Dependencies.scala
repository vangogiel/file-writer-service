import sbt._

object Dependencies {
  val scalaTestVersion = "3.2.9"
  val scalaTestPlusVersion = "5.1.0"

  object Provided {
    val macwire: ModuleID = "com.softwaremill.macwire" %% "macros" % "2.5.8" % "provided"
  }

  object Compile {
    val confluentReleases = "confluent" at "https://packages.confluent.io/maven/"
    val playJsonDerivedCodecs: ModuleID = "org.julienrf" %% "play-json-derived-codecs" % "7.0.0"
    val logStashEncoder = "net.logstash.logback" % "logstash-logback-encoder" % "6.4"
    val logback = "ch.qos.logback" % "logback-classic" % "1.4.3"
  }

  object Test {
    val scalaTest = "org.scalatest" %% "scalatest" % scalaTestVersion % "test"
    val scalaTestPlays = "org.scalatestplus.play" %% "scalatestplus-play" % scalaTestPlusVersion % "test"
    val scalaMock = "org.scalamock" %% "scalamock" % "4.4.0" % "test"
  }
}
