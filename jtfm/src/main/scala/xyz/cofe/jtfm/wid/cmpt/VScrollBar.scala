package xyz.cofe.jtfm.wid.cmpt

import xyz.cofe.jtfm.wid.Widget
import xyz.cofe.jtfm.ev.OwnProperty
import com.googlecode.lanterna.graphics.TextGraphics
import xyz.cofe.jtfm.wid.BackgroundProperty
import xyz.cofe.jtfm.wid.ForegroundProperty
import xyz.cofe.jtfm.wid.OpaqueProperty
import xyz.cofe.jtfm.wid._
import xyz.cofe.jtfm.gr.Symbols

/** Вертикальный скролл-бар */
class VScrollBar 
  extends Widget[VScrollBar]
    with BackgroundProperty[VScrollBar]
    with ForegroundProperty[VScrollBar]
    with OpaqueProperty[VScrollBar]
{
  private val arrowUp   = "\u2bc5"
  private val arrowDown = "\u2bc6"
  private val marker    = "\u25c6"

  val value:OwnProperty[Double,VScrollBar] = new OwnProperty(0,this)
  value.listen{(_,_,_) => { this.repaint() }}

  override def render( gr:TextGraphics ):Unit = {
    this.renderOpaque(gr)

    gr.setForegroundColor(foreground)
    gr.setBackgroundColor(background)

    gr.putString(0,0,arrowUp)
    gr.putString(0,rect.height-1,arrowDown)

    val free = rect.height - 2
    if( free==1 ){
      gr.putString(0,1,marker)
    }else if( free>1 ){
      (0 until free).foreach { y =>
        val s = Symbols.SingleThin.vert + ""
        gr.putString(0,1+y,s)
      }

      val vActual = (value.value max 0.0) min 1.0
      val y = ((free-1) * vActual).toInt
      gr.putString(0,1+y,marker)
    }
  }
}
