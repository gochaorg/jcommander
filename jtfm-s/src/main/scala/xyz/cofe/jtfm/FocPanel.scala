package xyz.cofe.jtfm

import xyz.cofe.term.ui.Panel
import xyz.cofe.term.ui.WidgetInput
import xyz.cofe.term.ui._
import xyz.cofe.term.ui.prop._
import xyz.cofe.term.common.InputEvent
import xyz.cofe.term.common.InputMouseButtonEvent

class FocPanel(name:String) extends Panel with WidgetInput with VisibleProp:
  
  this.paintStack.add { pctx => 
    pctx.write(0,0, s"contains=${focus.contains} owner=${focus.isOwner}" )
  }

  override def toString(): String = s"FocPanel($name)"

  override def input(inputEvent: InputEvent): Boolean = {    
    println(s"FocPanel($name) " + { 
      inputEvent match
        case me: InputMouseButtonEvent =>
          s"InputMouseButtonEvent(pos=${me.position()}, button=${me.button()}, pressed=${me.pressed()})"
        case _ => inputEvent.getClass().toString()
    })
    false
  }