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
import java.util.concurrent.atomic.AtomicLong
import java.util.WeakHashMap
import xyz.cofe.term.ui.Widget
import xyz.cofe.json4s3.derv.ToJson
import xyz.cofe.json4s3.derv._
import xyz.cofe.term.ui.log.given

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

  case class WidgetId(id:String)

  private val widIdSeq = new AtomicLong(0)
  private val widIdMap = new WeakHashMap[Widget,String]()
  def idOf( wid:Widget ):WidgetId = 
    val id = widIdMap.get(wid)
    if id==null
    then
      val idNum = widIdSeq.incrementAndGet()
      val idStr = s"w${idNum}($wid)"
      widIdMap.put(wid,idStr)
      WidgetId(idStr)
    else
      WidgetId(id)

  def writeTo( eventWriter: SesInputEvent => Unit ):SesInputLog = new SesInputLog {

    override def inputEvent[R](inputEvent: CInputEvent)(code: => R): R = 
      InputEvent(inputEvent).foreach( ev => eventWriter(SesInputEvent.Input(ev)) )
      code

    override def resize[R](size: Size)(code: => R): R = 
      eventWriter( SesInputEvent.Resize(size) )
      code

    override def focusNext[R](code: => R): R = 
      eventWriter( SesInputEvent.FocusNext )
      code

    override def focusPrev[R](code: => R): R = 
      eventWriter( SesInputEvent.FocusPrev )
      code

    override def switchFocus(from: Option[WidgetInput], to: Option[WidgetInput]): Unit = 
      eventWriter( SesInputEvent.SwitchFocus(from.map(idOf),to.map(idOf)) )

    override def sendInput[R](wid: WidgetInput, event: CInputEvent)(code: => R): R = 
      InputEvent(event).foreach( ev => eventWriter(SesInputEvent.SendInput(idOf(wid), ev)))
      code

    override def tryInput(wid: WidgetInput, event: CInputKeyEvent)(code: => Boolean): Boolean = 
      val res = code
      InputEvent(event).foreach( ev => eventWriter( SesInputEvent.TryInput(idOf(wid), ev, res) ))
      res
  }

  def writeJsonTo( out:Appendable ):SesInputLog =
    writeTo { ev =>
      out.append( ev.json ).append("\n")
    }

  enum InputEvent:
    case Key(  keyName:KeyName, altDown:Boolean, shiftDown:Boolean, ctrlDown:Boolean )
    case Char( char:String,     altDown:Boolean, shiftDown:Boolean, ctrlDown:Boolean )
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

  enum SesInputEvent:
    case Input(event:InputEvent)
    case Resize(size:Size)
    case FocusNext
    case FocusPrev
    case SwitchFocus(from:Option[WidgetId], to:Option[WidgetId])
    case SendInput(wid:WidgetId, inputEvent:InputEvent)
    case TryInput(wid:WidgetId, inputEvent:InputEvent, result:Boolean)

