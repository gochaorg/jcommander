package xyz.cofe.term.ui.table

import xyz.cofe.term.ui.PaintStack
import xyz.cofe.term.ui.SizeProp

import TableGrid._
import xyz.cofe.term.ui.add
import xyz.cofe.term.paint.PaintCtx
import xyz.cofe.term.paint.TextBlock
import xyz.cofe.term.ui.PaintTextColor
import xyz.cofe.term.buff.ScreenChar
import xyz.cofe.term.ui.FillBackgroundColor
import xyz.cofe.term.paint._

trait TableGridPaint[A] 
extends TableGrid[A]
with FillBackgroundColor
with PaintTextColor
with PaintStack:
  paintStack.add(paintTableGrid)
  paintStack.add(paintHeader)

  def paintTableGrid(paint:PaintCtx):Unit =
    val (lines, rects) = renderDelims.get
      .partitionMap {
        case RenderDelim.RenderLine(line) => Left(line) 
        case RenderDelim.Whitespace(rect) => Right(rect)
      }

    paint.foreground = paintTextColor
    paint.background = fillBackgroundColor

    rects.map { r => (r.leftTop, TextBlock.fill(r.size,ScreenChar(' ',paintTextColor, fillBackgroundColor)) ) }
      .foreach { case (at, textBlock) => paint.write(at,textBlock) }

    lines.draw(paint)
    
  def paintHeader(paint:PaintCtx):Unit =
    paint.foreground = paintTextColor
    paint.background = fillBackgroundColor
    headersBlocks.get.map { hb => 
      val pctx = paint.context
        .offset(hb.rect.leftTop)
        .size(hb.rect.size)
        .clipping(true)
        .build

      pctx.write(0,0, hb.col.id)
    }