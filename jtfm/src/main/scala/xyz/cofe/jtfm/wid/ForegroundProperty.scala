package xyz.cofe.jtfm.wid

import com.googlecode.lanterna.TextColor
import xyz.cofe.jtfm.ev.OwnProperty

/** Свойство: цвет текста */
trait ForegroundProperty[SELF : RepaitRequest] {
  self: Widget[_] =>
  
  /** Свойство: цвет текста */
  lazy val foreground:OwnProperty[TextColor,SELF] =
    OwnProperty( TextColor.ANSI.WHITE_BRIGHT.asInstanceOf[TextColor] ,self.asInstanceOf[SELF])
      .observe( (prop,old,cur)=>{
        val rep = implicitly[RepaitRequest[SELF]]
        rep.repaitRequest(prop.owner)
      })
      ._1
}
