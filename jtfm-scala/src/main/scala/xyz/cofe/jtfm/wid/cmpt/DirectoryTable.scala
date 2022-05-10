package xyz.cofe.jtfm.wid.cmpt

import xyz.cofe.jtfm.ev.OwnProperty
import java.nio.file.Path
import java.nio.file.Files
import org.slf4j.LoggerFactory

class DirectoryTable extends FilesTable {
  private val log = LoggerFactory.getLogger(classOf[DirectoryTable])

  val currentDir:OwnProperty[Option[Path],DirectoryTable] = OwnProperty(None,this)
  currentDir.listen( (_,_,cdOpt) => {
    readDir(cdOpt)
  })

  def readDir( cdOpt:Option[Path] ):Unit = {
    cdOpt match {
      case None =>
        log.info("read dir none")
        selection.clear()
        data = List()
      case Some(cd) =>
        log.info(s"read the dir ${cd}")
        try {
          var files = List[Path]()
          val ds = Files.newDirectoryStream(cd)
          ds.forEach { path =>
            files = path :: files
          }
          ds.close()

          selection.clear()
          data = files.sortWith( (a,b) => FilesTable.sort.defaultSort(a,b)<0 )
        } catch {
          case e:Throwable => log.warn(s"can't read dir $cd",e)
        }
    }
  }

  
}
