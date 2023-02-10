package xyz.cofe.jtfm.conf

import xyz.cofe.jtfm.ui.table.conf.DirectoryTableConf
import java.nio.file.Path
import xyz.cofe.files.AppHome
import xyz.cofe.json4s3.derv.ToJson
import xyz.cofe.json4s3.derv.FromJson
import xyz.cofe.files.pathToJson
import xyz.cofe.files.pathFromJson

case class LeftRightDirs(
  left: DirectoryTableConf,
  right: DirectoryTableConf
)

object LeftRightDirs:

  def x = {
    import xyz.cofe.json4s3.derv._
    val d : DirectoryTableConf = ???
    d.json
  }
  val defaultConf : LeftRightDirs = LeftRightDirs(
    left  = DirectoryTableConf.defaultConf.copy(directory = Some(Path.of("."))),
    right = DirectoryTableConf.defaultConf.copy(directory = Some(Path.of("."))),
  )

  def confFile(appHome:AppHome):ConfFile[LeftRightDirs] = 
    ConfFile.File(appHome.directory.resolve("left-right.jsonc"))

  def read(using appHome:AppHome):Either[ConfError,LeftRightDirs] =
    confFile(appHome).read

  def write(value:LeftRightDirs)(using appHome:AppHome):Either[ConfError,Unit] =
    confFile(appHome).write(value)
