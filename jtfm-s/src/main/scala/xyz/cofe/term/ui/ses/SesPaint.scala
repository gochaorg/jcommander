package xyz.cofe.term.ui
package ses

import xyz.cofe.term.buff.ChangeMetricBuffer
import xyz.cofe.term.buff.Buffer
import xyz.cofe.term.paint.BasicPaintCtx
import java.util.concurrent.atomic.AtomicInteger
import xyz.cofe.term.common.Position
import xyz.cofe.term.common.Size
import xyz.cofe.term.buff.ScreenBufSync
import xyz.cofe.metric._

trait SesPaint extends SesBase:
  val screenBuffer = ChangeMetricBuffer(Buffer())
  val repaintRequests = new AtomicInteger(1)

  def repaint(widget:Widget):Unit = {
    repaintRequests.incrementAndGet()
  }

  private val tRepaintRoot = Metrics.tracker("SesPaint.repaint.root")
  private val tRepaintSync = Metrics.tracker("SesPaint.repaint.sync")

  protected def repaint():Unit = 
    if repaintRequests.get()>0 
    then
      repaintRequests.set(0)

      tRepaintRoot{
        rootWidget.paint(BasicPaintCtx(
          screenBuffer, Position(0,0), Size(screenBuffer.width, screenBuffer.height), true
        ))
      }

      tRepaintSync(ScreenBufSync.sync(console, screenBuffer))
