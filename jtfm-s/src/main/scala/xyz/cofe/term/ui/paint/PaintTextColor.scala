package xyz.cofe.term.ui
package paint

import xyz.cofe.term.common.Color
import xyz.cofe.term.ui.prop.color._

trait PaintTextColor extends ForegroundColor:
  def paintTextColor:Color =
    if this.isInstanceOf[WidgetInput]
    then
      val foc = this.asInstanceOf[WidgetInput].focus
      if this.isInstanceOf[FocusOwnerFgColor] && foc.isOwner 
      then this.asInstanceOf[FocusOwnerFgColor].focusOwnerFgColor.get 
      else 
        if this.isInstanceOf[FocusContainerFgColor] && foc.contains 
        then this.asInstanceOf[FocusContainerFgColor].focusContainerFgColor.get 
        else foregroundColor.get
    else
      foregroundColor.get

