package xyz.cofe.term.ui

import xyz.cofe.term.paint.PaintCtx

trait PaintChildrenMethod extends WidgetChildren[_]:
  def paintChildren(paint:PaintCtx):Unit =
    children.get.foreach { widget => 
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

trait PaintChildren extends PaintStack with PaintChildrenMethod:
  paintStack.add(paintChildren)


