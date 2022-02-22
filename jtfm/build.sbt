import sbt.Keys.libraryDependencies

val scala3Version = "3.1.1"

lazy val root = project
  .in(file("."))
  .settings(
    name := "jtfm",
    version := "0.1.0-SNAPSHOT",
    scalaVersion := scala3Version,
    scalacOptions += "-target:jvm-1.8",
    libraryDependencies += "com.novocode" % "junit-interface" % "0.11" % "test",
    libraryDependencies += "com.googlecode.lanterna" % "lanterna" % "3.1.1",
    assembly / assemblyJarName := "jtfm.jar",
    assembly / mainClass := Some("xyz.cofe.jtfm.hello"),
  )
