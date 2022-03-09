package xyz.cofe.jtfm.wid.wc

import com.googlecode.lanterna.screen.Screen
import xyz.cofe.jtfm.Navigate
import xyz.cofe.jtfm.gr.Point
import xyz.cofe.jtfm.wid.Widget

class WidgetTreeRender[W <: Widget[_]]
(
  root: W,
  screen: Screen
) (
  implicit 
    navigate: Navigate[W],
) {
  def toLocal(wid:W, p:Point):Point =
    wid.widgetPath
      .map( w => w.rect.value.leftTop )
      .foldLeft(p)((a,b)=>{a.translate(b)})
 
  def apply():Unit = {
    val g = screen.newTextGraphics
    navigate.forwardIterator(root).foreach { w => 
      val lt = toLocal(w,w.rect.value.leftTop)
      val loc_g = g.newTextGraphics(lt, w.rect.value.size)
      w.render(loc_g)
    }
  }
}
