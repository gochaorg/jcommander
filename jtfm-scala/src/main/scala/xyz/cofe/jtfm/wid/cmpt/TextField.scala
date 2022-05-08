package xyz.cofe.jtfm.wid.cmpt

import xyz.cofe.jtfm.wid.Widget
import xyz.cofe.jtfm.wid.TextProperty
import xyz.cofe.jtfm.wid.ForegroundProperty
import xyz.cofe.jtfm.wid.FocusProperty
import xyz.cofe.jtfm.wid.BackgroundProperty
import com.googlecode.lanterna.graphics.TextGraphics
import xyz.cofe.jtfm.wid.OpaqueProperty
import xyz.cofe.jtfm.wid.WidgetCycle
import xyz.cofe.jtfm.gr.Point
import com.googlecode.lanterna.input.KeyStroke
import java.awt.event.MouseEvent
import com.googlecode.lanterna.input.KeyType
import com.googlecode.lanterna.input.MouseAction
import xyz.cofe.jtfm.wid.Caret

class TextField 
  extends Widget[TextField]
    with TextProperty[TextField]
    with ForegroundProperty[TextField]
    with BackgroundProperty[TextField]
    with FocusProperty[TextField]()
    with OpaqueProperty[TextField]
{
  private var cursor0 = 0
  private def cursor:Int = cursor0
  private def cursor_=(v:Int):Unit = {
    cursor0 = v
    if focus.value then
      Caret.show( cursorPosAbs )
  }

  override def render(gr:TextGraphics):Unit = {
    this.renderOpaque(gr)
    if rect.width>0 && rect.height>0 then
      gr.setForegroundColor(foreground.value)
      gr.setBackgroundColor(background.value)
      if text.value.length<=rect.width then
        gr.putString(0,0,text.value)
      else
        val cur = ((cursor max 0) min text.value.length)
        val from = if cur>=rect.width then cur-rect.width else 0
        val to = ((from+rect.width) min text.value.length) max 0
        val frm1 = from min to
        val to1 = from max to
        val str = text.value.substring(frm1,to1)
        gr.putString(0,0,str)
  }

  private def cursorPos:Point = Point(
    ((cursor max 0) min text.value.length) min rect.width-1
    ,0)
  private def cursorPosAbs:Point = {
    import xyz.cofe.jtfm.wid.Widget.PointOps
    cursorPos.toAbsolute(this)
  }

  focus.onGain { _ =>
    Caret.show( cursorPosAbs )
  }

  focus.onLost { _ =>
    Caret.hide
  }

  private def insertChar( ch:Char ):Boolean = {
    if cursor>=text.value.length then
      text.value = text.value + ch
      cursor = text.value.length
    else
      val c = (cursor max 0) min text.value.length
      val s0 = text.value.substring(0,c)
      val s1 = text.value.substring(c)
      text.value = s0 + ch + s1
      cursor = c+1
    true
  }
  
  private def deleteLeft():Boolean = {
    if cursor>0 then
      val c = (cursor max 0) min text.value.length
      val s0 = text.value.substring(0,c-1)
      val s1 = text.value.substring(c)
      text.value = s0 + s1
      cursor = c-1
    true
  }
  private def deleteRight():Boolean = {
    if cursor<text.value.length then
      val c = (cursor max 0) min text.value.length
      val s0 = text.value.substring(0,c)
      val s1 = text.value.substring(c+1)
      text.value = s0 + s1
    else 
      text.value = text.value.substring(0, text.value.length-1)
    true
  }
  private def moveCaretLeft():Boolean = {
    cursor = (( cursor-1 ) max 0) min text.value.length
    repaint()
    true
  }
  private def moveCaretRight():Boolean = {
    cursor = (( cursor+1 ) max 0) min text.value.length
    repaint()
    true
  }
  private def moveCaretHome():Boolean = {
    cursor = 0
    repaint()
    true
  }
  private def moveCaretEnd():Boolean = {
    cursor = text.value.length
    repaint()
    true
  }

  override def input( ks:KeyStroke ):Boolean = {
    ks match {
      case ma:MouseAction => true
      case _ => ks.getKeyType match {
        case KeyType.Character if ks.getCharacter!=null =>
          insertChar(ks.getCharacter)
        case KeyType.ArrowLeft  => moveCaretLeft()
        case KeyType.ArrowRight => moveCaretRight()
        case KeyType.Delete     => deleteRight()
        case KeyType.Backspace  => deleteLeft()
        case KeyType.Home  => moveCaretHome()
        case KeyType.End  => moveCaretEnd()
        case _ => false
      }
    }
  }
}
