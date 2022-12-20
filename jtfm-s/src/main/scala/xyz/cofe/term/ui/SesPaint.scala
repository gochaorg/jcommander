package xyz.cofe.term.ui

import xyz.cofe.term.buff.ChangeMetricBuffer
import xyz.cofe.term.buff.Buffer
import xyz.cofe.term.paint.BasicPaintCtx
import java.util.concurrent.atomic.AtomicInteger
import xyz.cofe.term.common.Position
import xyz.cofe.term.common.Size
import xyz.cofe.term.buff.ScreenBufSync


trait SesPaint extends SesBase:
  protected val screenBuffer = ChangeMetricBuffer(Buffer())
  protected val repaintRequests = new AtomicInteger(1)

  def repaint(widget:Widget):Unit = {
    repaintRequests.incrementAndGet()
  }

  protected def repaint():Unit = 
    if repaintRequests.get()>0 
    then
      repaintRequests.set(0)

      // val ctx = ConsoleCtx(console,Position(0,0),console.getSize())
      // rootWidget.paint(ctx)

      rootWidget.paint(BasicPaintCtx(
        screenBuffer, Position(0,0), Size(screenBuffer.width, screenBuffer.height), true
      ))
      ScreenBufSync.sync(console, screenBuffer)
