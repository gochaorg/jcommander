package xyz.cofe.term.ui
package paint

import xyz.cofe.term.paint.PaintCtx
import xyz.cofe.term.ui.prop._

trait PaintChildrenMethod extends WidgetChildren[_]:
  def paintChildren(paint:PaintCtx):Unit =
    PaintChildren.paint(children.get, paint)

trait PaintChildren extends PaintStack with PaintChildrenMethod:
  paintStack.add(paintChildren)

object PaintChildren:
  def paint(childs:Iterable[? <: Widget], paint:PaintCtx):Unit =
    childs.foreach { widget => 
      def paintChild():Unit = {
        val loc  = widget.location.get
        val size = widget.size.get
        if size.height()>0 && size.width()>0
        then
          val wCtx = paint.context.offset(loc).size(size).build
          widget.paint(wCtx)
      }
      widget match
        case visProp:VisibleProp if visProp.visible.value.get => paintChild()
        case _ => ()
    }

