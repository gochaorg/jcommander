package xyz.cofe.jtfm.wid.cmpt

import xyz.cofe.jtfm.wid.{BackgroundProperty, ForegroundProperty, TextProperty, Widget}
import com.googlecode.lanterna.graphics.TextGraphics
import xyz.cofe.jtfm.wid.FocusProperty
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.input.MouseAction
import com.googlecode.lanterna.input.KeyType

class MenuContainer
  extends Widget[MenuContainer]
  with FocusProperty[MenuContainer](true)
  with TextProperty[MenuContainer]
  with MenuItem[MenuContainer]
{
  menuItemInit()
  override def input(ks:KeyStroke):Boolean = {
    ks match {
      case ma:MouseAction =>
        true
      case _ =>
        ks.getKeyType match {
          case KeyType.ArrowRight => switchNext()
          case KeyType.ArrowLeft => switchPrev()
          case KeyType.Escape => menuBar.flatMap { x => x.restoreInitialUI(); Some(true) }.getOrElse( false )
          case _:AnyRef => false
        }
    }
  }

  private def switchNext():Boolean = {
    nextMenu match {
      case Some(nm) => nm.focus.request { _ =>
          nested.foreach { _.visible.value = false }
        }
        true
      case None => false
    }
  }
  private def switchPrev():Boolean = {
    prevMenu match {
      case Some(nm) => nm.focus.request { _ =>
          nested.foreach { _.visible.value = false }
        }
        true
      case None => false
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
      gr.setForegroundColor(fg)
      gr.setBackgroundColor(bg)
      println("mc "+text.value+" visible"+visible.value)
      gr.putString(0,0,text.value)
    }
  }
}
