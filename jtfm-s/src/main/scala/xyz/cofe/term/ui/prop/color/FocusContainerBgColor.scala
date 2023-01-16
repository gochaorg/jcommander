package xyz.cofe.term.ui

import xyz.cofe.lazyp.Prop
import xyz.cofe.term.common.Color
import xyz.cofe.lazyp.ReadWriteProp

trait FocusContainerBgColor:
  val focusContainerBgColor: ReadWriteProp[Color] = ReadWriteProp(Color.BlackBright)  
  if this.isInstanceOf[Widget] 
  then
    val wid = this.asInstanceOf[Widget]
    focusContainerBgColor.onChange( wid.repaint )
  def focusContainerBgColor_=( col:Color ):Unit = focusContainerBgColor.set(col)

