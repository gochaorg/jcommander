package xyz.cofe.jtfm.wid

import xyz.cofe.jtfm.ev.OwnProperty

trait VisibleProperty[SELF] {
  self: Widget[_] =>
  lazy val visible:OwnProperty[Boolean,SELF] = OwnProperty(true,self.asInstanceOf[SELF])
}
