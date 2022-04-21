package xyz.cofe.jtfm.wid.cmpt

import xyz.cofe.jtfm.wid.{BackgroundProperty, ForegroundProperty, TextProperty, Widget}
import com.googlecode.lanterna.graphics.TextGraphics
import xyz.cofe.jtfm.wid.FocusProperty
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.input.MouseAction
import com.googlecode.lanterna.input.KeyType
import xyz.cofe.jtfm.gr.Rect
import xyz.cofe.jtfm.gr.Point
import xyz.cofe.jtfm.gr.HVLine
import xyz.cofe.jtfm.gr.HVLineOps
import com.googlecode.lanterna.TextColor
import xyz.cofe.jtfm.ev.EvalProperty
import xyz.cofe.jtfm.wid.wc.FocusManager
import xyz.cofe.jtfm.wid.WidgetCycle

/**
 * Меню контейнер, содержит вложенные пункты меню
 */
class MenuContainer
  extends Widget[MenuContainer]
  with FocusProperty[MenuContainer](true)
  with TextProperty[MenuContainer]
  with MenuItem[MenuContainer]
{
  menuItemInit()

  private val renderColors:EvalProperty[ (TextColor,TextColor,TextColor,TextColor), MenuContainer ] = 
    EvalProperty( ()=>{
      menuBar.map( mbar => 
        if( focus.value ){
            (mbar.focusForeground.value, mbar.focusBackground.value,
            mbar.activeForeground.value, mbar.activeBackground.value
            )
        }else if( mbar.focus.contains ){
          (mbar.activeForeground.value, mbar.activeBackground.value,
            mbar.activeForeground.value, mbar.activeBackground.value
          )
        }else{
          (mbar.foreground.value, mbar.background.value,
            mbar.foreground.value, mbar.background.value
          )
        }
      ).getOrElse( (TextColor.ANSI.WHITE,TextColor.ANSI.BLACK,TextColor.ANSI.WHITE,TextColor.ANSI.BLACK) )
    })

  WidgetCycle.jobs.foreach { jbs => 
    jbs.add( ()=>{
      FocusManager.tryGet.foreach { fm => {
          //println("FM inst")
          fm.onChange( (_) => {
            //println("recompute menu colors")
            renderColors.recompute() 
          })
        }
      }
    })
  }

  override def render(gr:TextGraphics):Unit = {
    gr.setForegroundColor(renderColors.value._1)
    gr.setBackgroundColor(renderColors.value._2)
    
    gr.putString(0,0,text.value)
  }

  private val border:Border = new Border()
  nested.append( border )
  border.visible.value = false

  renderColors.listen( (_,_,cur) => {
    border.foreground.value = cur._3
    border.background.value = cur._4
  })

  private def nestedMenuItems = nested.filter { _.isInstanceOf[MenuItem[_]] }.map { _.asInstanceOf[MenuItem[_]] }

  /** Рамка в пределах которой расположены пункты меню */
  private var nestedItemsRect:Option[Rect] = None

  /** Рамка в пределах которой расположены пункты меню */
  def nestedItemsBound:Option[Rect] = nestedItemsRect

  /**
   * При получении фокуса, 
   * раставляет вложенные пункты меню
   */
  private def doLayout( f_mi:MenuItem[_]=>Unit=_=>() ):Unit = {
    var x=1
    var y=2

    val (_mitems, others) = nested.partition { _.isInstanceOf[MenuItem[_]] }
    others.foreach { _.visible.value = false }
    
    val mitems = _mitems.map { _.asInstanceOf[MenuItem[_]] }
    val maxWidth = mitems.map { _.renderableWidth }.maxOption.getOrElse { 0 }

    nestedMenuItems.foldLeft( y )( (_y,mi) =>
      mi.rect.value = Rect(x,_y).size(maxWidth,1)
      mi.visible.value = true
      f_mi(mi)
      _y + 1
    )

    nestedItemsRect = if( nestedMenuItems.isEmpty ) None else {
      Some( nestedMenuItems.map( _.rect.value ).foldLeft( nestedMenuItems.head.rect.value )( (a,b) => {
        Rect( a.left min b.left, a.top min b.top ).to( a.right max b.right, a.bottom max b.bottom )
      }))
    }

    nestedItemsRect.foreach { rect => 
      val lt = rect.leftTop.translate(-1,-1)
      val rb = rect.rightBottom.translate(1,1)
      border.rect.value = Rect(lt,rb)
    }
  }

  focus.onGain { _ =>
    doLayout()
    border.visible.value = nestedItemsRect.isDefined
  }
  focus.onLost { _ =>
    if( !focus.contains ){
      nested.foreach { _.visible.value = false }
      border.visible.value = false
    }
  }

  override def input(ks:KeyStroke):Boolean = {
    ks match {
      case ma:MouseAction =>
        true
      case _ =>
        MenuKey.what(ks) match {
          case None => 
            false
          case Some(k_a) => k_a match {
            case MenuKey.Next => switchNextMenu()
            case MenuKey.Prev => switchPrevMenu()
            case MenuKey.GoSub => switchSubMenu()
            case MenuKey.Esc => menuBar.flatMap { 
              x => x.restoreInitialUI(); 
              Some(true) 
            }.getOrElse( false )
            case _ => 
              false
          }
        }
    }
  }
  
  /**
   * Переходит во вложенное меню
   */
  protected  def switchSubMenu():Boolean = {
    val mi = nestedMenuItems.headOption
    mi.map { x => x.focus.request(); true }.getOrElse( false )
  }

  override def toString():String = {
    s"MenuContainer ${text.value} ${rect.value}"
  }
}
