package xyz.cofe.jtfm.wid.cmpt

import com.googlecode.lanterna.graphics.TextGraphics
import xyz.cofe.jtfm.ev.OwnProperty
import xyz.cofe.jtfm.gr.Rect
import xyz.cofe.jtfm.wid.{BackgroundProperty, ForegroundProperty, OpaqueProperty, Widget}
import xyz.cofe.jtfm.gr.Symbols.{DoubleThin, SingleThin, Border as BorderSym}

class Border
  extends Widget[Border]
    with BackgroundProperty[Border]
    with ForegroundProperty[Border]
    with OpaqueProperty[Border]
{
  private val bsym : BorderSym = SingleThin
  
  override def render( gr:TextGraphics ):Unit = {
    this.renderOpaque(gr)

    val r = this.rect.value
    if( r.width>0 && r.height>0 ){
      gr.setBackgroundColor(background.value)
      gr.setForegroundColor(foreground.value)
      
      (0 until r.height).foreach { y =>
        gr.setCharacter(0, y, bsym.vert)
        gr.setCharacter(r.width-1, y, bsym.vert)
      }
      (0 until r.width).foreach { x =>
        gr.setCharacter(x, 0, bsym.horz)
        gr.setCharacter(x, r.height-1, bsym.horz)
      }
      gr.setCharacter(0, 0, bsym.leftTop)
      gr.setCharacter(r.width-1, 0, bsym.rightTop)
      gr.setCharacter(0, r.height-1, bsym.leftBottom)
      gr.setCharacter(r.width-1, r.height-1, bsym.rightBottom)
    }
  }
}
