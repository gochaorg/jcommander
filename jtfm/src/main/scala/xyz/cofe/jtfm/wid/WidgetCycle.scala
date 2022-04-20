package xyz.cofe.jtfm.wid

import com.googlecode.lanterna.terminal.Terminal
import xyz.cofe.jtfm.Navigate
import xyz.cofe.jtfm.NavigateFilter
import com.googlecode.lanterna.screen.Screen
import com.googlecode.lanterna.screen.TerminalScreen
import com.googlecode.lanterna.terminal.TerminalResizeListener
import com.googlecode.lanterna.TerminalSize
import xyz.cofe.jtfm.gr.Rect
import xyz.cofe.jtfm.wid.wc.*
import xyz.cofe.jtfm.wid.wc.State.Work

/** Цикл управления событиями ввода и рендера.
  *
  * <p> 1 поток = 1 цикл обработки
  */
class WidgetCycle(
  private val terminal: Terminal
) {
  import Widget._

  /** Виджеты */
  val root: VirtualWidgetRoot = VirtualWidgetRoot()

  @volatile
  private var _state_ : State = State.Init(root, terminal)

  /** Текщее состояние */
  def state:State = _state_
  private def state_= (s:State) = _state_ = s
  
  @volatile
  private var stopNow = false
  
  trait Stopper {
    def await():Unit
  }
  
  def stop():Stopper = {
    stopNow = true
    new Stopper {
      def await():Unit = {
        while
          !state.isInstanceOf[State.End]
        do
          Thread.sleep(10)
      }
    }
  }
  
  /**
   * Запуск цикла обработки событий
   */
  def run(): Unit = {
    while( (!state.isInstanceOf[State.End]) ){
      if( stopNow ){
        state = state.finish()
      }else {
        val newState = state match {
          case s: State.Init => s.run()
          case s: State.Work => s.run()
          case _ => state
        }
        state = newState
      }
    }
  }
  
  def jobs: Option[Jobs] = state match {
    case s: xyz.cofe.jtfm.wid.wc.State.Init =>
      Some(s.jobs)
    case s: xyz.cofe.jtfm.wid.wc.State.Work =>
      Some(s.jobs)
    case _: xyz.cofe.jtfm.wid.wc.State.End =>
      None
  }
  
  def workState: Option[Work] = state match {
    case s: xyz.cofe.jtfm.wid.wc.State.Work => Some(s)
    case _ => None
  }
}

object WidgetCycle {
  private val currentCycle: InheritableThreadLocal[WidgetCycle] =
    new InheritableThreadLocal()

  def apply(term: Terminal): Either[Error,WidgetCycle] = {
    val wc0 = currentCycle.get
    if (wc0 == null) {
      val wc1 = new WidgetCycle(term)
      currentCycle.set(wc1)
      Right(wc1)
    } else {
      if (wc0.terminal == term) {
        Right(wc0)
      } else {
        Left(
          new Error("has already created WidgetCycle for different Terminal")
        )
      }
    }
  }

  /** 
    * Получение цикла обработки UI для текущего потока
    * @return цикл
    */
  def tryGet: Option[WidgetCycle] = {
    val x = currentCycle.get
    if (x != null) Some(x) else None
  }

  def jobs: Option[Jobs] = for { 
    wc <- tryGet
    j <- wc.jobs
  } yield(j)
}
