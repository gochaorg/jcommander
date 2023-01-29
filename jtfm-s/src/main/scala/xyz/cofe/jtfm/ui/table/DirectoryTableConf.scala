package xyz.cofe.jtfm.ui.table

trait DirectoryTableConf:
  def moveParentNormalizePath:Boolean
  def forceFirstRowFocused:Boolean
  def clearSelectionOnCD:Boolean

object DirectoryTableConf:
  given DirectoryTableConf with
    override def moveParentNormalizePath: Boolean = true
    override def forceFirstRowFocused: Boolean = true
    override def clearSelectionOnCD: Boolean = true