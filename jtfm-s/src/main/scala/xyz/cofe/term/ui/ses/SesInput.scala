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
import xyz.cofe.lazyp.Prop
import org.slf4j.LoggerFactory
import org.slf4j.Logger
import xyz.cofe.log._

trait SesInputBehavior:
  def switchFocusOnMouseEvent:Boolean = true

object SesInputBehavior:
  given defaultBehavior:SesInputBehavior = new SesInputBehavior {}

trait SesInput(behavior:SesInputBehavior) extends SesPaint with SesJobs:
  private implicit val logger : Logger = LoggerFactory.getLogger("xyz.cofe.term.ui.SesInput")
  
  private var focusOwnerValue : Option[WidgetInput] = None

  /** владелец фокуса */
  def focusOwner:Option[WidgetInput] = focusOwnerValue
  protected def focusOwner_=(newOwner:Option[WidgetInput]):Unit = 
    val oldOwner = focusOwnerValue
    focusOwnerValue = newOwner

  /** Верхний диалог */
  def topDialog =
    rootWidget.children
      .filter(_.isInstanceOf[Dialog])
      .map(_.asInstanceOf[Dialog])
      .filter(_.visible.inTree)
      .lastOption

  /** 
   * подписчики на события ввода
   * 
   * ```fn( inputEvent ):Boolean``` 
   * 
   *  - если результат `true`  - то событие НЕ будет передаваться дальше в другие обработчики
   *  - если результат `false` - то событие будет передаваться дальше в другие обработчики
   */
  var inputListeners: List[InputEvent => Boolean] = List.empty

  val keyStrokeMap : KeyStrokeMap[Action] = KeyStrokeMap( Map(
    KeyStroke.KeyEvent(KeyName.Tab,false,false,false)         -> Set(Action.FocusNext),
    KeyStroke.KeyEvent(KeyName.ReverseTab,false,false,false)  -> Set(Action.FocusBack),
    KeyStroke.KeyEvent(KeyName.Escape,false,false,false)      -> Set(Action.CloseDialog),
  ))
  val keyStrokeInputParser : KeyStrokeMap.KeyStrokeInputParser[Action] = KeyStrokeMap.KeyStrokeInputParser(keyStrokeMap)

  /** обработка входящий событий (клавиатуры, мыши, окна, ...) */
  protected def processInput():Unit =
    val inputEvOpt = console.read()
    if( inputEvOpt.isPresent() ){      
      val inputEv = inputEvOpt.get()
      log"input event: ${inputEv}"

      val actions = keyStrokeInputParser.input(inputEv)
      log"matched actions: ${actions}"

      val consumed = inputListeners.foldLeft( false ){ case (consumed,ls) => 
        if !consumed then ls(inputEv) else consumed
      }
      log"inputListeners consumed=${consumed}"

      if !consumed 
      then
        inputEv match
          case resizeEv:InputResizeEvent =>
            val size = resizeEv.size()
            log"InputResizeEvent size=${size}"

            screenBuffer.resize(size)
            rootWidget.size.set(size)
          case ke:InputKeyEvent =>            
            if actions.nonEmpty 
            then
              log"InputKeyEvent: execute non empty actions"
              actions.foreach { 
                case Action.FocusNext => focusNext(ke)
                case Action.FocusBack => focusPrev(ke)
                case Action.CloseDialog if topDialog.isDefined => closeTopDialog()
                case _ => send2focused(ke)
              }
            else
              log"InputKeyEvent: send2focused"
              send2focused(ke)
          case me:InputMouseButtonEvent =>
            log"InputMouseButtonEvent ${me}"
            findWidgetAt(me.position()).headOption.foreach { case (wid,local) => 
              if topDialog.map { dlg => 
                val x = wid.toTreePath.listToLeaf.contains(dlg) 
                log"top dialog contains target widget = $x"
                x
              }.getOrElse( true )
              then
                if behavior.switchFocusOnMouseEvent && focusOwner != Some(wid) && me.pressed() 
                then 
                  switchFocusTo(wid)

                val eventForLocal = me.toLocal(local)
                wid.input( eventForLocal )
            }
          case _ => 
            send2focused(inputEv)
    }

  private def focusNext(ke:InputKeyEvent):Unit = 
    if ! focusOwner.map { focOwn => focOwn.input(ke) }.getOrElse(false)
    then
      NavigateFrom(focusOwner.getOrElse(rootWidget))
        .forward.typed[WidgetInput].visibleOnly
        .nextOption().foreach(switchFocusTo)

  private def focusPrev(ke:InputKeyEvent):Unit = 
      if ! focusOwner.map { focOwn => focOwn.input(ke) }.getOrElse(false)
      then
        NavigateFrom(focusOwner.getOrElse(rootWidget))
          .backward.typed[WidgetInput].visibleOnly
          .nextOption().foreach(switchFocusTo)

  private def switchFocusTo(widInput:WidgetInput):Unit =
    val topDlg = topDialog
    if topDlg.map { dlg => widInput.toTreePath.listToLeaf.contains(dlg) }.getOrElse( true )
    then      
      debug"switchFocusTo $widInput"
      val oldOwner = focusOwner
      focusOwner = Some(widInput)
      oldOwner.foreach( w => w.focus.lost(Some(widInput)) )
      widInput.focus.accept(oldOwner)
      widInput.repaint
    else
      topDlg.foreach { dlg =>
        debug"cancel by dialog $dlg"
      }

  private def findWidgetAt( absolutePos:Position ):List[(WidgetInput,Position)] =
    NavigateFrom(rootWidget).forward.typed[WidgetInput].visibleOnly.toList.map { wid =>       
      val localPos = wid.toTreePath.listToLeaf.map(_.location.get).foldLeft( absolutePos ) { case (res,pos) => res.move( -pos.x, -pos.y ) }
      (wid, localPos)
    } .filter { case (wid,localPos) => wid.size.get.leftUpRect(0,0).contains(localPos) }
      .reverse

  private def send2focused(ev:InputEvent):Unit =
    focusOwner.foreach { wid => 
      log"send2focused $ev $wid"
      wid.input(ev) 
    }

  def requestFocus( widInput:WidgetInput ):Unit =
    addJob( ()=>{
      switchFocusTo(widInput)
    })

  def closeTopDialog():Option[Dialog] =
    topDialog.map { dlg => 
      dlg.close()
      dlg
    }


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

  enum Action:
    case FocusNext
    case FocusBack
    case CloseDialog