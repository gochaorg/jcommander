package xyz.cofe.term.ui

import xyz.cofe.term.common.Color
import xyz.cofe.term.paint.PaintCtx
import xyz.cofe.term.buff.ScreenChar
import xyz.cofe.term.geom._

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

trait FillBackground extends PaintStack with FillBackgroundColor:
  paintStack.add(fillBackground)

  def fillBackground(paint:PaintCtx):Unit =    
    paint.background = fillBackgroundColor
    paint.fill( paint.bounds.size.leftUpRect(0,0) )
