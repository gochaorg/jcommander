package xyz.cofe.term.ui
package prop.color

import xyz.cofe.lazyp.Prop
import xyz.cofe.term.common.Color
import xyz.cofe.lazyp.ReadWriteProp

trait BackgroundColor:
  val backgroundColor: ReadWriteProp[Color] = ReadWriteProp(Color.Black)  
  if this.isInstanceOf[Widget] 
  then
    val wid = this.asInstanceOf[Widget]
    backgroundColor.onChange( wid.repaint )
  def backgroundColor_=( col:Color ):Unit = backgroundColor.set(col)

