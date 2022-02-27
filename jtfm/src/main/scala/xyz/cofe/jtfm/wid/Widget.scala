package xyz.cofe.jtfm.wid

import xyz.cofe.jtfm.{Nested, Parent}

/**
 * Виджет - визуальный элемент для рендера и управления данными
 */
trait Widget[SELF <: Widget[SELF]]
  extends
    Parent[SELF, Widget[_]] // Свойство parent
    , Nested[SELF, Widget[_]] // Дочерние объекты
{
  val me = this.asInstanceOf[Widget[_]]
  nested.listen((coll, idx, old, cur) => {
    cur match {
      case Some(c) => c.parent.value(Some(me))
      case _ =>
    }
    old match {
      case Some(o) => o.parent.value match {
        case Some(p) => if (p == me) {
          o.parent.value(None)
        }
        case _ =>
      }
      case _ =>
    }
  })
  
  /**
   * Путь от корня к узлу
   *
   * @return путь
   */
  def widgetPath: List[Widget[_]] = path {
    _ match {
      case w: Widget[_] => Some(w)
      case _ => None
    }
  }
}
