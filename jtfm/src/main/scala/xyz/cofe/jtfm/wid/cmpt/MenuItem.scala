package xyz.cofe.jtfm.wid.cmpt

import xyz.cofe.jtfm.wid.Widget
import xyz.cofe.jtfm.wid.TextProperty
import com.googlecode.lanterna.graphics.TextGraphics
import xyz.cofe.jtfm.wid.WidgetCycle
import xyz.cofe.jtfm.wid.Widget.likeTree
import xyz.cofe.jtfm.tree._
import xyz.cofe.jtfm.wid.FocusProperty
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.input.KeyType

trait MenuItem[SELF <: Widget[SELF]] 
  extends Widget[SELF]
  with TextProperty[SELF]
  with FocusProperty[SELF]
{
  def menuBar:Option[MenuBar] = widgetPath.reverse.find( _.isInstanceOf[MenuBar] ).map( _.asInstanceOf[MenuBar] )
    
  def nextMenu:Option[MenuContainer|MenuAction] = {
    val me:Widget[SELF] = this.asInstanceOf[Widget[SELF]]
    LikeTreeOps(me)(Widget.likeTree).siblings.find { _ match {
      case _:MenuAction => true
      case _:MenuContainer => true
      case _ => false
    }}.map( _.asInstanceOf[MenuContainer|MenuAction] )
  }

  def prevMenu:Option[MenuContainer|MenuAction] = {
    val me:Widget[SELF] = this.asInstanceOf[Widget[SELF]]
    LikeTreeOps(me)(Widget.likeTree).siblings.reverse.find { _ match {
      case _:MenuAction => true
      case _:MenuContainer => true
      case _ => false
    }}.map( _.asInstanceOf[MenuContainer|MenuAction] )
  }

  protected def nestedMenuLevel:Int = widgetPath.reverse.filter( it => { 
    it match {
      case _:MenuItem[_] => true
      case _:MenuBar => true
      case _ => false
    }
  }).takeWhile( it => !(it.isInstanceOf[MenuBar]) ).size

  enum MenuKey {
    case Next, Prev, GoSub, GoUp, Esc    
  }

  object MenuKey {
    def what(ks:KeyStroke):Option[MenuKey] = {
      val lvl = nestedMenuLevel-1
      ks.getKeyType match {
        case KeyType.ArrowRight if lvl==0 => Some(Next)
        case KeyType.ArrowLeft  if lvl==0 => Some(Prev)
        case KeyType.ArrowDown  if lvl==0 => Some(GoSub)
        case KeyType.ArrowDown  if lvl>0 => Some(Next)
        case KeyType.ArrowUp    if lvl>0 => Some(Prev)
        case KeyType.ArrowRight if lvl>0 => Some(GoSub)
        case KeyType.Escape => Some(Esc)
        case KeyType.Enter => Some(GoSub)
        case _:AnyRef => None
      }
    }
  }

  protected def switchNextMenu():Boolean = {
    nextMenu match {
      case Some(nm) => nm.focus.request { _ =>
          nested.foreach { _.visible.value = false }
        }
        true
      case None => false
    }
  }
  
  protected def switchPrevMenu():Boolean = {
    prevMenu match {
      case Some(nm) => nm.focus.request { _ =>
          nested.foreach { _.visible.value = false }
        }
        true
      case None => false
    }
  }

  protected def menuItemInit():Unit = {
    focus.onGain(from => {
      from.foreach { wfrom => 
        menuBar.foreach { mb =>
          if( !wfrom.widgetPath.contains(mb) ){
            mb.acceptFocusFrom( wfrom )
          }
        }
      }
    })
  }
}
