package xyz.cofe.jtfm.wid.cmpt

import com.googlecode.lanterna.graphics.TextGraphics
import xyz.cofe.jtfm.ev.OwnProperty
import xyz.cofe.jtfm.wid.{BackgroundProperty, ForegroundProperty, OpaqueProperty, TextProperty, Widget}

class Label
  extends Widget[Label]
    with TextProperty[Label]
    with ForegroundProperty[Label]
    with BackgroundProperty[Label]
    with OpaqueProperty[Label]
{
  override def render( gr:TextGraphics ):Unit = {
    this.renderOpaque(gr)
    
    gr.setBackgroundColor(background.value)
    gr.setForegroundColor(foreground.value)
    gr.putString(0,0,text.value)
  }
}
