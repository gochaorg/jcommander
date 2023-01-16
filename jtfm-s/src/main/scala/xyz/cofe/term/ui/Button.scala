package xyz.cofe.term.ui

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

class Button extends Label with WidgetInput:
  def this(text:String) = {
    this()
    this.text.set(text)
    this.size.set(Size(text.length(),1))
  }

  val focusBgColor: ReadWriteProp[Color] = ReadWriteProp(Color.BlackBright)
  val focusFgColor: ReadWriteProp[Color] = ReadWriteProp(Color.YellowBright)

  // val focusContainerBgColor: ReadWriteProp[Color] = ReadWriteProp(Color.BlackBright)
  // val focusContainerFgColor: ReadWriteProp[Color] = ReadWriteProp(Color.YellowBright)

  foregroundColor.set(Color.White)
  backgroundColor.set(Color.Black)

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
          then { firePressed(); true }
          else false
      case ke: InputCharEvent => 
        if ke.isAltDown() || ke.isControlDown() || ke.isShiftDown() 
        then false
        else if ke.getChar() == ' '
          then { firePressed(); true }
          else false
      case me: InputMouseButtonEvent => 
        if me.button()==MouseButton.Left && me.pressed()
        then { firePressed(); true }
        else false

  protected def firePressed():Unit =
    onActionListener.foreach(_())

  var onActionListener : List[()=>Unit] = List.empty
  def onAction( listener: => Unit ):ReleaseListener =
    val ls:()=>Unit = ()=>listener
    onActionListener = ls :: onActionListener
    new ReleaseListener {
      def release(): Unit = 
        onActionListener = onActionListener.filterNot( l => l==ls )
    }

  def action( ls: => Unit ):this.type =
    onAction(ls)
    this