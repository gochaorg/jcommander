package xyz.cofe.jtfm.wid.cmpt

import xyz.cofe.jtfm.wid.Widget
import xyz.cofe.jtfm.wid.BackgroundProperty
import xyz.cofe.jtfm.wid.ForegroundProperty
import xyz.cofe.jtfm.wid.OpaqueProperty
import com.googlecode.lanterna.graphics.TextGraphics

class Panel 
  extends Widget[Panel]
    with BackgroundProperty[Panel]
    with ForegroundProperty[Panel]
    with OpaqueProperty[Panel]
{
  override def render( gr:TextGraphics ):Unit = {
    this.renderOpaque(gr)
  }
}
