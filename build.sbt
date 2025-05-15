
ThisBuild / organization := "co.spendabit"
ThisBuild / scalaVersion := "2.12.18"
// ThisBuild / crossScalaVersions := Seq("2.12.18")
ThisBuild / version := "0.3.0"

lazy val testTools = project.in(file("."))
  .settings(
    name := "Test Tools",
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % "3.1.4",
      "com.storm-enroute" %% "scalameter" % "0.19",
      "org.eclipse.jetty" % "jetty-webapp" % "9.4.2.v20170220",
    )
  )

Compile / scalaSource := baseDirectory.value / "src"

publishMavenStyle := true
Test / publishArtifact := false
publishTo := sonatypePublishToBundle.value

sonatypeBundleDirectory := (ThisBuild / baseDirectory).value / target.value.getName / "sonatype-staging-abc" / (ThisBuild / version).value
sonatypeSessionName := s"[sbt-sonatype] test-tools ${version.value}"

licenses := Seq("The Unlicense" -> url("https://unlicense.org/"))

import xerial.sbt.Sonatype.GitHubHosting
sonatypeProjectHosting := Some(GitHubHosting("spendabit", "test-tools", email = "chris@spendabit.co"))
