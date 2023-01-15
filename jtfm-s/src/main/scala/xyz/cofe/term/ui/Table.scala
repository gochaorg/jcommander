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
  with TableScroll
  with TableRows[A]
  with TableSelection[A]
  with TableGrid[A]
  with TableGridPaint[A]
  
