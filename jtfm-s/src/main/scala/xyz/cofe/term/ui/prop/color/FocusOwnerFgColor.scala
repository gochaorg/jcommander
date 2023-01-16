package xyz.cofe.term.ui
package prop.color

import xyz.cofe.lazyp.Prop
import xyz.cofe.term.common.Color
import xyz.cofe.lazyp.ReadWriteProp

trait FocusOwnerFgColor:
  val focusOwnerFgColor: ReadWriteProp[Color] = ReadWriteProp(Color.YellowBright)
  if this.isInstanceOf[Widget] 
  then
    val wid = this.asInstanceOf[Widget]
    focusOwnerFgColor.onChange( wid.repaint )
  def focusOwnerFgColor_=( col:Color ):Unit = focusOwnerFgColor.set(col)
