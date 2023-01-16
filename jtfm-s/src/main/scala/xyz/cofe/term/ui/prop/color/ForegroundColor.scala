package xyz.cofe.term.ui
package prop.color

import xyz.cofe.lazyp.Prop
import xyz.cofe.term.common.Color
import xyz.cofe.lazyp.ReadWriteProp
import xyz.cofe.term.ui.Widget

trait ForegroundColor:
  val foregroundColor: ReadWriteProp[Color] = ReadWriteProp(Color.White)
  if this.isInstanceOf[Widget] 
  then
    val wid = this.asInstanceOf[Widget]
    foregroundColor.onChange( wid.repaint )
  def foregroundColor_=( col:Color ):Unit = foregroundColor.set(col)

