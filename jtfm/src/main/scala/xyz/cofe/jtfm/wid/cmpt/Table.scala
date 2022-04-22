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
import com.googlecode.lanterna.input.KeyType
import xyz.cofe.jtfm.ev.EvalProperty
import xyz.cofe.jtfm.wid.RepaitRequest

/**
 * Таблица
 */
class Table[A]
  extends Widget[Table[A]]
  with BackgroundProperty[Table[A]]
  with ForegroundProperty[Table[A]]
  with OpaqueProperty[Table[A]]
  with FocusProperty[Table[A]](repait=true)
{
  /** Данные таблицы */
  private var _data:Seq[A] = List()

  /** Данные таблицы */
  def data:Seq[A] = _data

  /** Данные таблицы */
  def data_=(v:Seq[A]):Unit = { 
    _data=v
    dataListeners.foreach(l=>l())
  }

  private val repeaitReq = RepaitRequest.currentCycle[Table[A]]
  private def repaint():Unit = repeaitReq.repaitRequest(this)

  private var dataListeners:List[()=>Unit] = List()
  private def onData( ls: =>Unit ):Unit = { dataListeners = (()=>{ls}) :: dataListeners }

  private var _columns:Seq[Column[A,_]] = List()

  /** Колонки таблицы */
  def columns:Seq[Column[A,_]] = _columns

  /** Колонки таблицы */
  def columns_=(v:Seq[Column[A,_]]) = {
    _columns = v
    columnsListeners.foreach(l=>l())
  }

  private var columnsListeners:List[()=>Unit] = List()
  private def onColumns( ls: =>Unit ):Unit = { columnsListeners = (()=>{ls}) :: columnsListeners }

  /** Цвет заголовков таблицы */
  val headerForeground: OwnProperty[TextColor,Table[A]] = OwnProperty( TextColor.ANSI.YELLOW_BRIGHT,this)

  /** Цвет строки с фокусом */  
  val focusForeground: OwnProperty[TextColor,Table[A]] = OwnProperty( TextColor.ANSI.YELLOW_BRIGHT,this)

  /** Фон строки с фокусом */  
  val focusBackground: OwnProperty[TextColor,Table[A]] = OwnProperty( TextColor.ANSI.BLACK,this)

  /** Цвет выбраннй/выделенной строки */  
  val selectedForeground: OwnProperty[Option[TextColor],Table[A]] = OwnProperty( Some(TextColor.ANSI.GREEN_BRIGHT),this)

  /** Фон выбраннй/выделенной строки */  
  val selectedBackground: OwnProperty[Option[TextColor],Table[A]] = OwnProperty( Some(TextColor.ANSI.WHITE),this)

  /** Ширина колонок */
  val columnsWidths:EvalProperty[List[(Column[A,_],Int)],Table[A]] = EvalProperty(()=>{ 
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
  })
  rect.listen( (_,_,_) => { columnsWidths.recompute() })
  onColumns { columnsWidths.recompute() }

  /** Размещение колонк с данными и заголовками */
  val columnsRect:EvalProperty[List[(Column[A,_],Rect)],Table[A]] = EvalProperty(()=>{
    var x = 1
    var cols = List[(Column[A,_],Rect)]()
    columnsWidths.value.foreach { (c,w) => 
      cols = cols :+ ( c, Rect(x,0).size(w,rect.height) )
      x += w+1
    }
    cols
  })
  columnsWidths.listen((prop,old,cur) => { columnsRect.recompute() })

  /** Высота колонки с данными */
  val anyDataRectsHeight:EvalProperty[Option[Int],Table[A]] = EvalProperty(()=>{
      val reducedHeight = rect.height - 2 - 1
      if reducedHeight<1 then 
        None 
      else 
        Some(reducedHeight)
  })
  rect.listen( (_,_,_)=>{ anyDataRectsHeight.recompute() } )

  /** смещение индекса отображаемой строки */
  val scrollOffset: OwnProperty[Int,Table[A]] = new OwnProperty[Int,Table[A]](0,this)

  /** Индексы видимых строк */
  val visibleRowIndexesBounds:EvalProperty[Option[(Int,Int)],Table[A]] = EvalProperty(()=>{
    anyDataRectsHeight.value match {
      case None =>         
        None
      case Some(size) => 
        val x = Some( (scrollOffset.value, (scrollOffset.value + size) min data.length ) )
        x
    }
  })
  scrollOffset.listen( (_,_,_)=>visibleRowIndexesBounds.recompute() )

  /** 
   * Видимые строки с индексами: Строка, Индекс строки в data, y координата в dataRect
   */
  val visibleRowWithIndexes:EvalProperty[Seq[(A,Int,Int)],Table[A]] = EvalProperty(()=>{
    visibleRowIndexesBounds.value match {
      case None => List()
      case Some( (from,toExc) )=>
        if from>=toExc then
          List()
        else
          data.drop(from).take(toExc-from)
            .zip(from until toExc)
            .zip(0 until (toExc - from))
            .map(x=>(x._1._1,x._1._2,x._2))
    }
  })
  visibleRowIndexesBounds.listen( (_,_,_)=>visibleRowWithIndexes.recompute() )
  onData { visibleRowWithIndexes.recompute() }

  /** Размещение колонок данных */
  val dataRects:EvalProperty[List[(Column[A,_],Rect)],Table[A]] = EvalProperty(()=>{
    anyDataRectsHeight.value match {
      case None => List()
      case Some(reducedHeight) =>
        columnsRect.value.map { (col,rect) => 
        if reducedHeight>0 then
          Some( (col, rect.translate(0,2).reSize.setHeight(reducedHeight) ) )
        else
          None
      } filter { _.isDefined } map { _.get }
    }    
  })
  columnsRect.listen( (_,_,_) => dataRects.recompute() )

  /** Вычисление ячейки для заданной координаты */
  def dataCell( pt:Point ):Option[(A,Column[A,_])] = {
    dataRects.value.filter { (column, rect) => 
      val x = rect.include(pt)
      x
    }.map { (column, rect) =>
      (column, (pt.y - rect.top) + scrollOffset.value)
    }.filter { (column, rowIdx) =>
      val x = rowIdx >= 0 && rowIdx < data.length
      x
    }.map { (column,ridx) => ( data(ridx), column ) }
    .headOption
  }

  private def grid( cols:Seq[(Column[A,_],Rect)] ):List[HVLine] = {
    var hvLines = List[HVLine]()
    
    val lt = Point(0,0)
    val rb = rect.value.rightBottom.translate(-3,-3)
    val rt = Point(rb.x, 0)
    val lb = Point(0, rb.y)

    val style = focus.contains match {
      case true => Style.Double
      case false => Style.Single
    }

    hvLines = HVLine(lt, rt, style) :: hvLines
    hvLines = HVLine(lb, rb, style) :: hvLines
    hvLines = HVLine(lt, lb, style) :: hvLines
    hvLines = HVLine(rt, rb, style) :: hvLines

    cols.drop(1).foreach { (c,r) => 
      val a = Point(r.left-1, lt.y)
      val b = Point(r.left-1, lb.y)
      hvLines = HVLine(a, b, Style.Single) :: hvLines
    }

    hvLines
  }

  /** Строка содержащая фокус */
  def focusedRow:Option[A] = 
    focusedRowIndex.value.flatMap { ridx => if( data!=null && ridx>=0 && ridx<data.length ) Some(data(ridx)) else None }

  /** Индекс строки содержащей фокус */
  val focusedRowIndex:OwnProperty[Option[Int],Table[A]] = new OwnProperty(None,this)

  private def isFocused( row:A, rowIdx:Int ):Boolean =
    focusedRowIndex.value.isDefined && focusedRowIndex.value.get==rowIdx

  private def isSelected( row:A, rowIdx:Int ):Boolean =
    false

  override def render( gr:TextGraphics ):Unit = {
    this.renderOpaque(gr)

    // grid
    gr.setBackgroundColor( background.value )
    gr.setForegroundColor( foreground.value )
    grid(columnsRect.value).draw(gr)

    // header
    columnsRect.value.foreach { (col,rct) =>
      gr.setForegroundColor( headerForeground.value )
      gr.draw( rct.translate(0,1), col.name, Align.Center )
    }

    // data
    visibleRowWithIndexes.value.foreach { (row,ridx,yOffset) =>
      //println(s"render row=${row} ridx=${ridx} yOffset=${yOffset}")
      dataRects.value.foreach { (col,rct) => 
        val str = col.asString(row)
        val (fg,bg) = {
          if( focus.contains && isFocused(row,ridx) )
            ( focusForeground.value, focusBackground.value )
          else if( isSelected(row,ridx) )
            ( selectedForeground.value.getOrElse(foreground.value)
            , selectedBackground.value .getOrElse(background.value)
            )
          else
            ( foreground.value, background.value )
        }
        val cellRect = rct.reSize.setHeight(1).translate(0,yOffset)
        gr.setForegroundColor( fg )
        gr.setBackgroundColor( bg )
        gr.draw( cellRect, str, Align.Begin )
      }
    }
  }

  /** Обработка событий мыши */
  protected def inputMouseAction(ma:MouseAction):Boolean = {
    dataCell( ma ) match {
      case None =>
      case Some( (dataRow, dataColumn) ) =>
        data.indexOf(dataRow) match {
          case ridx:Int if ridx>=0 =>
            focusedRowIndex.value = Some(ridx)
          case _ =>
        }
    }
    true
  }

  /** Переход к следующей строке */
  protected def switchNext():Boolean = {
    focusedRowIndex.value match {
      case Some(idx) if idx< data.length-1 => 
        focusedRowIndex.value = Some(idx+1)
        visibleRowIndexesBounds.value match {          
          case None =>
          case Some( (from,toExc) ) => anyDataRectsHeight.value match {
            case None =>
            case Some( dataHeight ) =>
              scrollOffset.value = 
                if focusedRowIndex.value.get >= toExc then
                  focusedRowIndex.value.get - dataHeight + 1
                else
                  scrollOffset.value
          }
        }
        true
      case _ => false
    }
  }

  /** Переход к предыдущей строке */
  protected def switchPrev():Boolean = {
    focusedRowIndex.value match {
      case Some(idx) if idx>0 => 
        focusedRowIndex.value = Some(idx-1)
        visibleRowIndexesBounds.value match {          
          case None =>
          case Some( (from,toExc) ) => anyDataRectsHeight.value match {
            case None =>
            case Some( dataHeight ) =>
              scrollOffset.value = 
                if focusedRowIndex.value.get < from then
                  focusedRowIndex.value.get
                else
                  scrollOffset.value
          }
        }
        true
      case _ => false
    }
  }

  /** Обработка событий клавиатуры */
  protected def inputKeyboard(ks:KeyStroke):Boolean = {
    ks.getKeyType match {
      case KeyType.ArrowUp => 
        switchPrev()      
      case KeyType.ArrowDown => 
        switchNext()
      case _ => false
    }
  }

  override def input( ks: KeyStroke ):Boolean = {
    val x = ks match {
      case ma:MouseAction => 
        inputMouseAction(ma)
      case _ => 
        inputKeyboard(ks)
    }
    if( x )repaint()
    x
  }
}
