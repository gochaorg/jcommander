import sbt.Keys.libraryDependencies

val scala3Version = "3.1.1"

lazy val root = project
  .in(file("."))
  .settings(
    name := "jtfm",
    version := "0.1.0-SNAPSHOT",

    scalaVersion := scala3Version,

    libraryDependencies += "com.novocode" % "junit-interface" % "0.11" % "test"
  )

scalacOptions += "-target:jvm-1.8"
libraryDependencies += "com.googlecode.lanterna" % "lanterna" % "3.1.1"

lazy val hello = taskKey[Unit]("sample task")
hello := println("hello print")