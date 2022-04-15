package xyz.cofe.jtfm.wid.cmpt

import xyz.cofe.jtfm.wid.{BackgroundProperty, ForegroundProperty, TextProperty, Widget}
import com.googlecode.lanterna.graphics.TextGraphics
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.input.KeyType
import com.googlecode.lanterna.input.MouseAction
import xyz.cofe.jtfm.wid.Shortcut
import xyz.cofe.jtfm.wid.wc.BroadcastReciver
import xyz.cofe.jtfm.wid.Shortcut.FunShortcut
import xyz.cofe.jtfm.wid.Shortcut.ChrShortcut
import xyz.cofe.jtfm.wid.Shortcut.SeqShortcut

class MenuAction( 
  val action:(MenuAction)=>Unit,
  val shortcut:Option[Shortcut] = None
)
  extends Widget[MenuAction]
  with TextProperty[MenuAction]
  with MenuItem[MenuAction]
  with BroadcastReciver
{
  override def renderableWidth:Int = text.value.length + shortcut.map( _.toString.length+1 ).getOrElse(0)

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

      for {
        mc <- upMenu
        bnd <- mc.nestedItemsBound
      } gr.putString(0,0," ".repeat(bnd.width))
      
      gr.putString(0,0,text.value)

      for {
        sh <- shortcut
        mc <- upMenu
        bnd <- mc.nestedItemsBound
        shTxt = sh.toString
      } gr.putString( bnd.width - shTxt.length, 0, shTxt )
    }
  }

  override def input(ks:KeyStroke):Boolean = {
    ks match {
      case ma:MouseAction =>
        ma.getButton() match {
          case 1 if ma.isMouseDown =>
            action(this)
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
              action(this)
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

  def reciveBroadcast( ks:KeyStroke ):Unit = {
    shortcut match {
      case None =>
      case Some(sct) => sct match {
        case sc:FunShortcut =>
          if sc.test(ks) then
            action(this)
        case sc:ChrShortcut =>
          if sc.test(ks) then
            action(this)
        case sc:SeqShortcut =>
      }
    }
  }
}
