package xyz.cofe.jtfm.wid

import com.googlecode.lanterna.graphics.TextGraphics
import xyz.cofe.jtfm.ev.OwnProperty

/** Свойство: "укрывитость" - закрашивать фон виджета или нет */
trait OpaqueProperty[SELF : RepaitRequest] {
  self: Widget[_] =>
  
  /** Свойство: "укрывитость" - закрашивать фон виджета или нет */
  lazy val opaque:OwnProperty[Boolean,SELF] =
    OwnProperty(true,self.asInstanceOf[SELF])
      .observe( (prop,old,cur)=>{
        val rep = implicitly[RepaitRequest[SELF]]
        rep.repaitRequest(prop.owner)
      })
      ._1
}

object OpaqueProperty {
  implicit class OpaqueOps[W <: OpaqueProperty[W] & BackgroundProperty[W] & RectProperty[W]]( self: W ) {
    def renderOpaque( gr: TextGraphics ):Unit = {
      if( self.opaque.value ){
        gr.setBackgroundColor(self.background.value)
        (0 until self.rect.height).foreach { y =>
          gr.putString(0,y," ".repeat(self.rect.width))
        }
      }
    }
  }
}