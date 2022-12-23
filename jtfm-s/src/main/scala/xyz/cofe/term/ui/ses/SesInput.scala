package xyz.cofe.term.ui
package ses

import xyz.cofe.term.common.InputResizeEvent
import xyz.cofe.term.common.InputKeyEvent
import xyz.cofe.term.common.KeyName

import SesInput._
import scala.reflect.ClassTag
import xyz.cofe.term.common.InputEvent
import com.googlecode.lanterna.screen.ScreenBuffer
import xyz.cofe.term.common.Size
import xyz.cofe.term.common.InputMouseButtonEvent
import xyz.cofe.term.common.Position
import xyz.cofe.term.buff._
import xyz.cofe.term.geom._
import xyz.cofe.term.common.MouseButton

trait SesInputBehavior:
  def switchFocusOnMouseEvent:Boolean = true

object SesInputBehavior:
  given defaultBehavior:SesInputBehavior = new SesInputBehavior {}

trait SesInput(log:SesInputLog, behavior:SesInputBehavior) extends SesPaint:
  private var focusOwnerValue : Option[WidgetInput] = None

  def focusOwner:Option[WidgetInput] = focusOwnerValue
  protected def focusOwner_=(newOwner:Option[WidgetInput]):Unit = 
    val oldOwner = focusOwnerValue
    focusOwnerValue = newOwner
    log.switchFocus(oldOwner, newOwner)

  protected def processInput():Unit =
    val inputEvOpt = console.read()
    if( inputEvOpt.isPresent() ){
      val inputEv = inputEvOpt.get()
      log.inputEvent(inputEv){
        inputEv match
          case resizeEv:InputResizeEvent =>
            val size = resizeEv.size()
            log.resize(size) {
              screenBuffer.resize(size)
              rootWidget.size.set(size)
            }
          case ke:InputKeyEvent =>
            ke.getKey() match
              case KeyName.Tab => focusNext(ke)
              case KeyName.ReverseTab => focusPrev(ke)
              case _ => send2focused(ke)
          case me:InputMouseButtonEvent =>
            findWidgetAt(me.position()).headOption.foreach { case (wid,local) => 
              if behavior.switchFocusOnMouseEvent && focusOwner != Some(wid) then switchFocusTo(wid)

              val eventForLocal = me.toLocal(local)
              log.sendInput(wid,eventForLocal)( wid.input( eventForLocal ))
            }
          case _ => 
            send2focused(inputEv)
      }
    }

  private def focusNext(ke:InputKeyEvent):Unit = 
    log.focusNext {      
      if ! focusOwner.map { focOwn => log.tryInput(focOwn,ke)(focOwn.input(ke)) }.getOrElse(false)
      then
        NavigateFrom(focusOwner.getOrElse(rootWidget))
          .forward.typed[WidgetInput].visibleOnly
          .nextOption().foreach(switchFocusTo)
    }

  private def focusPrev(ke:InputKeyEvent):Unit = 
    log.focusPrev {
      if ! focusOwner.map { focOwn => log.tryInput(focOwn,ke)(focOwn.input(ke)) }.getOrElse(false)
      then
        NavigateFrom(focusOwner.getOrElse(rootWidget))
          .backward.typed[WidgetInput].visibleOnly
          .nextOption().foreach(switchFocusTo)
    }

  private def switchFocusTo(widInput:WidgetInput):Unit =
    val oldOwner = focusOwner
    focusOwner = Some(widInput)
    oldOwner.foreach( w => w.focus.lost(Some(widInput)) )
    widInput.focus.accept(oldOwner)
    widInput.repaint
    log.switchFocus(oldOwner,Some(widInput))

  private def findWidgetAt( absolutePos:Position ):List[(WidgetInput,Position)] =
    NavigateFrom(rootWidget).forward.typed[WidgetInput].visibleOnly.toList.map { wid => 
      val localPos = wid.toTreePath.listToLeaf.map(_.location.get).foldLeft( absolutePos ) { case (res,pos) => res.move( -pos.x, -pos.y ) }
      (wid, localPos)
    } .filter { case (wid,localPos) => wid.size.get.leftUpRect(0,0).contains(localPos) }
      .reverse

  private def send2focused(ev:InputEvent):Unit =
    focusOwner.foreach( wid => log.sendInput(wid,ev)(wid.input(ev)) )
object SesInput:
  opaque type NavigateFrom = Widget
  object NavigateFrom:
    def apply(wid:Widget):NavigateFrom = wid
  extension (navFrom:NavigateFrom)
    def forward:Navigator[Widget] = Navigator(navFrom, w => w.toTreePath.nextByDeep.map(_.node) )
    def backward:Navigator[Widget] = Navigator(navFrom, w => w.toTreePath.prevByDeep.map(_.node) )

  extension (me:InputMouseButtonEvent)
    def toLocal(localPos:Position):InputMouseButtonEvent = new InputMouseButtonEvent {
      def button(): MouseButton = me.button()
      def position(): Position = localPos
      def pressed(): Boolean = me.pressed()
    }
