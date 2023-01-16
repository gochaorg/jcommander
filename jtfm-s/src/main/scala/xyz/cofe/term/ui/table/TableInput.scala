package xyz.cofe.term.ui.table

import xyz.cofe.term.ui.WidgetInput
import xyz.cofe.term.common.InputEvent
import xyz.cofe.term.common.InputMouseButtonEvent

trait TableInput[A]
extends WidgetInput
with TableRowsProp[A]
with TableGridProp[A]
:
  override def input(inputEvent: InputEvent): Boolean = 
    inputEvent match
      case me:InputMouseButtonEvent =>
        println(s"mouse ${me.position()} ${me.button()} ${me.pressed()}")
        
    false
