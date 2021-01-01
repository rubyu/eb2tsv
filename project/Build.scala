import sbt.Keys._
import sbt._
import sbtassembly.AssemblyPlugin.autoImport._

object Build extends sbt.Build {
  lazy val commonSettings =
    Defaults.coreDefaultSettings ++
      Seq(
        version := "1.0.0",
        scalaVersion := "2.11.8",
        organization := "com.github.rubyu",
        name := "eb2tsv"
      )

  lazy val project =
    Project("ebquery", file("."))
      .settings(commonSettings: _*)
      .settings(Seq(
        mainClass in assembly := Some("com.github.rubyu.ebquery.Main"),
        assemblyJarName in assembly := name.value + "-" + version.value + ".jar"
      ))
      .settings(Seq(
        scalacOptions := Seq(
          "-deprecation",
          "-unchecked",
          "-feature"
        )
      ))
      .settings(
        libraryDependencies ++= Seq(
          "org.scala-lang.modules" % "scala-xml_2.11" % "1.0.4",
          "org.slf4j" % "slf4j-api" % "1.7.21",
          "org.slf4j" % "slf4j-simple" % "1.7.21",
          "args4j" % "args4j" % "2.0.26",
          "commons-codec" % "commons-codec" % "1.9",
          "commons-lang" % "commons-lang" % "2.4",
          "com.github.tototoshi" % "scala-csv_2.11" % "1.3.6",
          "org.specs2" %% "specs2-core" % "3.7.2" % "test",
          "junit" % "junit" % "4.7" % "test",
          "com.rexsl" % "rexsl-w3c" % "0.13" % "test",
          "com.rexsl" % "rexsl-test" % "0.4.12" % "test",
          "javax.json" % "javax.json-api" % "1.0" % "test",
          // halt warning messages for multiple dependencies
          "org.scala-lang" % "scala-reflect" % "2.11.8" % "test",
          "org.scala-lang" % "scala-compiler" % "2.11.8" % "test",
          // halt warning messages for circular dependencies
          "com.jcabi" % "jcabi-log" % "0.12.1" % "test"
        )
      )
}
