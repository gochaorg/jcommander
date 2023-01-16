package xyz.cofe.term.ui.table

import xyz.cofe.term.ui._
import xyz.cofe.term.buff.ScreenChar
import xyz.cofe.term.paint._

import xyz.cofe.lazyp.Prop

import TableGridProp.RenderDelim
import TableGridPaint._

trait TableGridPaint[A] 
extends TableGridProp[A]
with FillBackgroundColor
with PaintTextColor
with PaintStack
with TableRowsProp[A]
with TableSelectionProp[A]
with TableScrollProp
with FocusOwnerBgColor
with FocusOwnerFgColor
  :
  paintStack.add(paintTableGrid)
  paintStack.add(paintTableHeader)
  paintStack.add(paintTableData)

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
    
  def paintTableHeader(paint:PaintCtx):Unit =
    paint.foreground = paintTextColor
    paint.background = fillBackgroundColor
    headersBlocks.get.map { hb => 
      val pctx = paint.context
        .offset(hb.rect.leftTop)
        .size(hb.rect.size)
        .clipping(true)
        .build

      pctx.write(0,0, hb.col.title.get)
    }

  val allDataRowsSum = Prop.eval(dataYPos,scroll.value,rows){ case (dataYPos,scroll,rows)=>
    val (dataYMin, dataYMax) = dataYPos
    
    val dataVisibleHeight = dataYMax - dataYMin
    val ridxVisibleFrom = scroll
    val ridxVisibleTo = scroll + dataVisibleHeight

    val (head, 
         tailRaw
        ) = rows.zipWithIndex.map { case(row,ridx) =>
      
      val y = dataYMin + (ridx - ridxVisibleFrom)
      val selected = selection.indexes.contains(ridx)
      val focused = selection.focusedIndex.get.map(idx => idx == ridx).getOrElse(false)

      if ridx < ridxVisibleFrom 
      then RenderDataRow.Head(row, ridx, selected, focused) 
      else
        if ridxVisibleFrom <= ridx && ridx < ridxVisibleTo 
        then 
          val sel = selection.indexes.contains(ridx)
          RenderDataRow.Render( row, ridx, selected, focused )
        else RenderDataRow.Tail(row, ridx, selected, focused)
    }.toList.partitionMap {
        case h : RenderDataRow.Head[_]   => Left(h) 
        case r : RenderDataRow.Render[_] => Right(r)
        case v : RenderDataRow.Tail[_]   => Right(v)
    } : @unchecked

    val (render:List[RenderDataRow.Render[A]],tail:List[RenderDataRow.Tail[A]]) = 
      tailRaw
      .asInstanceOf[List[RenderDataRow.Render[A] | RenderDataRow.Tail[A]]]
      .partitionMap {
      case r : RenderDataRow.Render[_] => Left(r)
      case v : RenderDataRow.Tail[_]   => Right(v)
    }

    AllDataRowsSum(head.asInstanceOf[List[RenderDataRow.Head[A]]], render, tail)
  }

  val renderDataRows = Prop.eval(allDataRowsSum) { case AllDataRowsSum(_,renderRows,_) => renderRows }

  def paintTableData(paint:PaintCtx):Unit =
    dataBlocks.get.foreach { dataBlock =>
      val yFrom = dataBlock.rect.top
      val x0 = dataBlock.rect.left
      val x1 = dataBlock.rect.right
      val column = dataBlock.col
      renderDataRows.get.zipWithIndex.foreach { case (renderRow,idx) =>
        val row = renderRow.row
        val y0 = yFrom + idx
        val y1 = y0 + 1
        val string = column.textOf(row)
        
        paint.foreground = foregroundColor
        paint.background = backgroundColor

        val pctx = paint.context
          .offset(x0,y0)
          .size(x1-x0, y1-y0)
          .clipping(true)
          .build
        pctx.write(0,0,string)
      }
    }  

object TableGridPaint:
  enum RenderDataRow:
    case Head[A]  ( row:A, index:Int, selected:Boolean, focused:Boolean ) extends RenderDataRow
    case Render[A]( row:A, index:Int, selected:Boolean, focused:Boolean ) extends RenderDataRow
    case Tail[A]  ( row:A, index:Int, selected:Boolean, focused:Boolean ) extends RenderDataRow

  case class AllDataRowsSum[A](
    invisibleHead: List[RenderDataRow.Head[A]],
    renderRows:    List[RenderDataRow.Render[A]],
    invisibleTail: List[RenderDataRow.Tail[A]],
  )
