package xyz.cofe.jtfm.wid.cmpt

import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.graphics.TextGraphics
import xyz.cofe.jtfm.ev.OwnProperty
import xyz.cofe.jtfm.gr.Rect
import xyz.cofe.jtfm.wid.{BackgroundProperty, ForegroundProperty, OpaqueProperty, Widget}
import xyz.cofe.jtfm.wid.FocusProperty
import com.googlecode.lanterna.input.MouseAction
import com.googlecode.lanterna.input.KeyStroke

/**
 * Главное меню,
 * расположается в верхней части
 */
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
  
  background.value = TextColor.ANSI.CYAN
  foreground.value = TextColor.ANSI.WHITE_BRIGHT

  // Прикрепляет себя (menuBar) к верхней части родительского контейнера  
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

  // Компоновка: расстанавливает дочерние пункты меню, скрывает остальные элементы
  private def layoutItems():Unit=
    var x = 0    
    nested.foreach { w =>
      w match {
        case mi: MenuItem[_] =>
          w.visible.value = true
          w.rect.value = Rect(x,0).size(mi.renderableWidth,1)
          x += w.rect.value.width + 1
          mi.nested.foreach { _.visible.value=false }
        case _ =>
          w.visible.value = false
      }
    }
  
  nested.listen( (lst,idx,old,cur)=>{
    layoutItems()
  })

  private var lastFocused:Option[Widget[_]] = None
  private def firstMenu:Option[MenuItem[_]] = nested.filter(_.isInstanceOf[MenuItem[_]]).map(_.asInstanceOf[MenuItem[_]]).headOption

  focus.onGain { lastFocusOwn => 
    lastFocused = lastFocusOwn
    layoutItems()
    firstMenu.foreach(_.focus.request())
  }

  /** 
   * internal
   * запоминает куда возвращать фокус после выбора пункта меню
   */
  def acceptFocusFrom( w:Widget[_] ):Unit = {
    lastFocused = Some(w)
  }

  /**
   * internal
   * восстанавливает первоначальный вид, возвращает фокус ввода на ранее сфокусированный элемент вне меню
   */
  def restoreInitialUI():Unit = {
    layoutItems()
    lastFocused match {
      case None =>
      case Some(w) => w match {
        case fp:FocusProperty[_] =>
          fp.focus.request()
        case _ =>
      }
    }
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
