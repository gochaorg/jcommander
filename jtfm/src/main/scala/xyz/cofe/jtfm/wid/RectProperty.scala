package xyz.cofe.jtfm.wid

import xyz.cofe.jtfm.ev.OwnProperty
import xyz.cofe.jtfm.gr.Rect

trait RectProperty[SELF] {
  self: Widget[_] =>
  lazy val rect:OwnProperty[Rect,SELF] = OwnProperty[Rect,SELF](Rect(0,0,1,1),self.asInstanceOf[SELF])
}
