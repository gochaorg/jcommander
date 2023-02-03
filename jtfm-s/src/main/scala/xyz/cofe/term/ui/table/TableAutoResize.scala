package xyz.cofe.term.ui.table

import xyz.cofe.lazyp.Prop
import xyz.cofe.term.ui.paint.PaintStack
import xyz.cofe.term.ui.paint.add
import xyz.cofe.term.paint.PaintCtx

trait TableAutoResize extends PaintStack with ColumnsProp[_] with BorderProp:
  val columnsSizeRecomputed = Prop.rw(false)
  size.onChange { columnsSizeRecomputed.set(false) }

  paintStack.add(checkColumnsSizeRecomputed)
  private def checkColumnsSizeRecomputed(paint:PaintCtx):Unit =
    if ! columnsSizeRecomputed.get then
      autoResizeColumns()

  def autoResizeColumnsDeferred():Unit =
    columnsSizeRecomputed.set(false)

  def autoResizeColumns():Unit =
    columnsSizeRecomputed.set(true)
    
    val borderWidth = border.get.left.size + border.get.right.size
    val columnsCount = columns.size
    val betweenColumnsWidth = columns.zipWithIndex.map { case (column,colIdx) =>
      val left  = column.leftDelimiter.get.size
      val right = column.rightDelimiter.get.size
      if colIdx==0
      then right
      else
        if colIdx==(columnsCount-1)
        then left
        else left + right
    }.sum

    val widWidth = size.get.width()
    val remeainderInner = widWidth - borderWidth - betweenColumnsWidth

    val constWidthColumns = columns.get.toList.flatMap( col => col.preferredWidth.get match
      case PreferredWidth.Auto => List.empty
      case PreferredWidth.Const(size) => List( (col,size) )
    )

    val columnsWidthAuto = columns.get.toList.flatMap { col => col.preferredWidth.get match
      case PreferredWidth.Auto => List(col)
      case PreferredWidth.Const(size) => List.empty
    }

    val minWidth = 1

    val constWidthSum = constWidthColumns.map((_,w)=>w max minWidth).sum
    val autoRemainer = remeainderInner - constWidthSum
    val autoOneWidth = (autoRemainer / columnsWidthAuto.size) max minWidth
    val autoDivRem = autoRemainer % columnsWidthAuto.size
    
    constWidthColumns.foreach { case (col,w) => 
      col.width.set(w) 
    }
    
    if autoRemainer>0
    then
      columnsWidthAuto.foldLeft( (autoRemainer,autoDivRem) ){ case ((rem,drem),col) => 
        if rem>0 
        then
          val wadd = if drem>0 then 1 else 0
          val w = autoOneWidth + wadd
          col.width.set( w )
          (rem - w, drem - wadd)
        else
          (rem, drem)
      }