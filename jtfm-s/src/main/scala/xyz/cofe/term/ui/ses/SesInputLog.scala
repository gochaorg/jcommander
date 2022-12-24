package xyz.cofe.term.ui.ses

import xyz.cofe.term.common.{
  InputEvent            => CInputEvent,
  InputKeyEvent         => CInputKeyEvent,
  InputCharEvent        => CInputCharEvent,
  InputMouseButtonEvent => CInputMouseButtonEvent,
  InputResizeEvent      => CInputResizeEvent
}
import xyz.cofe.term.ui.WidgetInput
import xyz.cofe.term.common.Size
import xyz.cofe.term.common.KeyName
import xyz.cofe.term.common.Position
import xyz.cofe.term.common.MouseButton

trait SesInputLog:
  def inputEvent[R](inputEvent:CInputEvent)(code: =>R):R = code
  def resize[R](size:Size)(code: =>R):R = code
  def focusNext[R](code: =>R):R = code
  def focusPrev[R](code: =>R):R = code
  def switchFocus(from:Option[WidgetInput],to:Option[WidgetInput]):Unit = ()
  def tryInput(wid:WidgetInput, event:CInputKeyEvent)(code: =>Boolean):Boolean = code
  def sendInput[R](wid:WidgetInput, event:CInputEvent)(code: =>R):R = code

object SesInputLog:
  given noLog:SesInputLog = new SesInputLog {}

  def simple(out:Appendable):SesInputLog = new SesInputLog {
    def tryAndLog[E,R](e:E)( code: => R ):R =
      try
        out.append(e.toString()).append("\n")
        val res = code
        res
      catch
        case err => 
          out.append(s"error $err").append("\n")
          throw err

    override def inputEvent[R](inputEvent: CInputEvent)(code: => R): R = 
      tryAndLog(InputEvent(inputEvent))(code)

    override def focusNext[R](code: => R): R = 
      tryAndLog("focusNext")(code)

    override def focusPrev[R](code: => R): R = 
      tryAndLog("focusPrev")(code)

    override def resize[R](size: Size)(code: => R): R = 
      tryAndLog(s"resize $size")(code)

    override def sendInput[R](wid: WidgetInput, event: CInputEvent)(code: => R): R = 
      tryAndLog(s"sendInput $wid ${InputEvent(event)}")(code)

    override def tryInput(wid: WidgetInput, event: CInputKeyEvent)(code: => Boolean): Boolean = 
      code

    override def switchFocus(from: Option[WidgetInput], to: Option[WidgetInput]): Unit = 
      tryAndLog(s"switchFocus from $from to $to")(())
  }

  enum InputEvent:
    case Key( keyName:KeyName,altDown:Boolean,shiftDown:Boolean,ctrlDown:Boolean )
    case Char( char:String,altDown:Boolean,shiftDown:Boolean,ctrlDown:Boolean )
    case Mouse( position:Position, button:MouseButton, pressed:Boolean )
    case Resize( size:Size )

  object InputEvent:
    def apply(ev:CInputEvent):Option[InputEvent] =
      ev match
        case ke:CInputKeyEvent => Some(InputEvent.Key(ke.getKey(), ke.isAltDown(), ke.isShiftDown(), ke.isControlDown()))
        case ke:CInputCharEvent => Some(InputEvent.Char(""+ke.getChar(), ke.isAltDown(), ke.isShiftDown(), ke.isControlDown()))
        case me:CInputMouseButtonEvent => Some(InputEvent.Mouse(me.position(), me.button(), me.pressed()))
        case re:CInputResizeEvent => Some(InputEvent.Resize(re.size()))
        case _ => None

  // type LogId = Long
  // type ThreadId = Long
  // type Time = Long
      
  // enum LogEvent(id:LogId,parent:Option[LogId],threadId:ThreadId=1,time:Time=1):
  //   case Input(id:LogId,parent:Option[LogId], event:InputEvent) extends LogEvent(id,parent)
  //   case Resize(id:LogId,parent:Option[LogId], size:Size) extends LogEvent(id,parent)
  //   case FocusNext(id:LogId,parent:Option[LogId]) extends LogEvent(id,parent)
  //   case FocusPrev(id:LogId,parent:Option[LogId]) extends LogEvent(id,parent)
  //   case SwitchFocus(id:LogId,parent:Option[LogId])

