package xyz.cofe.jtfm.wid.cmpt

import xyz.cofe.jtfm.wid.{BackgroundProperty, ForegroundProperty, TextProperty, Widget}
import com.googlecode.lanterna.graphics.TextGraphics
import xyz.cofe.jtfm.wid.FocusProperty
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.input.MouseAction
import com.googlecode.lanterna.input.KeyType
import xyz.cofe.jtfm.gr.Rect

class MenuContainer
  extends Widget[MenuContainer]
  with FocusProperty[MenuContainer](true)
  with TextProperty[MenuContainer]
  with MenuItem[MenuContainer]
{
  menuItemInit()

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

  private def nestedMenuItems = nested.filter { _.isInstanceOf[MenuItem[_]] }.map { _.asInstanceOf[MenuItem[_]] }

  private def doLayout( f_mi:MenuItem[_]=>Unit=_=>() ):Unit = {
    var x=0
    var y=1
    val (_mitems, others) = nested.partition { _.isInstanceOf[MenuItem[_]] }
    others.foreach { _.visible.value = false }
    val mitems = _mitems.map { _.asInstanceOf[MenuItem[_]] }

    val maxWidth = mitems.map { _.text.value.length }.maxOption.getOrElse { 0 }

    nestedMenuItems.foldLeft( y )( (_y,mi) =>
      mi.rect.value = Rect(x,_y).size(maxWidth,1)
      mi.visible.value = true
      f_mi(mi)
      _y + 1
    )
  }

  focus.onGain { _ =>
    doLayout()
  }
  focus.onLost { _ =>
    if( !focus.contains ){
      nested.foreach { _.visible.value = false }
    }
  }

  override def input(ks:KeyStroke):Boolean = {
    ks match {
      case ma:MouseAction =>
        true
      case _ =>
        val lvl = nestedMenuLevel-1
        println(s"mc (${text.value}) input $lvl")
        ks.getKeyType match {
          case KeyType.ArrowRight if lvl==0 => switchNext()
          case KeyType.ArrowLeft  if lvl==0 => switchPrev()
          case KeyType.ArrowDown  if lvl==0 => switchSubMenu()
          case KeyType.ArrowDown  if lvl>0 => switchNext()
          case KeyType.ArrowUp    if lvl>0 => switchPrev()
          case KeyType.ArrowRight if lvl>0 => switchSubMenu()
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
  private def switchSubMenu():Boolean = {
    val mi = nestedMenuItems.headOption
    mi.map { x => x.focus.request(); true }.getOrElse( false )
  }

  override def toString():String = {
    s"MenuContainer ${text.value} ${rect.value}"
  }
}
