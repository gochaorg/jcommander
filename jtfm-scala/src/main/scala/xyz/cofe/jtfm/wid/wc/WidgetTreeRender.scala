package xyz.cofe.jtfm.wid.wc

import com.googlecode.lanterna.screen.Screen
import xyz.cofe.jtfm.Navigate
import xyz.cofe.jtfm.gr.Point
import xyz.cofe.jtfm.wid.Widget
import xyz.cofe.jtfm.wid.Widget._

class WidgetTreeRender[W <: Widget[_]]
(
  root: W,
  screen: Screen
) (
  implicit 
    val navigate: Navigate[W],
) {
  var repaitRequestCount = 0
  
  def repaitRequest():WidgetTreeRender[W] = {
    repaitRequestCount += 1
    this
  }
  
  def apply():Unit = {
    if( repaitRequestCount>0 ) {
      repaitRequestCount = 0

      val g = screen.newTextGraphics
      val zeroPoint = Point(0, 0)
      navigate.forwardIterator(root).foreach { w =>
        val lt = zeroPoint.toAbsolute(w)
        //println(s"wc render ${w} at ${lt}")
        val loc_g = g.newTextGraphics(lt, w.rect.value.size)
        w.render(loc_g)
      }
    }
  }
}
