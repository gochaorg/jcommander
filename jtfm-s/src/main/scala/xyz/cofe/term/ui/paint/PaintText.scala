package xyz.cofe.term.ui

import xyz.cofe.term.common.Color
import xyz.cofe.term.paint.PaintCtx

trait PaintText extends PaintStack with TextProperty with PaintTextColor:
  paintStack.add(paintText)

  def paintText( paint:PaintCtx ):Unit =
    paint.foreground = paintTextColor

    if this.isInstanceOf[FillBackground] 
    then 
      paint.background = 
        this.asInstanceOf[FillBackground].fillBackgroundColor

    paint.write(0,0,text.get)