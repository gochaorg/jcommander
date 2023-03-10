package xyz.cofe.jtfm.wid

import xyz.cofe.jtfm.{LikeTree, Nested, Parent}
import com.googlecode.lanterna.input.KeyStroke
import xyz.cofe.jtfm.gr.Point
import xyz.cofe.jtfm.Navigate
import xyz.cofe.jtfm.NavigateFilter

/**
 * Виджет - визуальный элемент для рендера и управления данными
 */
trait Widget[SELF <: Widget[SELF]]
  extends
    Parent[SELF, Widget[_]]   // Свойство parent
    , Nested[SELF, Widget[_]] // Дочерние объекты
    , RectProperty[SELF]      // Расположение виджета
    , VisibleProperty[SELF]   // Видим виджет
    , Render                  // Рендер
    , UserInput[KeyStroke]    // События пользовательского ввода
{
  private val repeaitReq = RepaitRequest.currentCycle[SELF]
  def repaint():Unit = repeaitReq.repaitRequest(this.asInstanceOf[SELF])

  private val me = this.asInstanceOf[Widget[_]]
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

  private val self1 = this

  def widgetTree: Iterator[Widget[_]] = new Iterator[Widget[_]] {
    given navFilter:NavigateFilter[Widget[_]] = NavigateFilter.any
    val nav = Navigate.deepOrder[Widget[_]]
    var from = self1.asInstanceOf[Widget[_]]

    def next: Widget[_] = {
      val res = from
      if( from!=null ){
        from = nav.next(from) match {
          case Some(next_w) => next_w            
          case None => null
        }
      }
      res
    }
    def hasNext: Boolean = from!=null
  }
}

object Widget {
  import xyz.cofe.jtfm.LikeTree
  
  implicit val likeTree: LikeTree[Widget[_]] = new {
    def parent(n:Widget[_]):Option[Widget[_]] = n.parent.value
    def childrenCount(n:Widget[_]):Int = n.nested.size
    def indexOf(parent:Widget[_], child:Widget[_]):Option[Int] = {
      val x = parent.nested.zip(0 until parent.nested.size).filter( (w,i) => w==child && w.isInstanceOf[Widget[_]] ).map( (w,i) => (w.asInstanceOf[Widget[_]], i) );
      val xi = x.headOption.map( (_,i) => i )
      xi
    }
    def child(parent:Widget[_], idx:Int):Option[Widget[_]] = {
      val x = parent.nested.zip(0 until parent.nested.size).filter( (w,i) => i==idx && w.isInstanceOf[Widget[_]] ).map( (w,i) => (w.asInstanceOf[Widget[_]], i) );
      x.headOption.map( (w,_) => w )
    }
  }
  
  implicit class PointOps( p:Point ) {
    /**
     * Конвертирует точку в абсолютные координаты
     * @return абсолютная координата
     */
    def toAbsolute[W <: Widget[_]](w:W):Point =
      w.widgetPath
        .map( w => w.rect.value.leftTop )
        .foldLeft(p)((a,b)=>{a.translate(b)})
  
    /**
     * Конвертирует точку в локальные координаты
     * @return лоакльная координата
     */
    def toLocal[W <: Widget[_]](w:W):Point =
      w.widgetPath
        .map( w => w.rect.value.leftTop.invert() )
        .foldLeft(p)((a,b)=>{a.translate(b)})
  }
}
