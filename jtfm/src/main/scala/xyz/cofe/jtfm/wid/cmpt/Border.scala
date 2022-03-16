package xyz.cofe.jtfm.wid.cmpt

import com.googlecode.lanterna.graphics.TextGraphics
import com.googlecode.lanterna.input.{KeyStroke, KeyType}
import xyz.cofe.jtfm.ev.OwnProperty
import xyz.cofe.jtfm.gr.Rect
import xyz.cofe.jtfm.wid.{BackgroundProperty, FocusProperty, ForegroundProperty, OpaqueProperty, Widget}
import xyz.cofe.jtfm.gr.Symbols.{DoubleThin, SingleThin, Border as BorderSym}

class Border
  extends Widget[Border]
    with BackgroundProperty[Border]
    with ForegroundProperty[Border]
    with OpaqueProperty[Border]
    with FocusProperty[Border](true)
{
  private def bsym : BorderSym = focus.value match {
    case false => SingleThin
    case true => DoubleThin
  }
  
  override def input(ev: KeyStroke): Boolean = {
    val res = !List(KeyType.Tab, KeyType.ReverseTab).contains(ev.getKeyType)
    //println(s"Border.input($ev) = $res")
    res
  }
  
  override def render( gr:TextGraphics ):Unit = {
    this.renderOpaque(gr)
    
    val bsym = this.bsym
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
