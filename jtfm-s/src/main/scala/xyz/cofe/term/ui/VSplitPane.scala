package xyz.cofe.term.ui

import xyz.cofe.term.ui.prop.WidgetChildren
import xyz.cofe.term.ui.paint.PaintChildren
import xyz.cofe.term.ui.prop.LocationRWProp
import xyz.cofe.term.ui.prop.SizeRWProp
import xyz.cofe.lazyp.Prop
import xyz.cofe.lazyp.Prop._
import xyz.cofe.term.paint.PaintCtx
import xyz.cofe.lazyp.ReleaseListener
import xyz.cofe.term.ui.prop.WidgetChildrenRead
import xyz.cofe.term.common.Position
import xyz.cofe.term.common.Size

//todo require ReadOnly children

class VSplitPane
extends WidgetInput
with LocationRWProp
with SizeRWProp
with WidgetChildrenRead
with PaintChildren
:
  type RelocableWidget = Widget & LocationRWProp & SizeRWProp

  val delimeter = Prop.rw(0.5)
  delimeter.onChange(recompute)
  delimeter.onChange(repaint)

  val leftWidget  = Prop.rw(None:Option[? <: RelocableWidget])
  leftWidget.onChange(repaint)
  leftWidget.onChange(reomputeDeferred)

  val rightWidget = Prop.rw(None:Option[? <: RelocableWidget])
  rightWidget.onChange(repaint)
  rightWidget.onChange(reomputeDeferred)

  size.onChange(reomputeDeferred)

  override def children: Iterable[? <: Widget] = 
    leftWidget.get.toList ++ rightWidget.get.toList

  private var defferdRecomputed = false
  def reomputeDeferred:Unit = 
    defferdRecomputed = false
    Session.addJob {
      if ! defferdRecomputed then
        recompute
        defferdRecomputed = true
    }
    
  private var leftRightRelease:List[ReleaseListener] = List.empty
  private def rebind:Unit =
    leftRightRelease.foreach( _.release() )
    leftRightRelease = children.map { w => 
      w.visible.value.onChange { reomputeDeferred }
    }.toList

  def recompute:Unit = 
    if leftWidget.get.isDefined && rightWidget.get.isDefined then
      val lwid = leftWidget.get.get
      val rwid = rightWidget.get.get
      if lwid.visible.inTree && rwid.visible.inTree then
        recomputeBoth(lwid, rwid)
      else if lwid.visible.inTree && ! rwid.visible.inTree then
        recomputeLeft( lwid )
      else if ! lwid.visible.inTree && rwid.visible.inTree then
        recomputeRight( rwid )
      else 
        recomputeNone()
    else if leftWidget.get.isDefined && rightWidget.get.isEmpty then
      val lwid = leftWidget.get.get
      if lwid.visible.inTree then
        recomputeLeft(lwid)
      else
        recomputeNone()
    else if leftWidget.get.isEmpty && rightWidget.get.isDefined then
      val rwid = rightWidget.get.get
      if rwid.visible.inTree then
        recomputeRight(rwid)
      else
        recomputeNone()
    else
      recomputeNone()

  private def recomputeBoth( leftWid: RelocableWidget, rightWid: RelocableWidget ):Unit =
    val delim = (((delimeter.get min 1.0) max 0.0) * size.width()).toInt
    leftWid.location = Position(0,0)
    leftWid.size = Size( delim, size.height() )

    rightWid.location = Position(delim,0)
    rightWid.size = Size( size.width() - delim, size.height() )

  private def recomputeLeft( leftWid: RelocableWidget ):Unit =
    recomputeOne(leftWid)

  private def recomputeRight( rightWid: RelocableWidget ):Unit =
    recomputeOne(rightWid)

  private def recomputeOne( wid: RelocableWidget ):Unit =
    wid.location = Position(0,0)
    wid.size = size.get

  private def recomputeNone():Unit =
    ()
