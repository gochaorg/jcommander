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
import java.util.Queue
import java.util.concurrent.ConcurrentLinkedQueue

class Session( console: Console, initialize: => Unit ):
  object rootWidget extends Panel with RootWidget
  val screenBuffer = ChangeMetricBuffer(Buffer())
  @volatile var stop = false

  private val jobs:Queue[()=>Unit] = new ConcurrentLinkedQueue()

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

  val repaintRequests = new AtomicInteger(1)

  def repaint(widget:Widget):Unit = {
    repaintRequests.incrementAndGet()
  }
  
  private def processInput():Unit =
    val inputEvOpt = console.read()
    if( inputEvOpt.isPresent() ){
      val inputEv = inputEvOpt.get()
      inputEv match
        case resizeEv:InputResizeEvent =>
          val size = resizeEv.size()
          screenBuffer.resize(size)
          rootWidget.size.set(size)
        case _ => 
          rootWidget.children.nested.foreach { path => 
            path.last match
              case wInput:WidgetInput =>
                wInput.input(inputEv)
              case _ =>
          }
    }

  private def processJobs():Unit = 
    var stop = false
    while !stop do
      val job = jobs.poll()
      if job!=null then
        job()
      else
        stop = true

  def addJob( job:()=>Unit ):Unit = 
    jobs.add(job)

  private def repaint():Unit = 
    if repaintRequests.get()>0 
    then
      repaintRequests.set(0)

      // val ctx = ConsoleCtx(console,Position(0,0),console.getSize())
      // rootWidget.paint(ctx)

      rootWidget.paint(BasicPaintCtx(
        screenBuffer, Position(0,0), Size(screenBuffer.width, screenBuffer.height), true
      ))
      ScreenBufSync.sync(console, screenBuffer)

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