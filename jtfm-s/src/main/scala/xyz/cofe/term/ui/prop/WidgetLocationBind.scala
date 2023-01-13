package xyz.cofe.term.ui

import xyz.cofe.term.geom.Rect
import xyz.cofe.term.common.Size
import xyz.cofe.term.common.Position
import xyz.cofe.lazyp.ReleaseListener

case class BindWidget[A <: Widget]( widget:A ):
  def rect:Rect = widget.locationRect.get
  def size:Size = widget.size.get
  def width:Int = size.width()
  def height:Int = size.height()

  def location:Position = widget.location.get
  def leftTop:Position = rect.leftTop
  def leftBottom:Position = rect.leftBottom
  def rightTop:Position = rect.rightTop
  def rightBottom:Position = rect.rightBottom
  def center:Position = rect.center

extension (widget:Widget & SizeRWProp & LocationRWProp)
  def bind[A <: Widget]( wi:A )( compute:BindWidget[A] => Rect ):ReleaseListener = 
    def recompute:Unit = {
      val rect = compute(BindWidget(wi))
      widget.location = rect.leftTop
      widget.size = rect.size
    }
    val ls1 = wi.location.onChange ( recompute )
    val ls2 = wi.size.onChange( recompute )
    recompute
    ReleaseListener {
      ls1.release()
      ls2.release()
    }

  def bind[A<:Widget,B<:Widget]( wa:A, wb:B )( compute:(BindWidget[A],BindWidget[B]) => Rect ):ReleaseListener = 
    def recompute:Unit = {
      val rect = compute(BindWidget(wa),BindWidget(wb))
      widget.location = rect.leftTop
      widget.size = rect.size
    }
    val lst = List
      (wa.location.onChange ( recompute )
      ,wa.size.onChange( recompute )
      ,wb.location.onChange ( recompute )
      ,wb.size.onChange( recompute )
      )
    recompute
    ReleaseListener {
      lst.foreach(_.release())
    }    