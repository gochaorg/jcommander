package xyz.cofe.term.ui

import xyz.cofe.term.common.InputEvent
import xyz.cofe.lazyp.ReleaseListener
import xyz.cofe.term.ui.prop._

import ses._

trait WidgetInput extends Widget:
  def input(inputEvent:InputEvent):Boolean = 
    WidgetInput.delegateToNested(this, inputEvent)

  val focus:FocusClient = FocusClient(this)

object WidgetInput:
  def delegateToNested(widget:WidgetInput, inputEvent:InputEvent):Boolean =
    if widget.isInstanceOf[WidgetChildren[_]] 
    then 
      val childs = widget.asInstanceOf[WidgetChildren[Widget]].children.toList
      childs.foldLeft( Option[Boolean](false) ){
        case (consumed,child) =>
          consumed match
            case None => child match
              case wi:WidgetInput => Some(wi.input(inputEvent))
              case _ => None
            case Some(true) => Some(true)
            case Some(false) => child match
              case wi:WidgetInput => Some(wi.input(inputEvent))
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
  
  var onAcceptListeners : List[Option[WidgetInput]=>Unit] = List.empty
  def accept(from:Option[WidgetInput]):Unit =
    history = (FocusAction.Give(from) :: history).take(historyLen)
    onAcceptListeners.foreach(_(from))
    onChangeListeners.foreach(_())

  def onAccept( listener: Option[WidgetInput]=>Unit ):ReleaseListener =
    onAcceptListeners = listener :: onAcceptListeners    
    new ReleaseListener {
      def release(): Unit = 
        onAcceptListeners = onAcceptListeners.filterNot( l => l==listener )
    }

  var onLostListeners : List[Option[WidgetInput]=>Unit] = List.empty
  def lost(to:Option[WidgetInput]):Unit =
    history = (FocusAction.Lost(to) :: history).take(historyLen)
    onLostListeners.foreach(_(to))
    onChangeListeners.foreach(_())

  def onLost( listener: Option[WidgetInput]=>Unit ):ReleaseListener =
    onLostListeners = listener :: onLostListeners
    new ReleaseListener {
      def release(): Unit = 
        onLostListeners = onLostListeners.filterNot( l => l==listener )
    }

  var onChangeListeners: List[()=>Unit] = List.empty
  def onChange( listener: =>Unit ):ReleaseListener =
    val ls: ()=>Unit = ()=>listener
    onChangeListeners = ls :: onChangeListeners
    new ReleaseListener {
      def release(): Unit = 
        onChangeListeners = onChangeListeners.filterNot( l => l==ls )
    }

  def request:Unit = session.foreach { ses => ses.requestFocus(widget) }
