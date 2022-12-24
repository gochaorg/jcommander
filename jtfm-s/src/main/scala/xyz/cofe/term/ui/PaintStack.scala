package xyz.cofe.term.ui

import xyz.cofe.term.paint.PaintCtx
import xyz.cofe.lazyp.ReadWriteProp
import xyz.cofe.term.common.Color
import xyz.cofe.term.buff.ScreenChar
import javafx.scene.layout.Background

trait PaintStack extends Widget:
  val paintStack: ReadWriteProp[List[PaintCtx => Unit]] = ReadWriteProp(List())
  override def paint(paintCtx: PaintCtx): Unit = 
    paintStack.get.foreach { fn => fn(paintCtx) }

extension (paintStack: ReadWriteProp[List[PaintCtx => Unit]])
  def add( render:PaintCtx => Unit ):Unit =
    paintStack.set(
      paintStack.get :+ render
    )

trait BackgroundColor:
  val backgroundColor: ReadWriteProp[Color] = ReadWriteProp(Color.Black)  
  if this.isInstanceOf[Widget] 
  then
    val wid = this.asInstanceOf[Widget]
    backgroundColor.onChange( wid.repaint )

trait ForegroundColor:
  val foregroundColor: ReadWriteProp[Color] = ReadWriteProp(Color.White)
  if this.isInstanceOf[Widget] 
  then
    val wid = this.asInstanceOf[Widget]
    foregroundColor.onChange( wid.repaint )

trait FocusOwnerBgColor:
  val focusOwnerBgColor: ReadWriteProp[Color] = ReadWriteProp(Color.BlackBright)  
  if this.isInstanceOf[Widget] 
  then
    val wid = this.asInstanceOf[Widget]
    focusOwnerBgColor.onChange( wid.repaint )

trait FocusOwnerFgColor:
  val focusOwnerFgColor: ReadWriteProp[Color] = ReadWriteProp(Color.YellowBright)
  if this.isInstanceOf[Widget] 
  then
    val wid = this.asInstanceOf[Widget]
    focusOwnerFgColor.onChange( wid.repaint )

trait FocusContainerBgColor:
  val focusContainerBgColor: ReadWriteProp[Color] = ReadWriteProp(Color.BlackBright)  
  if this.isInstanceOf[Widget] 
  then
    val wid = this.asInstanceOf[Widget]
    focusContainerBgColor.onChange( wid.repaint )

trait FocusContainerFgColor:
  val focusContainerFgColor: ReadWriteProp[Color] = ReadWriteProp(Color.WhiteBright)
  if this.isInstanceOf[Widget] 
  then
    val wid = this.asInstanceOf[Widget]
    focusContainerFgColor.onChange( wid.repaint )

trait FillBackground extends PaintStack with BackgroundColor:
  paintStack.set(
    paintStack.get :+ { paint => 
      fillBackground(paint)
    }
  )

  def fillBackgroundColor:Color =
    if this.isInstanceOf[WidgetInput]
    then
      val foc = this.asInstanceOf[WidgetInput].focus
      if this.isInstanceOf[FocusOwnerBgColor] && foc.isOwner 
      then this.asInstanceOf[FocusOwnerBgColor].focusOwnerBgColor.get 
      else 
        if this.isInstanceOf[FocusContainerBgColor] && foc.contains 
        then this.asInstanceOf[FocusContainerBgColor].focusContainerBgColor.get 
        else backgroundColor.get
    else
      backgroundColor.get

  def fillBackground(paint:PaintCtx):Unit =    
    val chr = ScreenChar(' ',Color.White, fillBackgroundColor)
    (0 until paint.bounds.size.height()).flatMap { y => 
      (0 until paint.bounds.size.width()).map { x => (x,y) }
    }.foreach { case (x,y) => 
      paint.write(x,y,chr)
    }

trait PaintChildren extends PaintStack with WidgetChildren[_]:
  paintStack.set(
    paintStack.get :+ { paint => 
      paintChildren(paint)
    }
  )

  def paintChildren(paint:PaintCtx):Unit =
    children.get.foreach { widget => 
      def paintChild():Unit = {
        val loc  = widget.location.get
        val size = widget.size.get
        if size.height()>0 && size.width()>0
        then
          val wCtx = paint.context.offset(loc).size(size).build
          widget.paint(wCtx)
      }
      widget match
        case visProp:VisibleProp if visProp.visible => paintChild()
        case _ => paintChild()
    }

trait TextProperty extends Widget:
  val text: ReadWriteProp[String] = ReadWriteProp("text")
  text.onChange { repaint }

trait PaintText extends PaintStack with TextProperty with ForegroundColor:
  paintStack.set(
    paintStack.get :+ { paint =>
      paintText(paint)
    }
  )

  def paintTextColor:Color =
    if this.isInstanceOf[WidgetInput]
    then
      val foc = this.asInstanceOf[WidgetInput].focus
      if this.isInstanceOf[FocusOwnerFgColor] && foc.isOwner 
      then this.asInstanceOf[FocusOwnerFgColor].focusOwnerFgColor.get 
      else 
        if this.isInstanceOf[FocusContainerFgColor] && foc.contains 
        then this.asInstanceOf[FocusContainerFgColor].focusContainerFgColor.get 
        else foregroundColor.get
    else
      foregroundColor.get
  
  def paintText( paint:PaintCtx ):Unit =
    paint.foreground = paintTextColor

    if this.isInstanceOf[FillBackground] 
    then 
      paint.background = 
        this.asInstanceOf[FillBackground].fillBackgroundColor

    paint.write(0,0,text.get)
