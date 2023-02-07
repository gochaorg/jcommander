package xyz.cofe.term.ui
package ses

import java.util.concurrent.atomic.AtomicInteger

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import xyz.cofe.term.buff.ChangeMetricBuffer
import xyz.cofe.term.buff.Buffer
import xyz.cofe.term.paint.BasicPaintCtx
import xyz.cofe.term.common.Position
import xyz.cofe.term.common.Size
import xyz.cofe.term.buff.ScreenBufSync
import xyz.cofe.metric._
import xyz.cofe.log._

trait SesPaint extends SesBase:
  private implicit val logger: Logger = LoggerFactory.getLogger("xyz.cofe.term.ui.ses.SesPaint")

  val screenBuffer = ChangeMetricBuffer(Buffer())
  val repaintRequests = new AtomicInteger(1)

  def repaint(widget:Widget):Unit = {
    repaintRequests.incrementAndGet()
  }

  private var rememberedCursorPosition : Option[Position] = None
  private var rememberedCursorVisible : Boolean = false

  def remeberCursorInfo( pos:Position, visible:Boolean ):Unit = 
    debug"remeberCursorInfo( $pos, $visible )"
    rememberedCursorPosition = Some(pos)
    rememberedCursorVisible = visible

  private val tRepaintRoot = Metrics.tracker("SesPaint.repaint.root")
  private val tRepaintSync = Metrics.tracker("SesPaint.repaint.sync")

  protected def repaint():Unit = 
    if repaintRequests.get()>0 
    then
      repaintRequests.set(0)

      rememberedCursorPosition = None
      rememberedCursorVisible = false

      tRepaintRoot{
        rootWidget.paint(BasicPaintCtx(
          screenBuffer, Position(0,0), Size(screenBuffer.width, screenBuffer.height), true
        ))
      }

      if(rememberedCursorPosition.isDefined && rememberedCursorVisible)
      then
        val err1 = (screenBuffer.cursorPos = rememberedCursorPosition.get)
        val err2 = (screenBuffer.cursorVisible = true)
        debug"set cursor visible at ${rememberedCursorPosition.get} ${err1} ${err2}"
      else
        val err1 = (screenBuffer.cursorVisible = false)
        debug"set cursor invisible ${err1}"

      tRepaintSync(ScreenBufSync.sync(console, screenBuffer))
