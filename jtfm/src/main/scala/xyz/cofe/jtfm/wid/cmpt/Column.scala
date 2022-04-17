package xyz.cofe.jtfm.wid.cmpt

/**
 * Колонка таблицы
 */
class Column[Row,V](
  val name:String,
  val reader:Row=>V,
  val render:V=>String
) {
  def asString(row:Row):String = render( reader(row) )
  // object width {
  //   var prefect:Option[Int] = None
  // }
}

object Column {
  def apply[Row,V]( name:String, reader:Row=>V, render:V=>String ):Column[Row,V] = { 
    new Column[Row,V]( name, reader, render )
  }
}
