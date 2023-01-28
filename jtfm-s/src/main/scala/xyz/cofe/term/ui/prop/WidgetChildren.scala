package xyz.cofe.term.ui
package prop

import xyz.cofe.term.cs.ObserverList

trait WidgetChildrenRead:
  def children: Iterable[? <: Widget]

trait WidgetChildren[C <: Widget] extends Widget with WidgetChildrenRead:
  val children:ObserverList[C] = ObserverList.empty
  children.onChange { repaint }
  children.onInsert { ch => ch.parent.set(Some(this)) }
  children.onDelete { ch => ch.parent.compareAndSet(Some(this),None) }

