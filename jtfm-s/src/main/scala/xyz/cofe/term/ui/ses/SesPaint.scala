package xyz.cofe.term.ui
package ses

import xyz.cofe.term.buff.ChangeMetricBuffer
import xyz.cofe.term.buff.Buffer
import xyz.cofe.term.paint.BasicPaintCtx
import java.util.concurrent.atomic.AtomicInteger
import xyz.cofe.term.common.Position
import xyz.cofe.term.common.Size
import xyz.cofe.term.buff.ScreenBufSync
import xyz.cofe.term.buff.ScreenBufferSyncLog

trait SesPaint(log:SessionLog, syncLog: ScreenBufferSyncLog) extends SesBase:
  val screenBuffer = ChangeMetricBuffer(Buffer())
  val repaintRequests = new AtomicInteger(1)

  def repaint(widget:Widget):Unit = {
    repaintRequests.incrementAndGet()
  }

  protected def repaint():Unit = 
    if repaintRequests.get()>0 
    then      
      log("repaint by request")
      repaintRequests.set(0)

      log("paint root")
      try
        rootWidget.paint(BasicPaintCtx(
          screenBuffer, Position(0,0), Size(screenBuffer.width, screenBuffer.height), true
        ))

        log("sync ? sync ?")
        syncLog("sync ?")
        given syncLog1:ScreenBufferSyncLog = syncLog
        ScreenBufSync.sync(console, screenBuffer)
      catch
        case err: Throwable => 
          log(s"got error $err")