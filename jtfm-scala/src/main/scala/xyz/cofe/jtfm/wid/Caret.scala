package xyz.cofe.jtfm.wid

import xyz.cofe.jtfm.gr.Point

object Caret {
  def hide:Unit = for( wc <- WidgetCycle.tryGet; ws <- wc.workState ) {
    ws.caret = None
  }
  def show( pt:Point ) = for( wc <- WidgetCycle.tryGet; ws <- wc.workState ) {
    ws.caret = Some(pt)
  }
}
