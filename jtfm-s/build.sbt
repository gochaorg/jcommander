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
libraryDependencies += "xyz.cofe" % "term-common" % "0.3.1"
libraryDependencies += "xyz.cofe" %% "json4s3" % "0.1.2" 

////////////////////////////////////////////////////////////////////////

scalacOptions ++= Seq(
  "-Xmax-inlines:64"
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
val binBashScriptSrc  = BashScript (mainClass,jvmOpts=jvmOpts).fullScript
val binBatchScriptSrc = BatchScript(mainClass,jvmOpts=jvmOpts, javaExe=JavaExe.window).fullScript

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