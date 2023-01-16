package xyz.cofe.term.ui
package prop

import xyz.cofe.lazyp.ReadWriteProp
import xyz.cofe.lazyp.Prop
import xyz.cofe.term.common.Color
import xyz.cofe.term.paint.PaintCtx

trait TextProperty extends Widget:
  val text: ReadWriteProp[String] = ReadWriteProp("text")
  text.onChange { repaint }
  def text_=( string:String ):Unit = text.set(string)

implicit def textProp2String( prop:Prop[String] ):String = prop.get

