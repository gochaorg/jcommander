package xyz.cofe.term.ui

import xyz.cofe.term.common.Color
import xyz.cofe.term.paint.PaintCtx

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

trait PaintText extends PaintStack with TextProperty with PaintTextColor:
  paintStack.add(paintText)

  def paintText( paint:PaintCtx ):Unit =
    paint.foreground = paintTextColor

    if this.isInstanceOf[FillBackground] 
    then 
      paint.background = 
        this.asInstanceOf[FillBackground].fillBackgroundColor

    paint.write(0,0,text.get)