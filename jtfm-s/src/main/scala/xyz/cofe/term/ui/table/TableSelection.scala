package xyz.cofe.term.ui.table

import xyz.cofe.term.ui.Widget
import xyz.cofe.term.cs.ObserverListImpl

trait TableSelection[A] extends TableRows[A]:
  val selection = new ObserverListImpl[A] with FocusedItem[A](rows)
  selection.onChange(repaint)
  selection.focusedIndex.onChange(repaint)
