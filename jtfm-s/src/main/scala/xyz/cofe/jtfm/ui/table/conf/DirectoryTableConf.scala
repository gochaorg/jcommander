package xyz.cofe.jtfm.ui.table
package conf

import java.nio.file.Path
import xyz.cofe.term.ui.table.conf.ColumnConf

case class DirectoryTableConf(
  moveParentNormalizePath:Boolean = true,
  forceFirstRowFocused:Boolean = true,
  clearSelectionOnCD:Boolean = true,
  directory: Option[Path] = None,
  columns: List[ColumnConf] = List.empty
):
  def merge( tbl:DirectoryTable ):DirectoryTableConf =
    copy(
      directory = tbl.directory.get,
      columns = tbl.columns.map( c => ColumnConf.from(c) ).toList
    )

object DirectoryTableConf:
  implicit val defaultConf: DirectoryTableConf = DirectoryTableConf()
