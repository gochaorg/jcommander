package xyz.cofe.jtfm.wid.cmpt

import xyz.cofe.jtfm.wid.Widget
import xyz.cofe.jtfm.wid.TextProperty
import com.googlecode.lanterna.graphics.TextGraphics

trait MenuItem[SELF <: Widget[SELF]] 
  extends Widget[SELF]
  with TextProperty[SELF]
{
  def menuBar:Option[MenuBar] = widgetPath.reverse.find( _.isInstanceOf[MenuBar] ).map( _.asInstanceOf[MenuBar] )
}
