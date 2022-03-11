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
    gr.setBackgroundColor(background.value)
    gr.setForegroundColor(foreground.value)

    if( opaque.value ){
      (0 until rect.height).foreach { y =>
        gr.putString(0,y," ".repeat(rect.width))
      }
    }
    
    gr.putString(0,0,text.value)
  }
}
