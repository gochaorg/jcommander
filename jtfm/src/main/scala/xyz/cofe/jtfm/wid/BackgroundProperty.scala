package xyz.cofe.jtfm.wid

import com.googlecode.lanterna.TextColor
import xyz.cofe.jtfm.ev.OwnProperty

trait BackgroundProperty[SELF : RepaitRequest] {
  self: Widget[_] =>
  
  lazy val background:OwnProperty[TextColor,SELF] =
    OwnProperty( TextColor.ANSI.BLACK.asInstanceOf[TextColor] ,self.asInstanceOf[SELF])
      .observe( (prop,old,cur)=>{
        val rep = implicitly[RepaitRequest[SELF]]
        rep.repaitRequest(prop.owner)
      })
      ._1
}
