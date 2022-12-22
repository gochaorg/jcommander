package xyz.cofe.jtfm

import xyz.cofe.term.ui.Panel
import xyz.cofe.term.ui.WidgetInput
import xyz.cofe.term.ui._
import xyz.cofe.term.common.InputEvent

class FocPanel(name:String) extends Panel with WidgetInput with VisibleProp:
  def input(inputEvent: InputEvent): Unit = ()
  
  this.paintStack.add { pctx => 
    pctx.write(0,0, s"contains=${focus.contains} owner=${focus.isOwner}" )
  }

  override def toString(): String = s"FocPanel($name)"