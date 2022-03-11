package xyz.cofe.jtfm.wid

import xyz.cofe.jtfm.ev.OwnProperty
import xyz.cofe.jtfm.gr.{Point, Rect}

import scala.ref.WeakReference

trait RectProperty[SELF : RepaitRequest] {
  self: Widget[_] =>
  
  lazy val rect:OwnProperty[Rect,SELF] =
    OwnProperty[Rect,SELF](Rect(0,0,1,1),self.asInstanceOf[SELF])
      .observe( (prop,old,cur)=>{
        val rep = implicitly[RepaitRequest[SELF]]
        rep.repaitRequest(prop.owner)
      })
      ._1
}

object RectProperty {
  implicit class RectProp[SELF](val prop: OwnProperty[Rect,SELF]) {
    def width:Int = prop.value.width
    def width_= (v:Int):Unit = {
      val p0 = prop.value.leftTop
      val p1 = prop.value.rightBottom
      val w = prop.value.width
      val h = prop.value.height
      prop.value = Rect( p0, p0.translate(v,h) )
    }
    
    def height:Int = prop.value.height
    def height_= (v:Int):Unit = {
      val p0 = prop.value.leftTop
      val p1 = prop.value.rightBottom
      val w = prop.value.width
      val h = prop.value.height
      prop.value = Rect( p0, p0.translate(w,v) )
    }
    
    object move {
      def relative( x:Int, y:Int ):Unit = {
        prop.value = Rect( prop.value.leftTop.translate(x,y), prop.value.rightBottom.translate(x,y) )
      }
      def relative( p:Point ):Unit = {
        relative( p.x, p.y )
      }
      
      def absolute( x:Int, y:Int ):Unit = {
        prop.value = Rect( Point(x,y), Point(x,y).translate(width, height) )
      }
      def absolute( p:Point ):Unit = absolute(p.x, p.y)
    }

    trait Binds {
      def unbind():Unit
    }
    
    def bind[SOME]( src: OwnProperty[Rect,SOME] )( eval: (OwnProperty[Rect,SELF], Rect)=>Rect ):Binds = {
      val ls = src.listener( (srcProp,old,cur) => {
        prop.value = eval(prop,cur)
      })
      ls.add()
      val lsWeak = WeakReference(ls)
      new Binds {
        override def unbind():Unit = {
          lsWeak.get match {
            case Some(ls) => ls.remove(); lsWeak.clear();
            case _ =>
          }
        }
      }
    }
    
    def bind[SOME <: Widget[SOME]]( src: Widget[SOME] )( eval: (OwnProperty[Rect,SELF], Rect)=>Rect ):Binds =
      bind( src.rect )(eval)
  }
}
