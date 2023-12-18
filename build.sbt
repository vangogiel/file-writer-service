import CiCommands.{ ciBuild, devBuild }

ThisBuild / version := "0.1"

ThisBuild / scalaVersion := "2.13.6"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .disablePlugins(PlayLayoutPlugin)
  .settings(
    name := "file-writer-exercise",
    libraryDependencies ++=
      Seq(
        Dependencies.Provided.macwire,
        Dependencies.Compile.playJsonDerivedCodecs,
        Dependencies.Compile.logStashEncoder,
        Dependencies.Compile.logback,
        Dependencies.Compile.catsEffect,
        Dependencies.Compile.catsLawEffect,
        Dependencies.Compile.akkaActor,
        Dependencies.Test.scalaTest,
        Dependencies.Test.scalaTestPlays,
        Dependencies.Test.scalaMock,
        Dependencies.Test.akkaActorTypedTestKit
      ),
    resolvers += Dependencies.Compile.confluentReleases,
    routesImport := Seq(),
    commands ++= Seq(devBuild, ciBuild),
    coverageExcludedPackages := "<empty>;Reverse.*;router\\.*"
  )

javaOptions ++= Seq(
  "-XX:MaxRAMPercentage=50"
)

scalacOptions ++=
  Seq(
    "-deprecation",
    "-feature",
    "-deprecation",
    "-unchecked",
    "-Xcheckinit",
    "-Xfatal-warnings",
    "-Xlint:adapted-args",
    "-Ywarn-dead-code",
    "-Ywarn-unused:implicits",
    "-Ywarn-unused:locals",
    "-Ywarn-unused:params",
    "-Ywarn-unused:patvars",
    "-Ywarn-unused:privates",
    "-Ywarn-unused:imports"
  )
