package xyz.cofe.jtfm.wid.cmpt

import xyz.cofe.jtfm.wid.{BackgroundProperty, FocusProperty, ForegroundProperty, OpaqueProperty, Widget}
import com.googlecode.lanterna.graphics.TextGraphics
import xyz.cofe.jtfm.gr.Rect
import xyz.cofe.jtfm.gr.TextGraphicsOps
import xyz.cofe.jtfm.gr.HVLine
import xyz.cofe.jtfm.gr.HVLineOps
import xyz.cofe.jtfm.gr.Point
import xyz.cofe.jtfm.gr.Symbols.Style
import com.googlecode.lanterna.TextColor
import xyz.cofe.jtfm.ev.OwnProperty
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.input.MouseAction
import xyz.cofe.jtfm.gr.Align

/**
 * Таблица
 */
class Table[A]
  extends Widget[Table[A]]
  with BackgroundProperty[Table[A]]
  with ForegroundProperty[Table[A]]
  with OpaqueProperty[Table[A]]
  with FocusProperty[Table[A]]()
{
  var data:Seq[A] = List()
  var columns:Seq[Column[A,_]] = List()

  val headerForeground: OwnProperty[TextColor,Table[A]] = OwnProperty( TextColor.ANSI.YELLOW_BRIGHT,this)

  private def columnsWidths:Seq[(Column[A,_],Int)] = {
    var widths:List[(Column[A,_],Int)] = List()
    
    val total = rect.width
    var restWidth = total - columns.size - 2
    
    val freeWidthCols = columns
    if freeWidthCols.size>0 then
      val freeWperCol = (restWidth / freeWidthCols.size) max 1
      val l = (freeWidthCols.map { c =>
        val w = (
          if restWidth>0 then
            if restWidth>=freeWperCol then
              freeWperCol
            else
              restWidth
          else
            0
          )
        if w>0 then
          restWidth -= w
          Some( (c,w) )
        else
          None
      } filter { _.isDefined } map { _.get })      
      widths = widths ::: l.toList

    widths
  }

  private def columnsRect:Seq[(Column[A,_],Rect)] = {
    var x = 1
    var cols = List[(Column[A,_],Rect)]()
    columnsWidths.foreach { (c,w) => 
      cols = cols :+ ( c, Rect(x,0).size(w,rect.height) )
      x += w+1
    }
    cols
  }

  private def grid( cols:Seq[(Column[A,_],Rect)] ):List[HVLine] = {
    var hvLines = List[HVLine]()
    
    val lt = Point(0,0)
    val rb = rect.value.rightBottom.translate(-2,-2)
    val rt = Point(rb.x, 0)
    val lb = Point(0, rb.y)

    hvLines = HVLine(lt, rt, Style.Single) :: hvLines
    hvLines = HVLine(lb, rb, Style.Single) :: hvLines
    hvLines = HVLine(lt, lb, Style.Single) :: hvLines
    hvLines = HVLine(rt, rb, Style.Single) :: hvLines

    cols.drop(1).foreach { (c,r) => 
      val a = Point(r.left-1, lt.y)
      val b = Point(r.left-1, lb.y)
      hvLines = HVLine(a, b, Style.Single) :: hvLines
    }

    //hvLines = HVLine(lt.translate(0,2), rt.translate(0,2), Style.Single) :: hvLines

    hvLines
  }

  override def render( gr:TextGraphics ):Unit = {
    this.renderOpaque(gr)

    gr.setBackgroundColor( background.value )
    gr.setForegroundColor( foreground.value )
    gr.putString(0,0,"table")

    val colRects = columnsRect

    // grid
    grid(colRects).draw(gr)

    // header
    colRects.foreach { (col,rct) =>
      gr.setForegroundColor( headerForeground.value )
      //drawCell(gr, rct.translate(0,1), col.name)
      gr.draw( rct.translate(0,1), col.name, Align.Center )
    }

    // data rects
    val dataRects = colRects.map { (col,rect) => 
      val reducedHeight = rect.height - 2 - 1
      if reducedHeight>0 then
        Some( (col, rect.translate(0,2).reSize.extend(0,-3) ) )
      else
        None
    } filter { _.isDefined } map { _.get }

    val visibleRows = data.take( dataRects.head._2.height )
    (0 until visibleRows.size).zip( visibleRows ).foreach { (y,row) =>
      dataRects.foreach { (col,rct) =>
        val str = col.asString(row)
        val cell = rct.reSize.setHeight(1).translate(0,y)
        gr.setForegroundColor( foreground.value )
        gr.setBackgroundColor( background.value )
        gr.draw( cell, str, Align.Begin )
      }
    }
  }

  private def inputMouseAction(ma:MouseAction):Boolean = {
    true
  }

  private def inputKeyboard(ma:KeyStroke):Boolean = {
    false
  }

  override def input( ks: KeyStroke ):Boolean = {
    ks match {
      case ma:MouseAction => inputMouseAction(ma)
      case _ => inputKeyboard(ks)
    }
  }
}
