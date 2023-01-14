package xyz.cofe.term.ui

import xyz.cofe.term.common.Color
import xyz.cofe.term.paint.PaintCtx
import xyz.cofe.term.buff.ScreenChar
import xyz.cofe.term.geom._

trait FillBackground extends PaintStack with FillBackgroundColor:
  paintStack.add(fillBackground)

  def fillBackground(paint:PaintCtx):Unit =    
    paint.background = fillBackgroundColor
    paint.fill( paint.bounds.size.leftUpRect(0,0) )
