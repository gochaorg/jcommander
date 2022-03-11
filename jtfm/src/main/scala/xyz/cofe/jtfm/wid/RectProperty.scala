package xyz.cofe.jtfm.wid

import xyz.cofe.jtfm.ev.OwnProperty
import xyz.cofe.jtfm.gr.Rect

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
  implicit class RectProp[SELF](val rp: OwnProperty[Rect,SELF]) {
    def width:Int = rp.value.width
    def width_= (v:Int):Unit = {
      val p0 = rp.value.leftTop
      val p1 = rp.value.rightBottom
      val w = rp.value.width
      val h = rp.value.height
      rp.value = Rect( p0, p0.translate(v,h) )
    }
    
    def height:Int = rp.value.height
    def height_= (v:Int):Unit = {
      val p0 = rp.value.leftTop
      val p1 = rp.value.rightBottom
      val w = rp.value.width
      val h = rp.value.height
      rp.value = Rect( p0, p0.translate(w,v) )
    }
  }
}
