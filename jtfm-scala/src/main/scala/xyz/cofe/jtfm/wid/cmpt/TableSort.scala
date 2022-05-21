package xyz.cofe.jtfm.wid.cmpt

/** Правило сортировки данных внутри таблицы */
case class TableSort(
  columns:Seq[(Column[_,_],Boolean)] = List()
) {
  /** Нет правил сортировки */
  lazy val empty:Boolean = columns.isEmpty

  /** Есть правила сортировки */
  lazy val nonEmpty:Boolean = columns.nonEmpty

  /** Содержит среди правил сортировки указанную колонку */
  def contains(col:Column[_,_]):Boolean = columns.exists( _._1 == col )

  /** Сортировать в прямом порядке указанную колонку */  
  def asc(col:Column[_,_]):Option[Boolean] = columns.find( _._1 == col ).map( _._2 )

  /** Сортировать в обратном порядке указанную колонку */
  def desc(col:Column[_,_]):Option[Boolean] = columns.find( _._1 == col ).map( !_._2 )
}
