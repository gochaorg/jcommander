package xyz.cofe.term.ui

import xyz.cofe.term.common.Color

trait FillBackgroundColor extends BackgroundColor:
  def fillBackgroundColor:Color =
    if this.isInstanceOf[WidgetInput]
    then
      val foc = this.asInstanceOf[WidgetInput].focus
      if this.isInstanceOf[FocusOwnerBgColor] && foc.isOwner 
      then this.asInstanceOf[FocusOwnerBgColor].focusOwnerBgColor.get 
      else 
        if this.isInstanceOf[FocusContainerBgColor] && foc.contains 
        then this.asInstanceOf[FocusContainerBgColor].focusContainerBgColor.get 
        else backgroundColor.get
    else
      backgroundColor.get
