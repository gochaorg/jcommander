package xyz.cofe.term.ui

import xyz.cofe.term.cs.ObserverList

trait WidgetChildren[C <: Widget] extends Widget:
  val children:ObserverList[C] = ObserverList.empty
  children.onChange { repaint }
  children.onInsert { ch => ch.parent.set(Some(this)) }
  children.onDelete { ch => ch.parent.compareAndSet(Some(this),None) }

