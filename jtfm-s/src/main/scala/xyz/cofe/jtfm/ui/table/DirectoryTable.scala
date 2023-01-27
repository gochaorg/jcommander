package xyz.cofe.jtfm.ui.table

import xyz.cofe.term.ui.Table
import java.nio.file.Path
import xyz.cofe.lazyp.Prop
import xyz.cofe.term.ui.Session
import xyz.cofe.files._

class DirectoryTable
extends Table[Path]:
  val directory = Prop.rw(None:Option[Path])
  directory.onChange {
    directory.get.map { dir => 
      dir.readDir
    }.getOrElse( Right(List.empty[Path]) )
    .foreach { files =>
      rows.clear()
      rows.append(files)
    }
  }

  columns.append(FilesTable.columns)