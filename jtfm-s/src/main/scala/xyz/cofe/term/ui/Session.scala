package xyz.cofe.term.ui

import xyz.cofe.term.buff.ScreenBuffer
import xyz.cofe.term.buff.Buffer
import xyz.cofe.term.common.Console
import xyz.cofe.term.common.InputResizeEvent
import xyz.cofe.term.common.InputKeyEvent
import xyz.cofe.term.common.InputCharEvent
import xyz.cofe.term.common.InputMouseEvent

import xyz.cofe.term.paint.PaintCtx
import xyz.cofe.term.buff.ChangeMetricBuffer
import xyz.cofe.term.paint.BasicPaintCtx
import xyz.cofe.term.common.Position
import xyz.cofe.term.common.Size
import xyz.cofe.term.buff.ScreenBufSync
import java.util.concurrent.atomic.AtomicInteger
import xyz.cofe.term.paint.ConsoleCtx

class Session( val console: Console, initialize: => Unit )
extends SesBase 
  with SesJobs 
  with SesPaint
  with SesInput:
    
  object rootWidget extends Panel with RootWidget:
    def session:Session = Session.this

  @volatile var stop = false

  protected def startSession():Unit = {
    val conSize = console.getSize()
    screenBuffer.resize( conSize )
    rootWidget.size.set( conSize )

    initialize

    while( !stop ){
      processInput()
      processJobs()
      repaint()      
      Thread.sleep(1)
    }
  }

object Session:
  private val currentSessionTL :  ThreadLocal[Session] = new InheritableThreadLocal[Session]

  def currentSession:Option[Session] =
    currentSessionTL.synchronized {
      val ses = currentSessionTL.get()
      if ses!=null 
      then Some(ses)
      else None
    }

  private def setCurrentSession(ses:Session) =
    currentSessionTL.synchronized {
      val oldSes = currentSessionTL.get()
      if oldSes!=null 
      then throw new IllegalThreadStateException("session already created")
      else 
        currentSessionTL.set(ses)
    }

  def start(console: Console)(initialize: => Unit):Session =
    require(console!=null)
    val ses = new Session(console, initialize)
    setCurrentSession(ses)
    ses.startSession()
    ses

  def addJob(job: =>Unit):Unit =
    currentSession.foreach( ses => ses.addJob( ()=>job ) )