package xyz.cofe.jtfm.wid.cmpt

import xyz.cofe.jtfm.ev.OwnProperty
import java.nio.file.Path
import java.nio.file.Files
import org.slf4j.LoggerFactory
import com.googlecode.lanterna.TextColor
import xyz.cofe.jtfm.gr.Align
import xyz.cofe.jtfm.wid.RectProperty._
import xyz.cofe.jtfm.gr.Rect

class DirectoryTable extends FilesTable {
  private val log = LoggerFactory.getLogger(classOf[DirectoryTable])

  val currentDir:OwnProperty[Option[Path],DirectoryTable] = OwnProperty(None,this)
  currentDir.listen( (_,_,cdOpt) => {
    readDir(cdOpt)
  })

  val currentDirLabel:Label = Label()
  currentDirLabel.foreground.value = TextColor.ANSI.BLACK
  currentDirLabel.background.value = TextColor.ANSI.WHITE
  currentDirLabel.halign.value = Align.End
  currentDirLabel.rect.bindTo( this ){ rct => Rect(2,0).size(rct.width-2-1,1) }
  currentDir.listen( (_,_,cdOpt) => {
    cdOpt match {
      case None     => currentDirLabel.visible.value = false
      case Some(cd) => 
        currentDirLabel.visible.value = true
        currentDirLabel.text.value = cd.toString
    }
  })
  nested.append(currentDirLabel)

  protected def readDir( cdOpt:Option[Path] ):Unit = {
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
