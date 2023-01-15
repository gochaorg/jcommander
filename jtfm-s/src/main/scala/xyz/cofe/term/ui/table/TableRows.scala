package xyz.cofe.term.ui.table

import xyz.cofe.term.cs.ObserverList
import xyz.cofe.term.ui.Widget

trait TableRows[A] extends Widget:
  val rows:ObserverList[A] = ObserverList.empty
  rows.onChange(repaint)
