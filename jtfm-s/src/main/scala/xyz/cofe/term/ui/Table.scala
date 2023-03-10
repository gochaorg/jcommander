package xyz.cofe.term.ui

import table._
import table.conf._

import xyz.cofe.term.cs.ObserverList
import xyz.cofe.lazyp.Prop
import xyz.cofe.term.cs.ObserverListImpl
import xyz.cofe.term.ui.prop._
import xyz.cofe.term.ui.prop.color._
import xyz.cofe.term.ui.paint._

import xyz.cofe.term.ui.table.TableGridProp
class Table[A]( using tableInputConf:TableInputConf, tableColors:TableColorsConf )
  extends Widget
  with WidgetInput
  with WidgetChildren[Widget] 
  with LocationRWProp
  with SizeRWProp
  with FillBackground
  with ColumnsProp[A]
  with HeaderProp
  with BorderProp
  with TableScrollProp
  with TableRowsProp[A]
  with TableSelectionProp[A]
  with TableGridProp[A]
  with TableGridPaint[A]( tableColors )
  with TableInput[A]
  with TableAutoResize
  with TableScrollPaint[A]
  with PaintChildren
