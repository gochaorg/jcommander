package xyz.cofe.term.ui
package paint

import xyz.cofe.term.paint.PaintCtx
import xyz.cofe.term.ui.prop._
import xyz.cofe.term.common.Position
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import xyz.cofe.log._

trait PaintChildrenMethod extends WidgetChildrenRead:
  def paintChildren(paint:PaintCtx):Unit =
    PaintChildren.paint(children, paint)

trait PaintChildren extends PaintStack with PaintChildrenMethod:
  paintStack.add(paintChildren)

object PaintChildren:
  implicit val logger: Logger = LoggerFactory.getLogger("xyz.cofe.term.ui.PaintChildren")

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
      if widget.visible.value.get then paintChild()
    }

