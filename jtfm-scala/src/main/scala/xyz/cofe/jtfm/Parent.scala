package xyz.cofe.jtfm

import xyz.cofe.jtfm.ev.OwnProperty

/**
 * Свойство parent - указывает на родительский узел в дереве
 *
 * @tparam SELF собственный тип
 * @tparam PRNT тип родителя
 */
trait Parent[SELF, PRNT] {
  /**
   * Возвращает родительское свойство
   */
  lazy val parent: OwnProperty[Option[PRNT], SELF] = OwnProperty(None,this.asInstanceOf[SELF])
  
  /**
   * Возвращает путь от корня к текущему узлу
   * @param node функция получения узла
   * @tparam N тип узла
   * @return список узлов, от корня к текущему
   */
  def path[N]( node:Any=>Option[N] ):List[N] = {
    var lst = List[N]()
    node(this) match {
      case Some(n) => lst = n :: lst
      case _ =>
    }
    parent.value match {
      case Some(p) => p match {
        case x:Parent[_,_] =>
          val pp = x.path(node).asInstanceOf[List[N]]
          lst = pp ::: lst
        case _ =>
      }
      case _ =>
    }
    lst
  }
}
