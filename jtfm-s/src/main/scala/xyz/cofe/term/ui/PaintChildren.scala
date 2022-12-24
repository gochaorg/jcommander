package xyz.cofe.term.ui

import xyz.cofe.term.paint.PaintCtx

trait PaintChildren extends PaintStack with WidgetChildren[_]:
  paintStack.set(
    paintStack.get :+ { paint => 
      paintChildren(paint)
    }
  )

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
        case visProp:VisibleProp if visProp.visible => paintChild()
        case _ => paintChild()
    }

