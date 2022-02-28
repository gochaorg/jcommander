package xyz.cofe.jtfm.wid

import xyz.cofe.jtfm.{LikeTree, Nested, Parent}

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

object Widget {
  import xyz.cofe.jtfm.LikeTree
  implicit val likeTree: LikeTree[Widget[_]] = new {
    def parent(n:Widget[_]):Option[Widget[_]] = n.parent.value
    def childrenCount(n:Widget[_]):Int = n.nested.size
    def indexOf(parent:Widget[_], child:Widget[_]):Option[Int] = {
      val x = parent.nested.zip(0 until parent.nested.size).filter( (w,i) => w==child && w.isInstanceOf[Widget[_]] ).map( (w,i) => (w.asInstanceOf[Widget[_]], i) );
      x.headOption.map( (_,i) => i )
    }
    def child(parent:Widget[_], idx:Int):Option[Widget[_]] = {
      val x = parent.nested.zip(0 until parent.nested.size).filter( (w,i) => i==idx && w.isInstanceOf[Widget[_]] ).map( (w,i) => (w.asInstanceOf[Widget[_]], i) );
      x.headOption.map( (w,_) => w )
    }
  }
}
