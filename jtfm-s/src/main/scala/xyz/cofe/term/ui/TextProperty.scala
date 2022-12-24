package xyz.cofe.term.ui

import xyz.cofe.lazyp.ReadWriteProp
import xyz.cofe.lazyp.Prop
import xyz.cofe.term.common.Color
import xyz.cofe.term.paint.PaintCtx

trait TextProperty extends Widget:
  val text: ReadWriteProp[String] = ReadWriteProp("text")
  text.onChange { repaint }
  def text_=( string:String ):Unit = text.set(string)

implicit def textProp2String( prop:Prop[String] ):String = prop.get

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

