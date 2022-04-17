package xyz.cofe.jtfm.wid

import com.googlecode.lanterna.TextColor
import xyz.cofe.jtfm.ev.OwnProperty

/** Свойство: цвет фона */
trait BackgroundProperty[SELF : RepaitRequest] {
  self: Widget[_] =>
  
  /** Свойство: цвет фона */
  lazy val background:OwnProperty[TextColor,SELF] =
    OwnProperty( TextColor.ANSI.BLACK.asInstanceOf[TextColor] ,self.asInstanceOf[SELF])
      .observe( (prop,old,cur)=>{
        val rep = implicitly[RepaitRequest[SELF]]
        rep.repaitRequest(prop.owner)
      })
      ._1
}
