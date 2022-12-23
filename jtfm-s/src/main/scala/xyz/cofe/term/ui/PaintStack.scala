package xyz.cofe.term.ui

import xyz.cofe.term.paint.PaintCtx
import xyz.cofe.lazyp.ReadWriteProp
import xyz.cofe.term.common.Color
import xyz.cofe.term.buff.ScreenChar
import javafx.scene.layout.Background

trait PaintStack extends Widget:
  val paintStack: ReadWriteProp[List[PaintCtx => Unit]] = ReadWriteProp(List())
  override def paint(paintCtx: PaintCtx): Unit = 
    paintStack.get.foreach { fn => fn(paintCtx) }

extension (paintStack: ReadWriteProp[List[PaintCtx => Unit]])
  def add( render:PaintCtx => Unit ):Unit =
    paintStack.set(
      paintStack.get :+ render
    )

trait BackgroundColor:
  val backgroundColor: ReadWriteProp[Color] = ReadWriteProp(Color.Black)

trait ForegroundColor:
  val foregroundColor: ReadWriteProp[Color] = ReadWriteProp(Color.White)

trait FillBackground extends PaintStack with BackgroundColor:
  paintStack.set(
    paintStack.get :+ { paint => 
      val chr = ScreenChar(' ',Color.White, backgroundColor.get)
      (0 until paint.bounds.size.height()).flatMap { y => 
        (0 until paint.bounds.size.width()).map { x => (x,y) }
      }.foreach { case (x,y) => 
        paint.write(x,y,chr)
      }
    }
  )  

trait PaintChildren extends PaintStack with WidgetChildren[_]:
  paintStack.set(
    paintStack.get :+ { paint => 
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
    }
  )

trait TextProperty extends Widget:
  val text: ReadWriteProp[String] = ReadWriteProp("text")
  text.onChange { repaint }

trait PaintText extends PaintStack with TextProperty with ForegroundColor:
  paintStack.set(
    paintStack.get :+ { paint =>
      paintText(paint)
    }
  )
  
  def paintText( paint:PaintCtx ):Unit =
    paint.foreground = foregroundColor.get
    if this.isInstanceOf[BackgroundColor] then paint.background = this.asInstanceOf[BackgroundColor].backgroundColor.get
    paint.write(0,0,text.get)
