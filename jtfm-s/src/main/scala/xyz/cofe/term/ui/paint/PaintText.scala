package xyz.cofe.term.ui

import xyz.cofe.term.common.Color
import xyz.cofe.term.paint.PaintCtx


trait PaintText extends PaintStack with TextProperty with ForegroundColor:
  paintStack.set(
    paintStack.get :+ { paint =>
      paintText(paint)
    }
  )

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
  
  def paintText( paint:PaintCtx ):Unit =
    paint.foreground = paintTextColor

    if this.isInstanceOf[FillBackground] 
    then 
      paint.background = 
        this.asInstanceOf[FillBackground].fillBackgroundColor

    paint.write(0,0,text.get)