package xyz.cofe.term.ui

import xyz.cofe.term.common.Position
import xyz.cofe.term.common.Size
import xyz.cofe.term.paint.PaintCtx
import xyz.cofe.lazyp.Prop

trait Widget:
  def location:Position
  def size:Size
  def paint(paintCtx:PaintCtx):Unit
  def repaint:Unit

trait Painteable

trait WidgetChildren:
  def children:Prop[List[Widget]]