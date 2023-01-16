package xyz.cofe.term.ui

import xyz.cofe.lazyp.Prop
import xyz.cofe.term.common.Color
import xyz.cofe.lazyp.ReadWriteProp

trait FocusContainerFgColor:
  val focusContainerFgColor: ReadWriteProp[Color] = ReadWriteProp(Color.WhiteBright)
  if this.isInstanceOf[Widget] 
  then
    val wid = this.asInstanceOf[Widget]
    focusContainerFgColor.onChange( wid.repaint )
  def focusContainerFgColor_=( col:Color ):Unit = focusContainerFgColor.set(col)

