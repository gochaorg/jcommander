package xyz.cofe.term.ui.prop.color

import xyz.cofe.lazyp.ReadWriteProp
import xyz.cofe.term.common.Color
import xyz.cofe.term.ui.Widget

trait SelectionBgColor:
  val selectionBgColor: ReadWriteProp[Color] = ReadWriteProp(Color.BlackBright)
  if this.isInstanceOf[Widget] 
  then
    val wid = this.asInstanceOf[Widget]
    selectionBgColor.onChange( wid.repaint )
  def selectionBgColor_=( col:Color ):Unit = selectionBgColor.set(col)

