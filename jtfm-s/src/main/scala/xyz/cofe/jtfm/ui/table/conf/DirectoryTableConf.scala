package xyz.cofe.jtfm.ui.table
package conf

import java.nio.file.Path

case class DirectoryTableConf(
  moveParentNormalizePath:Boolean = true,
  forceFirstRowFocused:Boolean = true,
  clearSelectionOnCD:Boolean = true,
  directory: Option[Path] = None,  
)

object DirectoryTableConf:
  implicit val defaultConf: DirectoryTableConf = DirectoryTableConf()