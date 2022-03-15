package xyz.cofe.jtfm.wid.wc

import xyz.cofe.jtfm.Navigate
import xyz.cofe.jtfm.wid.Widget

class FocusManager[W <: Widget[_]]
(
  val root: W,
  val navigate: Navigate[W]
) {
  def focusOwner:Option[Widget[_]] = None
}
