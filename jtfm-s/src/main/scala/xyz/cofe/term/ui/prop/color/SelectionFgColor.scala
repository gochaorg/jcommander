package xyz.cofe.term.ui.prop.color

import xyz.cofe.lazyp.ReadWriteProp
import xyz.cofe.term.common.Color
import xyz.cofe.term.ui.Widget

trait SelectionFgColor:
  val selectionFgColor: ReadWriteProp[Color] = ReadWriteProp(Color.RedBright)
  if this.isInstanceOf[Widget] 
  then
    val wid = this.asInstanceOf[Widget]
    selectionFgColor.onChange( wid.repaint )
  def selectionFgColor_=( col:Color ):Unit = selectionFgColor.set(col)

