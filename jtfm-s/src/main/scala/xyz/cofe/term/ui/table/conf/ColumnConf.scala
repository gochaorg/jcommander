package xyz.cofe.term.ui.table
package conf

import xyz.cofe.jtfm.ui.table.DirectoryTable

case class ColumnConf(
  id: String,
  preferredWidth: PreferredWidth,
  horizontalAlign: HorizontalAlign,
  leftDelimiter: Delimeter,
  rightDelimiter: Delimeter,
  title: Option[String],
):
  def applyTo(col:Column[?,?]):Unit =    
    title.foreach(t => col.title.set(t))
    col.preferredWidth.set(preferredWidth)
    col.horizontalAlign.set(horizontalAlign)
    col.leftDelimiter.set(leftDelimiter)
    col.rightDelimiter.set(rightDelimiter)

object ColumnConf:
  def from(col:Column[?,?]):ColumnConf =
    ColumnConf(
      id = col.id,
      preferredWidth = col.preferredWidth.get,
      horizontalAlign = col.horizontalAlign.get,
      leftDelimiter = col.leftDelimiter.get,
      rightDelimiter = col.rightDelimiter.get,
      title = Option(col.title.get)
    )
