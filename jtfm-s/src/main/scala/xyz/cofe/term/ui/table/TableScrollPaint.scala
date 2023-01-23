package xyz.cofe.term.ui.table

import xyz.cofe.term.ui.paint.add
import xyz.cofe.term.ui.prop.color._
import xyz.cofe.term.paint.PaintCtx
import xyz.cofe.term.geom.Symbols
import xyz.cofe.lazyp.Prop

trait TableScrollPaint[A] 
extends TableGridPaint[A]:
  paintStack.add(paintScrollBar)

  object scrollBar:
    lazy val invisibleHeaderRowsCount = Prop.eval(nonRenderHeadDataRows){ r => r.size }
    lazy val visibleRowsCount = Prop.eval(renderDataRows){ r => r.size }
    lazy val invisibleTailRowsCount   = Prop.eval(nonRenderTailDataRows){ r => r.size }
    lazy val paint = Prop.eval(invisibleHeaderRowsCount,visibleRowsCount,invisibleTailRowsCount,dataYPos) { case(hs,rs,ts,dy) => 
      val (dataYMin,dataYMax) = dy
      (hs!=0 || ts!=0) && rs>=5 && ((dataYMax-dataYMin)-2)>=3
    }
    lazy val yBegin = Prop.eval(dataYPos,paint) { case ((dataYMin,_),visible) =>
      if visible then Some(dataYMin) else None
    }
    lazy val yEnd = Prop.eval(dataYPos,paint){ case((_,dataYMax),visible) =>
      if visible then Some(dataYMax-1) else None
    }
    lazy val scrollSum = Prop.eval(yBegin,yEnd){ case(yb,ye) => 
      yb.flatMap { yb =>
        ye.map { ye =>
          (ye-yb)-2
        }
      }
    }
    lazy val rowsCount = Prop.eval(
      invisibleHeaderRowsCount, 
      visibleRowsCount, 
      invisibleTailRowsCount
    ) { case (hc,vc,tc) => hc+vc+tc }

    lazy val invScrollHead = Prop.eval(
      invisibleHeaderRowsCount,
      rowsCount,
      scrollSum
    ){ case (inv,rows,scrl) => 
      scrl.map { scrl => 
        ((inv.toDouble / rows.toDouble) * scrl.toDouble).toInt max {
          if inv>0 then 1 else 0
        }
      }
    }
    lazy val invScrollTail = Prop.eval(
      invisibleTailRowsCount,
      rowsCount,
      scrollSum
    ){ case (inv,rows,scrl) => 
      scrl.map { scrl => 
        ((inv.toDouble / rows.toDouble) * scrl.toDouble).toInt max {
          if inv>0 then 1 else 0
        }
      }
    }
    lazy val visScrollSize = Prop.eval(
      invScrollHead,
      invScrollTail,
      scrollSum,
    ){ case (head,tail,sum) => 
      head.flatMap { head => 
        tail.flatMap { tail =>
          sum.map { sum => 
            sum - head - tail
          }
        }
      }
    }

    lazy val yScrollAreaBegin = Prop.eval(yBegin){ case (y) => 
      y.map(_+1)
    }
    lazy val yScrollAreaEnd = Prop.eval(yEnd){ case (y) => 
      y.map(_-1)
    }

    lazy val yScrollBlockBegin = Prop.eval(yScrollAreaBegin,invScrollHead){ 
      case (y,s) => y.flatMap( y => s.map( s => y+s) )
    }
    lazy val yScrollBlockEnd   = Prop.eval(
      yScrollAreaBegin,
      invScrollHead,
      visScrollSize
    ){ case (y,s0,s1) =>
      y.flatMap(y=> s0.flatMap(s0=> s1.map(s1 => y+s0+s1)) )
    }

    lazy val x = Prop.eval(size){ case (size) => size.width()-1 }

  def paintScrollBar(paint:PaintCtx):Unit =
    val headerSize = nonRenderHeadDataRows.get.size
    val renderSize = renderDataRows.get.size
    val tailSize = nonRenderTailDataRows.get.size
    val (dataYMin,dataYMax) = dataYPos.get

    if (headerSize!=0 || tailSize!=0) && renderSize>=5 && ((dataYMax-dataYMin)-2)>=3
    then
      renderScroll(paint,headerSize,renderSize,tailSize)

  private def renderScroll(paint:PaintCtx,headerSize:Int,renderSize:Int,tailSize:Int):Unit =
    val x = scrollBar.x.get
    scrollBar.yBegin.get.foreach { yBegin => 
      scrollBar.yEnd.get.foreach { yEnd => 
        scrollBar.yScrollAreaBegin.get.foreach { yScrollAreaBegin => 
          scrollBar.yScrollAreaEnd.get.foreach { yScrollAreaEnd => 
            scrollBar.yScrollBlockBegin.get.foreach { yScrollBlockBegin =>
              scrollBar.yScrollBlockEnd.get.foreach { yScrollBlockEnd =>
                paint.background = backgroundColor
                paint.foreground = foregroundColor

                paint.write(x,yBegin,Symbols.Scroll.Vert.begin)
                paint.write(x,yEnd  ,Symbols.Scroll.Vert.end)

                (yScrollAreaBegin  to yScrollAreaEnd).foreach  { y => paint.write(x,y,Symbols.Scroll.Vert.shade) }
                (yScrollBlockBegin to yScrollBlockEnd).foreach { y => paint.write(x,y,Symbols.Scroll.Vert.block) }
              }
            }
          }
        }
      }
    }