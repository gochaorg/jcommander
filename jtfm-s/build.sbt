val scala3Version = "3.2.1"

//resolvers += Resolver.mavenLocal
resolvers += MavenCache("local-maven", file("/home/user/.m2/repository"))
// resolvers += Resolver.mavenLocal

//resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
resolvers += "maven central" at "https://repo1.maven.org/maven2"

//libraryDependencies += "xyz.cofe" % "term-common" % "0.2" withSources()
libraryDependencies += "xyz.cofe" % "term-common" % "0.3" withSources()
libraryDependencies += "xyz.cofe" %% "json4s3" % "0.1.0" 

lazy val root = project
  .in(file("."))
  .settings(
    name := "jtfm-s",
    version := "0.1.0-SNAPSHOT",
    scalaVersion := scala3Version,
    libraryDependencies += "org.scalameta" %% "munit" % "0.7.29" % Test
  )
