import _root_.dist.JvmOpt
import java.nio.charset.StandardCharsets
import dist._

val scala3Version = "3.2.1"

////////////////////////////////////////////////////////////////////////

resolvers += MavenCache("local-maven", file("/home/user/.m2/repository"))
// resolvers += Resolver.mavenLocal

//resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
resolvers += "maven central" at "https://repo1.maven.org/maven2"

//libraryDependencies += "xyz.cofe" % "term-common" % "0.2" withSources()
//libraryDependencies += "xyz.cofe" % "term-common" % "0.3" withSources()
// libraryDependencies += "xyz.cofe" % "term-common" % "0.3.1" // from "file:/home/user/code/term-common-parent/term-common/target/term-common-0.3.1-SNAPSHOT.jar"
libraryDependencies += "xyz.cofe" % "term-common" % "0.3.2-SNAPSHOT" from "file:/home/user/code/term-common-parent/term-common/target/term-common-0.3.2-SNAPSHOT.jar"

libraryDependencies += "xyz.cofe" %% "json4s3" % "0.1.2" 
//libraryDependencies += "org.slf4j" % "slf4j-api" % "2.0.6"
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.4.5"

libraryDependencies += "io.undertow" % "undertow-core" % "2.1.0.Final"
//libraryDependencies += "com.sparkjava" % "spark-core" % "2.9.4"
libraryDependencies += "io.monix" %% "monix" % "3.4.0"


////////////////////////////////////////////////////////////////////////

scalacOptions ++= Seq(
  "-Xmax-inlines:128"
)

lazy val root = project
  .in(file("."))
  .settings(
    name := "jtfm-s",
    version := "0.1.0-SNAPSHOT",
    scalaVersion := scala3Version,
    libraryDependencies += "org.scalameta" %% "munit" % "0.7.29" % Test
  )

//////////////////////////////////////////////////////////////
val distDir = settingKey[File]("Distributive directory")
distDir :=  baseDirectory.value / "dist"

val distJarsDirectory             = settingKey[File]("Where to copy all libs and built artifact")
distJarsDirectory                := distDir.value / "lib" / "jar"

val copyAllLibsAndArtifact  = taskKey[Unit]("Copy runtime dependencies and built artifact to 'distJarsDirectory'")
copyAllLibsAndArtifact   := {
  //val allLibs:                List[File]          = dependencyClasspath.in(Runtime).value.map(_.data).filter(_.isFile).toList
  val allLibs:                List[File]          = ( Runtime / dependencyClasspath ).value.map(_.data).filter(_.isFile).toList
  //val buildArtifact:          File                = packageBin.in(Runtime).value
  val buildArtifact:          File                = ( Runtime / packageBin ).value
  val jars:                   List[File]          = buildArtifact :: allLibs
  val `mappings src->dest`:   List[(File, File)]  = jars.map(f => (f, distJarsDirectory.value / f.getName))
  val log                                         = streams.value.log
  log.info(s"Copying to ${distJarsDirectory.value}:")
  log.info(s"  ${`mappings src->dest`.map(_._1).mkString("\n")}")
  IO.copy(`mappings src->dest`)
}

val distBinDir = taskKey[File]("Prepare dist / bin")
distBinDir := {
  val dist = distDir.value
  val bin = new File(dist, "bin")
  if( bin.exists ){
    bin.mkdirs()
  }
  bin
}

val mainClass = "xyz.cofe.jtfm.Main"

val jvmOpts = List[JvmOpt](
  JvmOpt.Custom("-Dxyz.cofe.term.default=auto")
)
val winJvmOpts = JvmOpt.Custom("-Djtfm.console=win") :: jvmOpts
val nixJvmOpts = JvmOpt.Custom("-Djtfm.console=nix") :: jvmOpts

val binBashScriptSrc  = BashScript (mainClass,jvmOpts=nixJvmOpts).fullScript
val binBatchScriptSrc = BatchScript(mainClass,jvmOpts=winJvmOpts, javaExe=JavaExe.window).fullScript

val bashScript = taskKey[Unit]("Generate bash launch script")
bashScript := {
  val file = new File(distBinDir.value, "jtfm.sh")
  IO.write(file, binBashScriptSrc.getBytes("UTF-8") )
  file.setExecutable(true)
}

val batScript = taskKey[Unit]("Generate batch launch script")
batScript := {
  val file = new File(distBinDir.value, "jtfm.bat")
  IO.write(file, binBatchScriptSrc.getBytes(StandardCharsets.ISO_8859_1) )
  file.setExecutable(true)
}

val dist = taskKey[Unit]("Generate dist")
dist := {
  batScript.value
  bashScript.value
  copyAllLibsAndArtifact.value
}


val distClean = taskKey[Unit]("clean dist")
distClean := {
  val distDir0 = distDir.value
  IO.delete(distDir0)
}