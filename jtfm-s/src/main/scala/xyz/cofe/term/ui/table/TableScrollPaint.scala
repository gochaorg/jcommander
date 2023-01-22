package xyz.cofe.term.ui.table

import xyz.cofe.term.ui.paint.add
import xyz.cofe.term.ui.prop.color._
import xyz.cofe.term.paint.PaintCtx
import xyz.cofe.term.geom.Symbols

trait TableScrollPaint[A] 
extends TableGridPaint[A]:
  paintStack.add(paintScrollBar)

  def paintScrollBar(paint:PaintCtx):Unit =
    val headerSize = nonRenderHeadDataRows.get.size
    val renderSize = renderDataRows.get.size
    val tailSize = nonRenderTailDataRows.get.size
    val (dataYMin,dataYMax) = dataYPos.get

    if (headerSize!=0 || tailSize!=0) && renderSize>=5 && ((dataYMax-dataYMin)-2)>=3
    then
      renderScroll(paint,headerSize,renderSize,tailSize)

  private def renderScroll(paint:PaintCtx,headerSize:Int,renderSize:Int,tailSize:Int):Unit =
    val (dataYMin,dataYMax) = dataYPos.get
    val yBegin = dataYMin
    val yEnd   = dataYMax-1

    val sz = (yEnd-yBegin)-2
    val summSize = headerSize + renderSize + tailSize

    val hs = ((headerSize.toDouble / summSize.toDouble) * sz).toInt max (if headerSize>0 then 1 else 0)
    val ts = ((tailSize.toDouble   / summSize.toDouble) * sz).toInt max (if tailSize>0 then 1 else 0)
    val rs = (sz-hs-ts)

    val (yShade0,yShade1) = (yBegin+1,   yEnd-1)
    val (yBlock0,yBlock1) = (yShade0+hs, yShade0+hs+rs)

    println( s"dataYMin=$dataYMin dataYMax=$dataYMax sz=$sz" )
    println( s"headerSize=$headerSize renderSize=$renderSize tailSize=$tailSize summSize=$summSize" )
    println( s"yShade0=$yShade0 yShade1=$yShade1" )
    println( s"yBlock0=$yBlock0 yBlock1=$yBlock1" )
    println( s"hs=$hs ts=$ts rs=$rs" )

    val x = size.get.width()-1

    paint.background = backgroundColor
    paint.foreground = foregroundColor

    paint.write(x,yBegin,Symbols.Scroll.Vert.begin)
    paint.write(x,yEnd  ,Symbols.Scroll.Vert.end)

    (yShade0 to yShade1).foreach { y => paint.write(x,y,Symbols.Scroll.Vert.shade) }
    (yBlock0 to yBlock1).foreach { y => paint.write(x,y,Symbols.Scroll.Vert.block) }