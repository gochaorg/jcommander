package xyz.cofe.jtfm.ui.table

import xyz.cofe.term.ui.table._
import xyz.cofe.files._
import java.nio.file.Path

object FilesTable:

  class FilesColumns:
    val nameColumn = Column
      .id("file.name")
      .reader { (path:Path) => path.name }
      .text( text => text )
      .title("name")
      .widthAuto
      .leftAlign
      .build

    