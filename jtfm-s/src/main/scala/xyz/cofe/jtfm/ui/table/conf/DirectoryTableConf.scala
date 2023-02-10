package xyz.cofe.jtfm.ui.table
package conf

import java.nio.file.Path
import xyz.cofe.json4s3.derv.ToJson

case class DirectoryTableConf(
  moveParentNormalizePath:Boolean = true,
  forceFirstRowFocused:Boolean = true,
  clearSelectionOnCD:Boolean = true,
  directory: Option[Path] = None,  
)

object DirectoryTableConf:
  implicit val defaultConf: DirectoryTableConf = DirectoryTableConf()
