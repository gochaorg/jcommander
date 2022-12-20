package xyz.cofe.files

import java.nio.file.Path

trait AppHome(appName: String):
  def systemParameter_=(directory:Path):Unit =
    System.setProperty(s"${appName}.home",directory.toString())
  def systemParameter:Option[Path] =
    Option(System.getProperty(s"${appName}.home")).map(v => Path.of(v))
    
  def enviroment:Option[Path] =
    Option(System.getenv(appName.toUpperCase()+"_HOME")).map(v => Path.of(v))

  def localDir:Option[Path] =
    Path.of(".").canonical.upPath.reverse.foldLeft(None:Option[Path]) {
      case (res,pth) =>
        val dir = pth.resolve("."+appName)
        res match {
          case Some(value) => res
          case None => dir.isDirectory match {
            case Left(err) => res
            case Right(false) => res
            case Right(true) => Some(dir)
          }
        }
    }
  
  def homeDir: Option[Path] = {
    val dir = Path.of(System.getProperty("user.dir")).resolve("."+appName)
    dir.isDirectory match {
      case Right(true) => Some(dir)
      case _ => None
    }
  }

  def defaultDir:Path = Path.of(".").resolve("."+appName)

  lazy val directory:Path =
    systemParameter.getOrElse(
      enviroment.getOrElse(
        localDir.getOrElse(
          homeDir.getOrElse(
            defaultDir
          )
        )
      )
    )

