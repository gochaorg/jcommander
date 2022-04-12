package xyz.cofe.jtfm.wid.cmpt

import xyz.cofe.jtfm.wid.{BackgroundProperty, ForegroundProperty, TextProperty, Widget}
import com.googlecode.lanterna.graphics.TextGraphics
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.input.KeyType
import com.googlecode.lanterna.input.MouseAction

class MenuAction( val action:(MenuAction,KeyStroke)=>Unit )
  extends Widget[MenuAction]
  with TextProperty[MenuAction]
  with MenuItem[MenuAction]
{
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
      gr.putString(0,0,text.value)
    }
  }

  override def input(ks:KeyStroke):Boolean = {
    ks match {
      case ma:MouseAction =>
        ma.getButton() match {
          case 1 if ma.isMouseDown =>
            action(this,ma)
            menuBar.foreach { x => x.restoreInitialUI() }
            true
          case _ =>
        }
        false
      case _ =>
        MenuKey.what(ks) match {
          case None => false
          case Some(_x) => _x match {
            case MenuKey.GoSub =>
              action(this,ks)
              menuBar.foreach { x => x.restoreInitialUI() }
              true
            case MenuKey.Next => switchNextMenu()
            case MenuKey.Prev => switchPrevMenu()
            case MenuKey.Esc => menuBar.flatMap { x => x.restoreInitialUI(); Some(true) }.getOrElse( false )
            case _ =>
              false
          }
        }
    }
  }
}
