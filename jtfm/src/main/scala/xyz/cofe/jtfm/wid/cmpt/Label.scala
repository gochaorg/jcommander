package xyz.cofe.jtfm.wid.cmpt

import com.googlecode.lanterna.graphics.TextGraphics
import xyz.cofe.jtfm.ev.OwnProperty
import xyz.cofe.jtfm.wid.{TextProperty, Widget}

class Label extends Widget[Label] with TextProperty[Label] {
  override def render( gr:TextGraphics ):Unit = {
    gr.putString(0,0,text.value)
  }
}
