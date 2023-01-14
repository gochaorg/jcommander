package xyz.cofe.term.ui

import table._

import xyz.cofe.term.cs.ObserverList
import xyz.cofe.lazyp.Prop
import xyz.cofe.term.cs.ObserverListImpl

class Table[A]
  extends Widget
  with WidgetInput
  with WidgetChildren[Widget] 
  with VisibleProp
  with LocationRWProp
  with SizeRWProp
  with FillBackground
  with ColumnsProp[A]
  with HeaderProp
  with BorderProp
  with TableGrid[A]
  :
  
  val rows:ObserverList[A] = ObserverList.empty
  rows.onChange(repaint)

  object scroll:
    val value = Prop.rw(0)
    value.onChange(repaint)

  val selection = new ObserverListImpl[A] with FocusedItem[A](rows)
  selection.onChange(repaint)
  selection.focusedIndex.onChange(repaint)

