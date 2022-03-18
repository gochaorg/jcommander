package xyz.cofe.jtfm.wid.cmpt

import xyz.cofe.jtfm.wid.{BackgroundProperty, ForegroundProperty, TextProperty, Widget}
import com.googlecode.lanterna.graphics.TextGraphics
import xyz.cofe.jtfm.wid.FocusProperty
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.input.MouseAction

class MenuContainer
  extends Widget[MenuContainer]
  with FocusProperty[MenuContainer](true)
  with TextProperty[MenuContainer]
  with MenuItem[MenuContainer]
{
  override def input(ks:KeyStroke):Boolean = {
    ks match {
      case ma:MouseAction =>
        true
      case _ =>
        false
    }
  }

  override def render(gr:TextGraphics):Unit = {
    menuBar.foreach { mbar =>
      val (fg,bg) = if( focus.value ){
          (mbar.focusForeground.value, mbar.focusBackground.value)
        }else if( mbar.focus.contains ){        
          (mbar.activeForeground.value, mbar.activeBackground.value)
        }else{
          (mbar.foreground.value, mbar.background.value)
        }

      //println(s"render mc ${text.value} at ${rect.value}")
      gr.setForegroundColor(fg)
      gr.setBackgroundColor(bg)
      gr.putString(0,0,text.value)
    }
  }
}
