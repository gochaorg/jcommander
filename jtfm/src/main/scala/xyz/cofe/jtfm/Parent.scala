package xyz.cofe.jtfm

/**
 * Свойство parent - указывает на родительский узел в дереве
 * @tparam SELF собственный тип
 * @tparam PRNT тип родителя
 */
trait Parent[SELF,PRNT <: Parent[_,_]] {
  /**
   * Возвращает родительское свойство
   */
  lazy val parent: OwnProperty[Option[PRNT],SELF]
}
