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
import xyz.cofe.jtfm.ev.BasicCollection
import xyz.cofe.jtfm.wid.wc.Jobs
import org.slf4j.LoggerFactory
import xyz.cofe.jtfm.wid.MouseActionOps
import xyz.cofe.jtfm.wid.MouseButton

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
  private val log = LoggerFactory.getLogger(classOf[Table[_]])

  /** Данные таблицы */
  private var _data:Seq[A] = List()

  /** Данные таблицы */
  def data:Seq[A] = _data

  /** Данные таблицы */
  def data_=(v:Seq[A]):Unit = { 
    _data=v
    dataListeners.foreach(l=>l())
  }

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
  val selectedForeground: OwnProperty[Option[TextColor],Table[A]] = OwnProperty( Some(TextColor.ANSI.GREEN),this)

  /** Фон выбраннй/выделенной строки */  
  val selectedBackground: OwnProperty[Option[TextColor],Table[A]] = OwnProperty( Some(TextColor.ANSI.WHITE),this)

  /** Ширина колонок */
  val columnsWidths:EvalProperty[List[(Column[A,_],Int)],Table[A]] = EvalProperty(()=>{ 
    val undefWidths:List[(Column[A,_],Option[Int])] = columns.map { col =>  (col, col.width.prefect) }.toList
    val fixWidthSummary:Int = undefWidths.filter { (col,wOpt) => wOpt.isDefined }.map { (col,w) => w.get }.sum
    val visibleWidthSummary = rect.width - columns.size - 2

    val freeWidthSummary = (visibleWidthSummary - fixWidthSummary) max 0
    val freeColCount = undefWidths.filter { _._2.isEmpty }.length
    val autoSize = if( freeColCount>0 ){ (freeWidthSummary / freeColCount) max 1 } else { 1 }
    
    var restWidth = visibleWidthSummary

    var widths:List[(Column[A,_],Int)] = List()
    for( (col,wOpt) <- undefWidths ) {
      if( restWidth>0 ){
        val w = (wOpt match {
          case Some(w2) => w2
          case None => autoSize
        }) min restWidth
        restWidth -= w
        widths = widths :+ (col,w)
      }
    }

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

  /** Размещение заголовков колонк */
  val columnHeadersRect:EvalProperty[List[(Column[A,_],Rect)],Table[A]] = EvalProperty(()=>{
    columnsRect.value.map { case(col,rct) => 
      (col,rct.reSize.setHeight(1).translate(0,1))
    }
  })
  columnsWidths.listen((_,_,_)=>columnHeadersRect.recompute())

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
  onData { scrollOffset.value = 0 }

  /** Индексы видимых строк */
  val visibleRowIndexesBounds:EvalProperty[Option[(Int,Int)],Table[A]] = EvalProperty(()=>{
    log.trace("compute visibleRowIndexesBounds")
    anyDataRectsHeight.value match {
      case None =>         
        log.trace("compute none")
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
    val rb = Point(rect.width-1, rect.height-1)
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

  /** Выбранные объекты */
  val selection:BasicCollection[A] = new BasicCollection()

  private def isFocused( row:A, rowIdx:Int ):Boolean =
    focusedRowIndex.value.isDefined && focusedRowIndex.value.get==rowIdx

  private def isSelected( row:A, rowIdx:Int ):Boolean =
    selection.exists( itm => itm==row )

  val vertScrollBar:VScrollBar = new VScrollBar()
  nested.append(vertScrollBar)
  vertScrollBar.visible.value = false
  vertScrollBar.rect.bindTo( this ){ rect =>
    Rect(rect.width-1,2).size( 1, (rect.height-3) max 0 )
  }

  visibleRowIndexesBounds.listen( (prop, _, range1) => {
    range1 match {
      case None => vertScrollBar.visible.value = false
      case Some( (from,to) ) =>
        val yBegin = from
        val yEnd = data.length - to
        if( yBegin>=0 & yEnd>=0 && (yEnd + yBegin)>0 ) {
          val tot = yEnd + yBegin
          vertScrollBar.value.value = ( yBegin.toDouble / tot.toDouble )
          vertScrollBar.visible.value = true
        }
    }
  })
  
  vertScrollBar.background.value = background.value
  background.listen( (_,_,c) => vertScrollBar.background.value = c )

  vertScrollBar.foreground.value = foreground.value
  foreground.listen( (_,_,c) => vertScrollBar.foreground.value = c )

  Jobs.add {
    visibleRowIndexesBounds.recompute()
  }

  override def render( gr:TextGraphics ):Unit = {
    log.debug("render {}", rect.value)    
    this.renderOpaque(gr)

    // grid
    // log.trace( "columnsRect {}", columnsRect.value )
    columnsRect.value.foreach { (col,crect) => log.debug("column {} {}",col.name, crect) }
    gr.setBackgroundColor( background.value )
    gr.setForegroundColor( foreground.value )
    grid(columnsRect.value).draw(gr)

    // header
    columnsRect.value.foreach { (col,rct) =>
      gr.setForegroundColor( headerForeground.value )
      gr.draw( rct.translate(0,1), col.name, Align.Center )
    }

    // data
    log.trace( "visibleRowWithIndexes {}",visibleRowWithIndexes.value )
    visibleRowWithIndexes.value.foreach { (row,ridx,yOffset) =>
      //println(s"render row=${row} ridx=${ridx} yOffset=${yOffset}")
      dataRects.value.foreach { (col,rct) => 
        val str = col.asString(row)
        log.trace( "dataRect col {} rect {} value {}",col.name,rct,str )
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
        log.trace( "cellRect {}",cellRect )
        gr.setForegroundColor( fg )
        gr.setBackgroundColor( bg )
        gr.draw( cellRect, str, Align.Begin )
      }
    }
  }

  /**
   * Вызывается из inputMouseAction, при клике на заголовок таблицы.
   * Нужно для определя собственных правил сортировки
   * @param ma Событие мыши
   * @param col Колонка
   * @param rect Рамка заголовка
   */
  protected def onColumnHeaderClick(ma:MouseAction, col:Column[A,_], rect:Rect):Unit = {
    //ma.button == Some(MouseButton.Left)
    log.info(s"click header ${col.name}")
  }

  /** Обработка событий мыши */
  protected def inputMouseAction(ma:MouseAction):Boolean = {
    val dataCellClicked = dataCell( ma ) match {
      case None => false
      case Some( (dataRow, dataColumn) ) =>
        data.indexOf(dataRow) match {
          case ridx:Int if ridx>=0 =>
            focusedRowIndex.value = Some(ridx)
            true
          case _ =>
            false
        }
    }
    if( !dataCellClicked ){
      columnHeadersRect.value.find { case (col,rct) => rct.include(ma) }.foreach { case (col,rct) =>
        onColumnHeaderClick(ma,col,rct)
      }
    }

    true
  }

  def scrollToVisible( row:Int ):Unit = {
    visibleRowIndexesBounds.value match {
      case None =>
      case Some( (from,toExc) ) => 
        if( row >= toExc ){
          // строка ниже видимых строк, 
          // скролировать к границе toExc
          anyDataRectsHeight.value match {
            case None =>
            case Some( dataHeight ) =>
              scrollOffset.value = row - dataHeight + 1
          }
        }else if( row<from ){
          // строка выше видимых строк, 
          // скролировать к границе from
          anyDataRectsHeight.value match {
            case None =>
            case Some( dataHeight ) =>
              scrollOffset.value = row
          }
        }
      }
    }

  /** Переход к следующей строке */
  def switchNext():Boolean = {
    focusedRowIndex.value match {
      case Some(idx) if idx< data.length-1 => 
        focusedRowIndex.value = Some(idx+1)
        scrollToVisible(focusedRowIndex.value.get)
        true
      case _ => false
    }
  }

  /** Переход через блок видимых строк (page down) */
  def switchNextPage():Boolean = {
    focusedRowIndex.value match {
      case Some(idx) if idx< data.length-1 => 
        visibleRowIndexesBounds.value match {
          case None =>
          case Some( (from,toExc) ) =>
            if( (toExc-from)>0 ) {
              focusedRowIndex.value = Some( (idx+(toExc-from)) min (data.length-1) )
              scrollToVisible(focusedRowIndex.value.get)
            }
        }
        true
      case _ => false
    }
  }

  /** Переход к предыдущей строке */
  def switchPrev():Boolean = {
    focusedRowIndex.value match {
      case Some(idx) if idx>0 => 
        focusedRowIndex.value = Some(idx-1)
        scrollToVisible(focusedRowIndex.value.get)
        true
      case _ => false
    }
  }

  /** Переход через блок видимых строк (page up) */
  def switchPrevPage():Boolean = {
    focusedRowIndex.value match {
      case Some(idx) if idx>0 => 
        visibleRowIndexesBounds.value match {
          case None =>
          case Some( (from,toExc) ) =>
            if( (toExc-from)>0 ) {
              focusedRowIndex.value = Some( (idx-(toExc-from)) max 0 )
              scrollToVisible(focusedRowIndex.value.get)
            }
        }
        true
      case _ => false
    }
  }

  /** Переход к концу таблицы */
  def switchEnd():Boolean = {
    if data.length>0 then
      focusedRowIndex.value = Some(data.length-1)
      scrollToVisible(focusedRowIndex.value.get)
      true
    else
      false
  }

  /** Переход к началу таблицы */
  def switchHome():Boolean = {
    if data.length>0 then
      focusedRowIndex.value = Some(0)
      scrollToVisible(focusedRowIndex.value.get)
      true
    else
      false
  }

  /** Инверсия выделенной строки */
  def invertSelectFocused():Boolean = {
    focusedRow match {
      case None => false
      case Some(it) =>
        if selection.exists(a => a==it) then
          selection.remove(it)
        else
          selection.append(it)
        true
    }
  }

  vertScrollBar.onScrollUp { 
    switchPrevPage()
    repaint()
  }
  vertScrollBar.onScrollDown { 
    switchNextPage()
    repaint()
  }
  vertScrollBar.onScrollTo { v => 
    val normal_v = (v min 1.0) max 0.0
    if( data.length>1 ){
      focusedRowIndex.value = Some( (normal_v * data.length).toInt )
      scrollToVisible(focusedRowIndex.value.get)
      repaint()
    }
  }

  /** Обработка событий клавиатуры */
  protected def inputKeyboard(ks:KeyStroke):Boolean = {
    ks.getKeyType match {
      case KeyType.ArrowUp   => switchPrev()      
      case KeyType.ArrowDown => switchNext()
      case KeyType.PageDown  => switchNextPage()
      case KeyType.PageUp    => switchPrevPage()
      case KeyType.Home      => switchHome()
      case KeyType.End       => switchEnd()
      case KeyType.Insert    => invertSelectFocused()
      case KeyType.Character => ks.getCharacter() match {
        case ' ' => 
          val a = invertSelectFocused() 
          val b = switchNext()
          a || b
        case _ => false
      }
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
