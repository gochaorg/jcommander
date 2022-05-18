package xyz.cofe.jtfm.wid.cmpt

/**
 * Колонка таблицы
 * @param name Имя колонки
 * @param reader Чтение значение
 * @param render Рендер значения
 */
case class Column[Row,V](
  /** Имя колонки */
  val name:String,

  /** Чтение значение */
  val reader:Row=>V,

  /** Рендер значения */
  val render:V=>String,

  /** Функция сравнения */
  val comparator:Option[(V,V)=>Int] = None
) {
  def asString(row:Row):String = render( reader(row) )

  /** Ширина колонки */
  object width {
    /** Предпочитаемая ширина */
    var prefect:Option[Int] = None
  }

  /** Указать функцию сортировки */
  def withSort( cmp:(V,V)=>Int ):Column[Row,V] = copy(comparator = Some(cmp))
}

object Column {
  def apply[Row,V]( name:String, reader:Row=>V, render:V=>String ):Column[Row,V] = { 
    new Column[Row,V]( name, reader, render )
  }
  def create[Row,V]( name:String, reader:Row=>V, render:V=>String )(using ord:Ordering[V]):Column[Row,V] = { 
    new Column[Row,V]( name, reader, render ).withSort { (a,b) => 
      ord.compare( a, b )
    }
  }
}
