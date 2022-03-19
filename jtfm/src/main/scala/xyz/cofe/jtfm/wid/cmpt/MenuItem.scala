package xyz.cofe.jtfm.wid.cmpt

import xyz.cofe.jtfm.wid.Widget
import xyz.cofe.jtfm.wid.TextProperty
import com.googlecode.lanterna.graphics.TextGraphics
import xyz.cofe.jtfm.wid.WidgetCycle
import xyz.cofe.jtfm.wid.Widget.likeTree
import xyz.cofe.jtfm.tree._

trait MenuItem[SELF <: Widget[SELF]] 
  extends Widget[SELF]
  with TextProperty[SELF]
{
  def menuBar:Option[MenuBar] = widgetPath.reverse.find( _.isInstanceOf[MenuBar] ).map( _.asInstanceOf[MenuBar] )
  
  // def visibleNavigator = for {
  //   wc <- WidgetCycle.tryGet
  //   ws <- wc.workState    
  // } yield ws.visibleNavigator
  
  def nextMenu:Option[MenuContainer|MenuAction] = {
    val me:Widget[_] = this.asInstanceOf[Widget[_]]        
    ???
  }
}
