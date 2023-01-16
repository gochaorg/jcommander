package xyz.cofe.term.ui
package paint

import xyz.cofe.term.paint.PaintCtx
import xyz.cofe.lazyp.ReadWriteProp
import xyz.cofe.term.common.Color
import xyz.cofe.term.buff.ScreenChar
import javafx.scene.layout.Background
import xyz.cofe.lazyp.Prop

trait PaintStack extends Widget:
  val paintStack: ReadWriteProp[List[PaintCtx => Unit]] = ReadWriteProp(List())
  override def paint(paintCtx: PaintCtx): Unit = 
    paintStack.get.foreach { fn => fn(paintCtx) }

extension (paintStack: ReadWriteProp[List[PaintCtx => Unit]])
  def add( render:PaintCtx => Unit ):Unit =
    paintStack.set(
      paintStack.get :+ render
    )
