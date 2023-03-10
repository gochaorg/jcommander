package xyz.cofe.term.ui.table

import xyz.cofe.term.ui._
import xyz.cofe.term.buff.ScreenChar
import xyz.cofe.term.paint._
import xyz.cofe.term.ui.prop._
import xyz.cofe.term.ui.paint._
import xyz.cofe.term.ui.prop.color._

import xyz.cofe.lazyp.Prop

import TableGridProp.ContentDelim
import TableGridPaint._

import xyz.cofe.term.ui.prop.color.colorProp2Color
import xyz.cofe.term.common.Color
import xyz.cofe.lazyp.ReleaseListener
import xyz.cofe.term.geom.Rect

import table.conf.TableColorsConf
import org.slf4j.LoggerFactory

trait TableGridPaint[A]( colors: TableColorsConf )
extends TableGridProp[A]
with FillBackgroundColor
with ForegroundColor
with PaintStack
with TableRowsProp[A]
with TableSelectionProp[A]
with TableScrollProp
with WidgetInput
  :
  private val logger = LoggerFactory.getLogger("xyz.cofe.term.ui.table.TableGridPaint")

  paintStack.add(paintTableGrid)
  paintStack.add(paintCrossColumnsSelect)
  paintStack.add(paintCrossColumnsFocus)
  paintStack.add(paintTableHeader)
  paintStack.add(paintTableData)

  foregroundColor = colors.foreground
  backgroundColor = colors.background
  selection.selectionBgColor = colors.selectionBg
  selection.selectionFgColor = colors.selectionFg
  selection.focusContainerBgColor = colors.focusContainerBg
  selection.focusContainerFgColor = colors.focusContainerFg
  selection.focusOwnerBgColor = colors.focusOwnerBg
  selection.focusOwnerFgColor = colors.focusOwnerFg

  def paintTableGrid(paint:PaintCtx):Unit =
    val (lines, rects) = renderDelims.get
      .partitionMap {
        case ContentDelim.RenderLine(line) => Left(line) 
        case ContentDelim.Whitespace(rect,_) => Right(rect)
      }

    paint.foreground = foregroundColor
    paint.background = fillBackgroundColor

    rects.map { r => (r.leftTop, TextBlock.fill(r.size,ScreenChar(' ',foregroundColor, fillBackgroundColor)) ) }
      .foreach { case (at, textBlock) => paint.write(at,textBlock) }

    lines.draw(paint)
    
  def paintTableHeader(paint:PaintCtx):Unit =
    paint.foreground = foregroundColor
    paint.background = fillBackgroundColor
    headersBlocks.get.map { hb => 
      val pctx = paint.context
        .offset(hb.rect.leftTop)
        .size(hb.rect.size)
        .clipping(true)
        .build

      pctx.write(0,0, 
        align(hb.col.horizontalAlign.get, hb.rect.width, hb.col.title.get)
      )
    }

  val allDataRowsSum = Prop.eval(dataYPos, scroll.value, rows, selection.indexes, selection.focusedIndex, focus.own)
                         { case (dataYPos, scroll      , rows, selection        , focusedIndex          , focusOwn)=>
    val (dataYMin, dataYMax) = dataYPos
    
    val dataVisibleHeight = dataYMax - dataYMin
    val ridxVisibleFrom = scroll
    val ridxVisibleTo = scroll + dataVisibleHeight

    logger.info(s"allDataRowsSum: focusedIndex=$focusedIndex focusOwn=$focusOwn")

    val (head, 
         tailRaw
        ) = rows.zipWithIndex.map { case(row,ridx) =>
      
      val y = dataYMin + (ridx - ridxVisibleFrom)
      val selected = selection.contains(ridx)
      val focused = focusedIndex.map(idx => idx == ridx).getOrElse(false) && focusOwn

      logger.debug(s"allDataRowsSum.row y=$y selected=$selected focused=$focused")

      if ridx < ridxVisibleFrom 
      then 
        RenderDataRow.Head(row, ridx, selected, focused) 
      else
        if ridxVisibleFrom <= ridx && ridx < ridxVisibleTo 
        then 
          RenderDataRow.Render( row, ridx, selected, focused )
        else 
          RenderDataRow.Tail(row, ridx, selected, focused)
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
  allDataRowsSum.onChange(repaint)

  val renderDataRows = Prop.eval(allDataRowsSum) { case AllDataRowsSum(_,renderRows,_) => renderRows }
  val nonRenderTailDataRows = Prop.eval(allDataRowsSum) { case AllDataRowsSum(_,_,rows) => rows }
  val nonRenderHeadDataRows = Prop.eval(allDataRowsSum) { case AllDataRowsSum(rows,_,_) => rows }

  protected var cellFormatters : List[CellStyle[A] => CellStyle[A]] = List.empty
  def addCellFormat( formatter:CellStyle[A] => CellStyle[A] ):ReleaseListener =
    cellFormatters = cellFormatters :+ formatter
    ReleaseListener {
      cellFormatters = cellFormatters.filter( _ != formatter )
    }

  private def align(halign:HorizontalAlign, renderStringWidth:Int, string:String):String =
    if string.length < renderStringWidth 
    then 
      val remaider = renderStringWidth - string.length
      val remStr = " "*remaider
      halign match
        case HorizontalAlign.Left => 
          string + remStr
        case HorizontalAlign.Right => 
          remStr + string
        case HorizontalAlign.Center => 
          val leftSize = remStr.length() / 2
          remStr.take(leftSize) + string + remStr.drop(leftSize)
    else if string.length > renderStringWidth
    then 
      val remaider = (renderStringWidth - string.length).abs
      halign match
        case HorizontalAlign.Left => 
          string.take(renderStringWidth)
        case HorizontalAlign.Center =>
          val leftSize = remaider / 2
          string.drop(leftSize).take(renderStringWidth)
        case HorizontalAlign.Right =>
          string.drop(remaider)
    else
      string

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

        val (fg,bg) = 
          if renderRow.focused
          then 
            if focus.isOwner
            then (selection.focusOwnerFgColor.get,     selection.focusOwnerBgColor.get)
            else (selection.focusContainerFgColor.get, selection.focusContainerBgColor.get)
          else
            if renderRow.selected
            then (selection.selectionFgColor.get, selection.selectionBgColor.get)
            else (foregroundColor.get, backgroundColor.get)

        val cellStyle = cellFormatters.foldLeft(
          CellStyle(
            row = row,
            index = renderRow.index,
            selected = renderRow.selected,
            focused = renderRow.focused,
            column = column,
            string = string,
            foreground = fg,
            background = bg,
            halign = column.horizontalAlign.get,
          )
        ){ case(cellStyle, fmt) => 
          fmt(cellStyle)
        }

        val pctx = paint.context
          .offset(x0,y0)
          .size(x1-x0, y1-y0)
          .clipping(true)
          .build

        pctx.foreground = cellStyle.foreground
        pctx.background = cellStyle.background
        pctx.write(0,0,align(cellStyle.halign, x1 - x0, cellStyle.string))
      }
    }

  val crossColumnsFocusPaint = Prop.rw(true)
  crossColumnsFocusPaint.onChange(repaint)

  def paintCrossColumnsFocus(paint:PaintCtx):Unit =
    if crossColumnsFocusPaint.get then
      val (fg,bg) = 
        if focus.isOwner 
        then (selection.focusOwnerFgColor.get, selection.focusOwnerBgColor.get)
        else (selection.focusContainerFgColor.get, selection.focusContainerBgColor.get)

      val (dataYmin,_) = dataYPos.get

      val ys = 
        renderDataRows.get.zipWithIndex
          .filter( (r,i)=>r.focused )
          .map( (r,i) => (i+dataYmin) )

      dataBlocks.get.bounds.foreach { dataBoundRect =>
        ys.foreach { y =>
          (dataBoundRect.left until dataBoundRect.right).foreach { x =>
            paint.read(x,y).foreach( chr =>
              paint.write(x,y,chr.copy(
                foreground = fg,
                background = bg,
              ))
            )
          }
        }
      }

  val crossColumnsSelectPaint = Prop.rw(true)
  crossColumnsSelectPaint.onChange(repaint)

  def paintCrossColumnsSelect(paint:PaintCtx):Unit =
    if crossColumnsSelectPaint.get then
      val (fg,bg) = (selection.selectionFgColor.get, selection.selectionBgColor.get)

      val (dataYmin,_) = dataYPos.get

      val ys = 
        renderDataRows.get.zipWithIndex
          .filter( (r,i)=>r.selected )
          .map( (r,i) => (i+dataYmin) )

      dataBlocks.get.bounds.foreach { dataBoundRect =>
        ys.foreach { y =>
          (dataBoundRect.left until dataBoundRect.right).foreach { x =>
            paint.read(x,y).foreach( chr =>
              paint.write(x,y,chr.copy(
                foreground = fg,
                background = bg,
              ))
            )
          }
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

  case class CellStyle[A](
    row: A,
    index: Int,
    selected: Boolean,
    focused: Boolean,
    column: Column[A,_],
    string: String,
    foreground: Color,
    background: Color,
    halign: HorizontalAlign,
  )