package xyz.cofe.term.ui.table

import xyz.cofe.lazyp.Prop

enum PreferredWidth:
  case Auto

enum HorizontalAlign:
  case Left
  case Center
  case Right

trait Column[R,V:CellText]:
  def id:String
  def read(row:R):V
  def textOf(row:R):String = summon[CellText[V]].cellTextOf(read(row))
  
  val title:Prop[String] = Prop.rw("?")
  val width:Prop[Int] = Prop.rw(1)

  val preferredWidth = Prop.rw(PreferredWidth)
  val horizontalAlign = Prop.rw(HorizontalAlign)
  val rightDelimiter = Prop.rw(Delimeter.SingleLine)
  val leftDelimiter = Prop.rw(Delimeter.None)