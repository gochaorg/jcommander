package xyz.cofe.jtfm.store.json

import java.nio.file.Path
import xyz.cofe.jtfm.files.canonical
import xyz.cofe.jtfm.files.upPath
import xyz.cofe.jtfm.files.isDir

object AppHome:
  lazy val defaultName = ".jtfm"

  lazy val directory:Path = {
    val cd = Path.of(".")
    cd.upPath.reverse.foldLeft( None:Option[Path] ){ case(a,p) =>
      a match
        case Some(_) => a
        case None => 
          val trgt = p.resolve(defaultName)
          trgt.isDir match
            case Right(true) => Some(trgt)
            case _ => a
    }.getOrElse( cd.resolve(defaultName).canonical )
  }
