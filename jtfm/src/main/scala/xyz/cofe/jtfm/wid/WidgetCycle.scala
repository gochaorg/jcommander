package xyz.cofe.jtfm.wid

import com.googlecode.lanterna.terminal.Terminal
import xyz.cofe.jtfm.Navigate
import xyz.cofe.jtfm.NavigateFilter
import com.googlecode.lanterna.screen.Screen
import com.googlecode.lanterna.screen.TerminalScreen
import com.googlecode.lanterna.terminal.TerminalResizeListener
import com.googlecode.lanterna.TerminalSize

import xyz.cofe.jtfm.gr.Rect
import xyz.cofe.jtfm.wid.wc._

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

  private var _state_ : State = State.Init(root, terminal)
  def state:State = _state_
  private def state_= (s:State) = _state_ = s

  /**
   * Запуск цикла обработки событий
   */
  def run(): Unit = {
    while( !state.isInstanceOf[State.End] ){
      val newState = state match {
        case s:State.Init => s.run()
        case s:State.Work => s.run()
        case _ => state
      }
      state = newState      
    }
  }
}

object WidgetCycle {
  private val currentCycle: InheritableThreadLocal[WidgetCycle] =
    new InheritableThreadLocal()

  def apply(term: Terminal): Either[WidgetCycle, Error] = {
    val wc0 = currentCycle.get
    if (wc0 == null) {
      val wc1 = new WidgetCycle(term)
      currentCycle.set(wc1)
      Left(wc1)
    } else {
      if (wc0.terminal == term) {
        Left(wc0)
      } else {
        Right(
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
}
