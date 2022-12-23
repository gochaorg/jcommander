package xyz.cofe.term.ui

import xyz.cofe.term.common.InputEvent

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

class FocusClient( widget:Widget ):
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

