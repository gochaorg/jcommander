package xyz.cofe.term.ui

import xyz.cofe.term.common.InputEvent
import xyz.cofe.lazyp.ReleaseListener
import xyz.cofe.term.ui.prop._

import ses._
import xyz.cofe.lazyp.Prop

trait WidgetInput extends Widget:
  def input(inputEvent:InputEvent):Boolean = 
    WidgetInput.delegateToNested(this, inputEvent)

  val focus:FocusClient = FocusClient(this)

object WidgetInput:
  def delegateToNested(widget:WidgetInput, inputEvent:InputEvent):Boolean =
    if widget.isInstanceOf[WidgetChildrenRead] 
    then 
      val childs = widget.asInstanceOf[WidgetChildrenRead].children.toList
      childs.foldLeft( Option[Boolean](false) ){
        case (consumed,child) =>
          consumed match
            case None => child match
              case wi:WidgetInput => Some(wi.input(inputEvent))
              case _ => None
            case Some(true) => Some(true)
            case Some(false) => child match
              case wi:WidgetInput => Some(wi.input(inputEvent) )
              case _ => None
      }.getOrElse(false)
    else false

  def delegateToNested(childs:Iterable[? <: WidgetInput], inputEvent:InputEvent):Boolean =
    childs.toList.foldLeft( Option[Boolean](false) ){
      case (consumed,child) =>
        consumed match
          case None => Some(child.input(inputEvent))
          case Some(true) =>  Some(true)
          case Some(false) => Some(child.input(inputEvent))
    }.getOrElse(false)

class FocusClient( widget:WidgetInput ):
  def rootWidget:Option[RootWidget] = widget.toTreePath.listToLeaf.headOption.flatMap { w => 
    if w.isInstanceOf[RootWidget] 
    then Some(w.asInstanceOf[RootWidget])
    else None
  }
  def session:Option[Session] = rootWidget.map(_.session)

  val own = Prop.rw(false)

  def isOwner:Boolean = 
    session
      .flatMap { _.focusOwner }
      .map { w => w==widget }.getOrElse(false)

  def contains:Boolean = 
    session
      .flatMap { _.focusOwner }
      .map { owner => 
        owner.toTreePath.listToLeaf.contains(widget)
      }.getOrElse(false)

  enum FocusAction:
    case Give(from:Option[WidgetInput])
    case Lost(to:Option[WidgetInput])

  var history:List[FocusAction] = List.empty
  var historyLen = 25
  
  val onAccept = Listener[Option[WidgetInput]]
  def accept(from:Option[WidgetInput]):Unit =
    own.set(true)
    history = (FocusAction.Give(from) :: history).take(historyLen)
    onAccept.emit(from)
    onChange.emit()

  val acceptChild = Listener[(Option[WidgetInput],WidgetInput)]()

  val onLost = Listener[Option[WidgetInput]]
  def lost(to:Option[WidgetInput]):Unit =
    own.set(false)    
    history = (FocusAction.Lost(to) :: history).take(historyLen)
    onLost.emit(to)
    onChange.emit()

  val onChange : Listener[Unit] = Listener.unit

  def request:Unit = session.foreach { ses => ses.requestFocus(widget) }
