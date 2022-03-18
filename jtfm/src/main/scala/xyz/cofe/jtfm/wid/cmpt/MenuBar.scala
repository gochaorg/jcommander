package xyz.cofe.jtfm.wid.cmpt

import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.graphics.TextGraphics
import xyz.cofe.jtfm.ev.OwnProperty
import xyz.cofe.jtfm.gr.Rect
import xyz.cofe.jtfm.wid.{BackgroundProperty, ForegroundProperty, OpaqueProperty, Widget}
import xyz.cofe.jtfm.wid.FocusProperty
import com.googlecode.lanterna.input.MouseAction
import com.googlecode.lanterna.input.KeyStroke

class MenuBar
  extends Widget[MenuBar]
  with BackgroundProperty[MenuBar]
  with ForegroundProperty[MenuBar]
  with FocusProperty[MenuBar](true)
  with OpaqueProperty[MenuBar]
{
  lazy val activeForeground: OwnProperty[TextColor,MenuBar] = OwnProperty[TextColor,MenuBar](TextColor.ANSI.WHITE_BRIGHT, this)
  lazy val activeBackground: OwnProperty[TextColor,MenuBar] = OwnProperty[TextColor,MenuBar](TextColor.ANSI.GREEN, this)
  lazy val focusForeground: OwnProperty[TextColor,MenuBar] = OwnProperty[TextColor,MenuBar](TextColor.ANSI.WHITE_BRIGHT, this)
  lazy val focusBackground: OwnProperty[TextColor,MenuBar] = OwnProperty[TextColor,MenuBar](TextColor.ANSI.BLACK, this)
  
  background.value = TextColor.ANSI.BLACK_BRIGHT
  foreground.value = TextColor.ANSI.WHITE_BRIGHT
  
  parent.listen( (prop,old,cur)=>{
    cur match {
      case None =>
      case Some(w) =>
        rect.value = Rect(0,0).size(w.rect.value.width, 1)
        w.rect.listen( (prop1,old,cur)=>{
          rect.value = Rect(0,0).size(w.rect.value.width, 1)
        })
    }
  })

  private def layoutItems():Unit=
    println("layout!")
    var x = 0    
    nested.foreach { w =>
      w match {
        case mi: MenuItem[_] =>
          w.visible.value = true
          w.rect.value = Rect(x,0).size(mi.text.value.length,1)
          x += w.rect.value.width + 1
          println(s"layout mi ${mi.text.value} to ${w.rect.value}")
        case _ =>
          w.visible.value = false
          println(s"layout skip mi ${w}")
      }
    }
  
  nested.listen( (lst,idx,old,cur)=>{
    println("layout trigger")
    layoutItems()
  })

  private var lastFocused:Option[Widget[_]] = None
  focus.onGain { lastFocusOwn => 
    lastFocused = lastFocusOwn
    println( s"MenuBar accept focus from ${lastFocusOwn}")
    layoutItems()
  }

  override def render(gr: TextGraphics): Unit = {
    val bg = if( focus.contains ) activeBackground.value else background.value
    gr.setBackgroundColor(bg)
    for( x<-(0 until rect.width); y<-(0 until rect.height))gr.setCharacter(x,y,' ')
  }

  override def input(ks:KeyStroke):Boolean = {
    ks match {
      case ma:MouseAction =>
        true
      case _ =>
        false
    }
  }
}
