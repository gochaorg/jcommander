package xyz.cofe.term.ui

import xyz.cofe.lazyp.Prop
import xyz.cofe.term.common.Color
import xyz.cofe.lazyp.ReadWriteProp

trait FocusOwnerBgColor:
  val focusOwnerBgColor: ReadWriteProp[Color] = ReadWriteProp(Color.BlackBright)  
  if this.isInstanceOf[Widget] 
  then
    val wid = this.asInstanceOf[Widget]
    focusOwnerBgColor.onChange( wid.repaint )
  def focusOwnerBgColor_=( col:Color ):Unit = focusOwnerBgColor.set(col)

