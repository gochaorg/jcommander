package xyz.cofe.jtfm.wid.cmpt

import xyz.cofe.jtfm.ev.OwnProperty
import java.nio.file.Path
import java.nio.file.Files

class DirectoryTable extends FilesTable {
  private def log(e:Throwable):Unit = {    
  }

  val currentDir:OwnProperty[Option[Path],DirectoryTable] = OwnProperty(None,this)
  currentDir.listen( (_,_,cdOpt) => {
    cdOpt match {
      case None =>
        selection.clear()
        data = List()
      case Some(cd) =>
        try {
          var files = List[Path]()
          val ds = Files.newDirectoryStream(cd)
          ds.forEach { path =>
            files = path :: files
          }
          ds.close()

          selection.clear()
          data = files
        } catch {
          case e:Throwable => log(e)
        }
    }
  })
}
