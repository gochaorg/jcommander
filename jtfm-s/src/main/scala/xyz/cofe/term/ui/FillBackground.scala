package xyz.cofe.term.ui

import xyz.cofe.term.common.Color
import xyz.cofe.term.paint.PaintCtx
import xyz.cofe.term.buff.ScreenChar

trait FillBackground extends PaintStack with BackgroundColor:
  paintStack.set(
    paintStack.get :+ { paint => 
      fillBackground(paint)
    }
  )

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

  def fillBackground(paint:PaintCtx):Unit =    
    val chr = ScreenChar(' ',Color.White, fillBackgroundColor)
    (0 until paint.bounds.size.height()).flatMap { y => 
      (0 until paint.bounds.size.width()).map { x => (x,y) }
    }.foreach { case (x,y) => 
      paint.write(x,y,chr)
    }

