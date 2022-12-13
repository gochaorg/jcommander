val scala3Version = "3.2.1"

resolvers += Resolver.mavenLocal

libraryDependencies += "xyz.cofe" % "term-common" % "0.2" withSources()

lazy val root = project
  .in(file("."))
  .settings(
    name := "jtfm-s",
    version := "0.1.0-SNAPSHOT",
    scalaVersion := scala3Version,
    libraryDependencies += "org.scalameta" %% "munit" % "0.7.29" % Test
  )
