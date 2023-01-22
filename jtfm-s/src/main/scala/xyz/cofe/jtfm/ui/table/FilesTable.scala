package xyz.cofe.jtfm.ui.table

import xyz.cofe.term.ui.table._
import xyz.cofe.files._
import java.nio.file.Path
import xyz.cofe.term.ui.Table

object FilesTable:

  def columns:List[Column[Path,_]] =
    val cols = new FilesColumns()
    List(
      cols.nameColumn,
      cols.sizeHumanReadableColumn,
    )

  class FilesColumns:
    val nameColumn = Column
      .id("file.name")
      .reader { (path:Path) => path.name }
      .text( text => text )
      .title("name")
      .widthAuto
      .leftAlign
      .build

    val sizeHumanReadableColumn = Column
      .id("file.size.h")
      .reader { (path:Path) => path.size }
      .text { etSize => etSize.map(sz => ByteSize(sz).humanReadable.toString()).getOrElse("?") }
      .width(8)
      .build

    //val fileTypeSymbolColumn

class FilesTable extends Table[Path]