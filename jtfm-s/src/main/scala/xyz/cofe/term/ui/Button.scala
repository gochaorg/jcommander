package xyz.cofe.term
package ui

import xyz.cofe.lazyp.ReadWriteProp
import xyz.cofe.term.common.Color
import xyz.cofe.term.paint.PaintCtx
import xyz.cofe.term.common.InputEvent
import xyz.cofe.term.common.InputKeyEvent
import xyz.cofe.term.common.InputCharEvent
import xyz.cofe.term.common.InputMouseButtonEvent
import xyz.cofe.term.common.KeyName
import xyz.cofe.term.common.MouseButton
import xyz.cofe.lazyp.ReleaseListener
import xyz.cofe.term.common.Size
import xyz.cofe.term.ui.paint._
import xyz.cofe.term.ui.prop.color._
import xyz.cofe.term.ui.conf._

class Button(using colors: ButtonColorConf) extends Label(using colors.toLabelColorConf) with WidgetInput:
  def this(text:String)(using colors: ButtonColorConf) = {
    this()
    this.text.set(text)
    this.size.set(Size(text.length(),1))
  }

  val focusBgColor: ReadWriteProp[Color] = ReadWriteProp(Color.BlackBright)
  val focusFgColor: ReadWriteProp[Color] = ReadWriteProp(Color.YellowBright)

  foregroundColor.set(colors.foreground)
  backgroundColor.set(colors.background)

  override def paintText(paint: PaintCtx): Unit = 
    val (fg,bg) =
      if focus.isOwner
        then (focusFgColor, focusBgColor)
      else (foregroundColor, backgroundColor)

    paint.foreground = fg.get
    paint.background = bg.get
    paint.write(0,0,text.get)

  override def input(inputEvent: InputEvent): Boolean = 
    inputEvent match
      case ke: InputKeyEvent => 
        if ke.isAltDown() || ke.isControlDown() || ke.isShiftDown() 
        then false
        else if ke.getKey() == KeyName.Enter
          then { onAction.emit(); true }
          else false
      case ke: InputCharEvent => 
        if ke.isAltDown() || ke.isControlDown() || ke.isShiftDown() 
        then false
        else if ke.getChar() == ' '
          then { onAction.emit(); true }
          else false
      case me: InputMouseButtonEvent => 
        if me.button()==MouseButton.Left && me.pressed()
        then { onAction.emit(); true }
        else false

  val onAction : Listener[Unit] = Listener.unit

  def action( ls: => Unit ):this.type =
    onAction.listen(ls)
    this